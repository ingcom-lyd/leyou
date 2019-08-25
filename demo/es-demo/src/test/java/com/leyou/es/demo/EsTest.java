package com.leyou.es.demo;

import com.leyou.es.pojo.Item;
import com.leyou.es.repository.ItemRepository;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SourceFilter;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class EsTest {

    @Autowired
    private ElasticsearchTemplate template;
    @Autowired
    private ItemRepository repository;

    @Test
    public void testCreate() {
        //创建索引库
        template.createIndex(Item.class);
        //映射关系
        template.putMapping(Item.class);
    }

    @Test
    public void indexList() {
        List<Item> list = new ArrayList<>();
        list.add(new Item(1L, "小米手机7", "手机", "小米", 3299.00, "http://image.leyou.com/13123.jpg"));
        list.add(new Item(2L, "坚果手机R1", "手机", "锤子", 3699.00, "http://image.leyou.com/13123.jpg"));
        list.add(new Item(3L, "华为META10", "手机", "华为", 4499.00, "http://image.leyou.com/13123.jpg"));
        list.add(new Item(4L, "小米Mix2S", "手机", "小米", 4299.00, "http://image.leyou.com/13123.jpg"));
        list.add(new Item(5L, "荣耀V10", "手机", "华为", 2799.00, "http://image.leyou.com/13123.jpg"));
        // 接收对象集合，实现批量新增
        repository.saveAll(list);
    }

    @Test
    public void testFind(){
        Iterable<Item> all = repository.findAll();
        for (Item item : all) {
            System.out.println("item:"+item);
        }
    }

    @Test
    public void testFindBy(){
        List<Item> list = repository.findByPriceBetween(2000D, 4000D);
        System.out.println(list);

    }

    @Test
    public void testQuery(){
        //创建查询构建器
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //结果过滤
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id","title","price"},null));
        //添加查询条件
        queryBuilder.withQuery(QueryBuilders.matchQuery("title", "小米手机"));
        //排序
        queryBuilder.withSort(SortBuilders.fieldSort("price").order(SortOrder.DESC));
        //分页
        queryBuilder.withPageable(PageRequest.of(0,2));

        Page<Item> result = repository.search(queryBuilder.build());

        long total = result.getTotalElements();
        System.out.println("total = " + total);

        int totalPages = result.getTotalPages();
        System.out.println("totalPages = " + totalPages);

        List<Item> itemList = result.getContent();
        for (Item item : itemList) {
            System.out.println("item = " + item);
        }
    }

    @Test
    public void testAgg(){
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        String aggName = "popularBrand";

        //聚合
        queryBuilder.addAggregation(AggregationBuilders.terms(aggName).field("brand"));

        //查询并返回带聚合的结果
        AggregatedPage<Item> result = template.queryForPage(queryBuilder.build(), Item.class);

        //解析聚合
        Aggregations aggregations = result.getAggregations();

        //获取指定名字的聚合
        StringTerms agg = aggregations.get(aggName);

        //获取通
        List<StringTerms.Bucket> buckets = agg.getBuckets();
        for (StringTerms.Bucket bucket : buckets) {
            System.out.println("key = " + bucket.getKeyAsString());
            System.out.println("doc_count= " + bucket.getDocCount());
        }
    }
}
