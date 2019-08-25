package com.leyou.search.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.JsonUtils;
import com.leyou.item.pojo.*;
import com.leyou.search.client.*;
import com.leyou.search.pojo.Goods;
import com.leyou.search.pojo.SearchRequest;
import com.leyou.search.pojo.SearchResult;
import com.leyou.search.repository.GoodRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SearchService {
    @Autowired
    private SpecClient specClient;
    @Autowired
    private CategoryClient categoryClient;
    @Autowired
    private BrandClient brandClient;
    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private SpecificationClient specificationClient;
    @Autowired
    private GoodRepository goodRepository;
    @Autowired
    private ElasticsearchTemplate template;

    private static final Logger log = LoggerFactory.getLogger(SearchService.class);

    public Goods buildGoods(Spu spu){

        //查询分类
        List<Category> categories = categoryClient.queryCategoryByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
        if (CollectionUtils.isEmpty(categories)) {
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }

        List<String> names = categories.stream().map(Category::getName).collect(Collectors.toList());
        String s = StringUtils.join(names, " ");

        //查询品牌
        Brand brand = brandClient.queryBrandById(spu.getBrandId());
        if (brand == null) {
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }

        //搜索字段
        String all = spu.getTitle() + s +brand.getName();
        //查询sku
        List<Sku> skuList = goodsClient.querySkuBySpuId(spu.getId());
        if (CollectionUtils.isEmpty(skuList)) {
            throw new LyException(ExceptionEnum.GOODS_SKU_NOT_FOUND);
        }
        //对sku进行处理
        List<Map<String,Object>> skus = new ArrayList<>();
        for (Sku sku : skuList) {
            Map<String, Object> map = new HashMap<>();
            map.put("id",sku.getId());
            map.put("title",sku.getTitle());
            map.put("price",sku.getPrice());
            map.put("images", StringUtils.substringBefore(sku.getImages(),","));

            skus.add(map);
        }

        //sku价格集合
        List<Long> prices = skuList.stream().map(Sku::getPrice).collect(Collectors.toList());

        //查询商品详情
        SpuDetail spuDetail = goodsClient.queryDetailById(spu.getId());
        System.out.println("spu.getId() = " + spu.getId());

        //查询规格参数
        List<SpecParam> params = specClient.queryParamList(null, spu.getCid3(), true);
        //获取通用规格参数
        Map<Long, String> genericSpec = JsonUtils.parseMap(spuDetail.getGenericSpec(), Long.class, String.class);
        System.out.println("genericSpec = " + genericSpec);
        //获取特有规格参数,key是规格参数的名字，值是规格参数的值
        Map<Long, List<String>> specialSpec = JsonUtils.nativeRead(spuDetail.getSpecialSpec(), new TypeReference<Map<Long, List<String>>>() {
        });
        //规格参数，key是带规格参数的名字，值是规格参数的值
        Map<String,Object> specs = new HashMap<>();
        for (SpecParam param : params){
           //规格名称
           String key = param.getName();
           Object value = "";
           //判断是否是通用规格
           if (param.getGeneric()){
               value = genericSpec.get(param.getId());
               //判断是否是数值类型
               if (param.getNumeric()){
                   //处理成段
                   value = chooseSegment(value.toString(),param);
               }
           }else {
               value = specialSpec.get(param.getId());
           }
           //存入map
           specs.put(key,value);
       }
        System.out.println("Map = " + specs);
        System.out.println("---------------------------------------------------------------");

        //构建goods对象
        Goods goods = new Goods();
        goods.setBrandId(spu.getBrandId());
        goods.setCid1(spu.getCid1());
        goods.setCid2(spu.getCid2());
        goods.setCid3(spu.getCid3());
        goods.setSubTitle(spu.getSubTitle());
        goods.setId(spu.getId());
        goods.setCreateTime(spu.getCreateTime());

        goods.setAll(all);// 搜索字段，包含标题，分类，品牌，规格等
        goods.setPrice(prices);// 所有sku的价格集合
        goods.setSkus(JsonUtils.serialize(skus));// 所有sku的集合的json
        goods.setSpecs(specs);// 所有可以搜索的规格参数

        return goods;
    }

    public SearchResult search(SearchRequest request) {
        Integer page = request.getPage() - 1;
        Integer size = request.getSize();
        String key = request.getKey();
        String sortBy = request.getSortBy();
        Boolean descending = request.getDescending();

        //创建查询构造器
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //其他查询构造器
        QueryBuilder query = buildBasicQueryWithFilter(request);
        MatchQueryBuilder basicQuery = QueryBuilders.matchQuery("all", key).operator(Operator.AND);
        //结果过滤
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id","subTitle","skus"},null));
        //分页
        queryBuilder.withPageable(PageRequest.of(page,size));
        //过滤，这里特殊操作：将分类过滤为只有手机
//        queryBuilder.withQuery(QueryBuilders.matchQuery("all",key));
//        queryBuilder.withQuery(QueryBuilders.boolQuery().must(QueryBuilders.matchQuery("all",key)).filter(QueryBuilders.termQuery("cid3",76)).);

        queryBuilder.withQuery(query);

        //排序
        if (!sortBy.isEmpty()) {
            if (sortBy.equals("createTime") ){
                queryBuilder.withSort(SortBuilders.fieldSort(sortBy).order(SortOrder.DESC));
            }
            if (sortBy.equals("price")){
                if (descending){
                    queryBuilder.withSort(SortBuilders.fieldSort("price").order(SortOrder.DESC));
                }else{
                    queryBuilder.withSort(SortBuilders.fieldSort("price").order(SortOrder.ASC));
                }
            }
        }

        //聚合
        String categoryAggName = "category";
        String brandAggName = "brand";
        //商品分类进行聚合
        queryBuilder.addAggregation(AggregationBuilders.terms(categoryAggName).field("cid3"));
        //品牌进行聚合
        queryBuilder.addAggregation(AggregationBuilders.terms(brandAggName).field("brandId"));

        //查询，获取结果
        AggregatedPage<Goods> result = template.queryForPage(queryBuilder.build(),Goods.class);

        //解析结果
        List<Goods> goodsList = result.getContent();

        long total = result.getTotalElements();
        double totalPage = Math.ceil(total*1.0 / size);

        //通过品牌桶，获取品牌集合
        List<Brand> brandList = queryBrandByAggregations(result.getAggregations(), brandAggName);
        //通过分类桶，获取分类集合
        List<Category> categoryList = queryCategoryByAggregations(result.getAggregations(), categoryAggName);

        //规格参数进行聚合,根据商品分类判断是否要聚合
        List<Map<String,Object>> specs = new ArrayList<>();
        if (categoryList.size() == 1 || categoryList.size() == 2) {
            specs = getSpec(categoryList.get(0).getId(),basicQuery);
        }

        return new SearchResult(total,new Double(totalPage).intValue(),goodsList,categoryList,brandList,specs);
    }

    /**
     * 多条件查询
     * @param request
     * @return
     */
    private QueryBuilder buildBasicQueryWithFilter(SearchRequest request) {
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        //基本查询
        queryBuilder.must(QueryBuilders.matchQuery("all",request.getKey()).operator(Operator.AND));
        //过滤条件构造器
        BoolQueryBuilder filterQueryBuilder = QueryBuilders.boolQuery();
        //整理过滤条件
        Map<String, String> filter = request.getFilter();
        for (Map.Entry<String,String> entry : filter.entrySet()){
            String key = entry.getKey();
            String value = entry.getValue();
            //商品分类和品牌要特殊处理
            if (!key.equals("brandId") && !key.equals("cid3")){
                key = "specs." + key + ".keyword";

            }

            //字符串类型，进行term查询
            filterQueryBuilder.must(QueryBuilders.termQuery(key,value));
        }
        //添加过滤条件
        queryBuilder.filter(filterQueryBuilder);

        return queryBuilder;
    }

    /**
     * 商品规格参数聚合
     * @param cid
     * @param query
     * @return
     */
    private List<Map<String, Object>> getSpec(Long cid, QueryBuilder query) {
        try {
            //不管是全局参数还是sku参数，只要是搜索参数，都需要根据分类id查询出来
            List<SpecParam> params = specClient.queryParamList(null, cid, true);
            List<Map<String,Object>> specs = new ArrayList<>();

            NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
            queryBuilder.withQuery(query);

            //聚合规格参数
            for (SpecParam param : params) {
                String key = param.getName();
                queryBuilder.addAggregation(AggregationBuilders.terms(key).field("specs." + key + ".keyword"));
            }

            //查询
            Map<String, Aggregation> aggs = template.query(queryBuilder.build(), SearchResponse::getAggregations).asMap();

            //解析聚合结果
            for (SpecParam param : params) {
                Map<String,Object> spec = new HashMap<>();

                String key = param.getName();
                spec.put("k",key);
                StringTerms terms = (StringTerms) aggs.get(key);
                List<StringTerms.Bucket> buckets = terms.getBuckets();
                spec.put("options",buckets.stream().map(StringTerms.Bucket::getKeyAsString));
                specs.add(spec);
            }

            return specs;

        }catch (Exception e){
            log.error("规格聚合出现异常：", e);
        }

        return null;
    }

    /**
     * 解析品牌桶结果
     * @param aggregations
     * @param aggName
     * @return
     */
    public List<Brand> queryBrandByAggregations(Aggregations aggregations,String aggName){
        try {
            LongTerms terms = aggregations.get(aggName);
            List<LongTerms.Bucket> buckets = terms.getBuckets();

            List<Long> ids = new ArrayList<>();
            for (LongTerms.Bucket bucket : buckets) {
                ids.add(bucket.getKeyAsNumber().longValue());
            }

            return brandClient.queryBrandByIds(ids);
        }catch (Exception e){
            log.error("分类聚合出现异常：", e);
        }

        return null;
    }

    /**
     * 解析商品分类桶结果
     * @param aggregations
     * @param aggName
     * @return
     */
    public List<Category> queryCategoryByAggregations(Aggregations aggregations,String aggName){
        try {
            LongTerms terms = aggregations.get(aggName);
            List<LongTerms.Bucket> buckets = terms.getBuckets();

            List<Long> ids = new ArrayList<>();
            for (LongTerms.Bucket bucket : buckets) {
                ids.add(bucket.getKeyAsNumber().longValue());
            }

            List<Category> categoryList = categoryClient.queryCategoryByIds(ids);

            List<Category> categories = new ArrayList<>();
            for (int i = 0;i <categoryList.size();i++){
                Category category = new Category();

                category.setId(categoryList.get(i).getId());
                category.setName(categoryList.get(i).getName());
                categories.add(category);
            }

            return categories;

        }catch (Exception e){
            log.error("分类聚合出现异常：", e);
        }

        return null;
    }

    private String chooseSegment(String value, SpecParam p) {
        double val = NumberUtils.toDouble(value);
        String result = "其它";
        // 保存数值段
        for (String segment : p.getSegments().split(",")) {
            String[] segs = segment.split("-");
            // 获取数值范围
            double begin = NumberUtils.toDouble(segs[0]);
            double end = Double.MAX_VALUE;
            if(segs.length == 2){
                end = NumberUtils.toDouble(segs[1]);
            }
            // 判断是否在范围内
            if(val >= begin && val < end){
                if(segs.length == 1){
                    result = segs[0] + p.getUnit() + "以上";
                }else if(begin == 0){
                    result = segs[1] + p.getUnit() + "以下";
                }else{
                    result = segment + p.getUnit();
                }
                break;
            }
        }
        return result;
    }

    public void createOrUpdateIndex(Long spuId) {
        //查询spu
        Spu spu = goodsClient.querySpuById(spuId);
        //构建goods
        Goods goods = buildGoods(spu);
        //存入索引库
        goodRepository.save(goods);
    }

    public void deleteIndex(Long spuId) {
        goodRepository.deleteById(spuId);
    }
}
