package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.dto.CartDTO;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.vo.PageResult;
import com.leyou.item.mapper.*;
import com.leyou.item.pojo.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class GoodsService {

    @Autowired
    private SpuMapper spuMapper;
    @Autowired
    private SpuDetailMapper spuDetailMapper;
    @Autowired
    private BrandMapper brandMapper;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private SkuMapper skuMapper;
    @Autowired
    private StockMapper stockMapper;
    @Autowired
    private AmqpTemplate amqpTemplate;

    public PageResult<Spu> querySpuByPage(Integer page, Integer rows, Boolean saleable, String key) {
        //分页
        PageHelper.startPage(page,rows);
        //过滤
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        //搜索字段排序
        if (StringUtils.isNotBlank(key)){
            criteria.andLike("title","%"+key+"%");
        }
        //上下架排序
        if (saleable != null) {
            criteria.andEqualTo("saleable",saleable);
        }
        //默认排序
        String clause = "last_update_time DESC";
        example.setOrderByClause(clause);
        //判断
        List<Spu> list = spuMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(list)){
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }

        //解析分类和品牌的名称
        loadCategoryAndBrandName(list);

        //解析分页结果
        PageInfo<Spu> info = new PageInfo<>(list);
        PageResult<Spu> result = new PageResult<>();
        result.setItems(list);
        result.setTotal(info.getTotal());

        return result;
    }

    private void loadCategoryAndBrandName(List<Spu> spus) {
        for (Spu spu : spus) {
            Brand brand = brandMapper.selectByPrimaryKey(spu.getBrandId());
            if (brand == null) {
                throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
            }

//            spu.setBname(brand.getName());
            spu.setBrandName(brand.getName());

            List<Long> ids = Arrays.asList(spu.getCid1(),spu.getCid2(),spu.getCid3());
            List<String> names = categoryService.queryByIds(ids).stream().map(Category::getName).collect(Collectors.toList());
//            spu.setCname(StringUtils.join(names,"/"));
            spu.setCategoryName(StringUtils.join(names,"/"));
        }
    }

    @Transactional
    public void saveGoods(Spu spu) {
        //新增spu
        spu.setCreateTime(new Date());
        spu.setLastUpdateTime(new Date());
        spu.setId(null);
        spu.setSaleable(true);
        spu.setValid(false);

        int count = spuMapper.insert(spu);
        if (count != 1) {
            throw new LyException(ExceptionEnum.GOODS_SAVE_ERROR);
        }
        //新增spudetail
        SpuDetail detail = spu.getSpuDetail();
        detail.setSpuId(spu.getId());
        spuDetailMapper.insert(detail);
        //新增sku和库存
        saveSkuAndStock(spu);

        //发送mq消息
        amqpTemplate.convertAndSend("item.insert",spu.getId());
    }

    public SpuDetail queryDetailById(Long spuId) {
        SpuDetail detail = spuDetailMapper.selectByPrimaryKey(spuId);
        if (detail == null){
            throw new LyException(ExceptionEnum.GOODS_DETAIL_NOT_FOUND);
        }

        return detail;
    }

    public List<Sku> querySkuBySpuId(Long spuId) {
        Sku sku = new Sku();
        sku.setSpuId(spuId);

        List<Sku> skus = skuMapper.select(sku);
        if (CollectionUtils.isEmpty(skus)) {
            throw new LyException(ExceptionEnum.GOODS_SKU_NOT_FOUND);
        }
        //查询库存
        loadStockInSku(skus);

//        List<Long> ids = skus.stream().map(Sku::getId).collect(Collectors.toList());
//        List<Stock> stockList = stockMapper.selectByIdList(ids);
//        if (CollectionUtils.isEmpty(stockList)) {
//            throw new LyException(ExceptionEnum.GOODS_STOCK_NOT_FOUND);
//        }

        return skus;
    }

    @Transactional
    public void updateGoods(Spu spu) {
        if (spu.getId() == null){
            throw new LyException(ExceptionEnum.GOODS_ID_CANNOT_BE_NULL);
        }
        Sku sku = new Sku();
        sku.setSpuId(spu.getId());
        //查询sku
        List<Sku> skuList = skuMapper.select(sku);
        if (! CollectionUtils.isEmpty(skuList)) {
            //删除sku
            skuMapper.delete(sku);
            //删除stock
            List<Long> ids = skuList.stream().map(Sku::getId).collect(Collectors.toList());
            stockMapper.deleteByIdList(ids);
        }
        //修改spu
        spu.setValid(null);
        spu.setSaleable(null);
        spu.setLastUpdateTime(null);
        spu.setCreateTime(null);

        int count = spuMapper.updateByPrimaryKeySelective(spu);
        if (count != 1) {
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        //修改detail
        count = spuDetailMapper.updateByPrimaryKeySelective(spu.getSpuDetail());
        if (count != 1) {
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        //新增sku和stock
        saveSkuAndStock(spu);

        //发送mq消息
        amqpTemplate.convertAndSend("item.update",spu.getId());
    }

    public void saveSkuAndStock(Spu spu){
        int count;
        //定义库存集合
        List<Stock> stockList = new ArrayList<>();
        for (Sku sku : spu.getSkus()) {
            //新增sku
            sku.setSpuId(spu.getId());
            sku.setCreateTime(new Date());
            sku.setLastUpdateTime(new Date());

            count = skuMapper.insert(sku);
            if (count != 1) {
                throw new LyException(ExceptionEnum.GOODS_SAVE_ERROR);
            }

            //新增stock
//            Stock stock = new Stock();
            Stock stock = sku.getStock();
            stock.setSkuId(sku.getId());
//            stock.setStock(sku.getStock());

            stockList.add(stock);
        }

        //批量新增库存
        count = stockMapper.insertList(stockList);
        if (count != stockList.size()){
            throw new LyException(ExceptionEnum.GOODS_SAVE_ERROR);
        }
    }

    @Transactional
    public void deleteGoodsBySpuId(Long spuId) {
        //删除spu
        int result = spuMapper.deleteByPrimaryKey(spuId);
        if (result != 1){
            throw new LyException(ExceptionEnum.GOODS_DELETE_ERROR);
        }
        //删除spuDetail
        result = spuDetailMapper.deleteByPrimaryKey(spuId);
        if (result != 1){
            throw new LyException(ExceptionEnum.GOODS_DELETE_ERROR);
        }
        //删除sku
        Sku sku = new Sku();
        sku.setSpuId(spuId);
        List<Sku> skuList = skuMapper.select(sku);
        if (CollectionUtils.isEmpty(skuList)) {
            throw new LyException(ExceptionEnum.GOODS_SKU_NOT_FOUND);
        }

        result = skuMapper.delete(sku);
        if (result != 1){
            throw new LyException(ExceptionEnum.GOODS_DELETE_ERROR);
        }
        //删除stock
        List<Long> ids = skuList.stream().map(Sku::getId).collect(Collectors.toList());

        result = stockMapper.deleteByIdList(ids);
        if (result != ids.size()){
            throw new LyException(ExceptionEnum.GOODS_DELETE_ERROR);
        }

        //发送mq消息
        amqpTemplate.convertAndSend("item.delete",spuId);
    }

    @Transactional
    public void shelfBySpuId(Long spuId,Boolean saleable) {
        Spu spu = new Spu();
        spu.setId(spuId);

        if (saleable){
            spu.setSaleable(false);
        }else {
            spu.setSaleable(true);
        }

        int result = spuMapper.updateByPrimaryKeySelective(spu);
        if (result != 1){
            throw new LyException(ExceptionEnum.GOODS_DOWN_SHELF_ERROR);
        }
    }

    public Spu querySpuById(Long id) {
        //查询spu
        Spu spu = spuMapper.selectByPrimaryKey(id);

        if (spu == null) {
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        //查询sku
        List<Sku> skuList = querySkuBySpuId(id);
        //查询detail
        SpuDetail spuDetail = queryDetailById(id);

        spu.setSkus(skuList);
        spu.setSpuDetail(spuDetail);

        return spu;
    }

    public List<Sku> querySkuBySpuIds(List<Long> ids) {
        List<Sku> skuList = new ArrayList<>();
        for (Long id : ids) {
            Sku sku = skuMapper.selectByPrimaryKey(id);
            skuList.add(sku);
        }
        if (CollectionUtils.isEmpty(skuList)) {
            throw new LyException(ExceptionEnum.GOODS_SKU_NOT_FOUND);
        }

        //查询库存
        loadStockInSku(skuList);

        return skuList;
    }

    private void loadStockInSku(List<Sku> skuList) {
        for (Sku s : skuList) {
            Stock stock = stockMapper.selectByPrimaryKey(s.getId());
            if (stock == null) {
                throw new LyException(ExceptionEnum.GOODS_STOCK_NOT_FOUND);
            }

            s.setStock(stock);
        }
    }

    @Transactional
    public void decreaseStock(List<CartDTO> carts) {
        for (CartDTO cart : carts) {
            int count = stockMapper.stockDecrease(cart.getSkuId(), cart.getNum());
            if (count != 1) {
                throw new LyException(ExceptionEnum.STOCK_NOT_ENOUGH);
            }
        }
    }
}
