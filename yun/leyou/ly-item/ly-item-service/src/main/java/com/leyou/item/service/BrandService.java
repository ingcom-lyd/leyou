package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.vo.PageResult;
import com.leyou.item.mapper.BrandMapper;
import com.leyou.item.pojo.Brand;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class BrandService {

    @Autowired
    private BrandMapper brandMapper;

    public PageResult<Brand> queryBrandByPage(Integer page, Integer rows, String sortBy, Boolean desc, String key) {
        //分页
        PageHelper.startPage(page,rows);
        //过滤
        Example example = new Example(Brand.class);
        if (StringUtils.isNotBlank(key)){
            //过滤条件
            Example.Criteria criteria = example.createCriteria();
            criteria.orLike("name","%"+key+"%");
            criteria.orEqualTo("letter",key.toUpperCase());
        }
        //排序
        String orderByClause = sortBy + (desc ? " DESC":" ASC");
        example.setOrderByClause(orderByClause);
        //查询

        List<Brand> list = brandMapper.selectByExample(example);

        if(CollectionUtils.isEmpty(list)){
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }

        //解析分页结果
        PageInfo<Brand> info = new PageInfo<>(list);

        return new PageResult<Brand>(info.getTotal(),list);
    }

    @Transactional
    public void saveBrand(Brand brand, List<Long> cids) {
        //新增品牌
        brand.setId(null);
        int count = brandMapper.insert(brand);

        if (count != 1){
            throw new LyException(ExceptionEnum.BRAND_SAVE_ERROR);
        }

        for (Long cid : cids) {
            count = brandMapper.insertCategoryBrand(cid, brand.getId());
            if (count != 1) {
                throw new LyException(ExceptionEnum.BRAND_SAVE_ERROR);
            }
        }
    }


    @Transactional
    public void updateBrand(Brand brand, List<Long> cids) {
        int result = brandMapper.updateByPrimaryKeySelective(brand);
        if (result != 1){
            throw new LyException(ExceptionEnum.BRAND_SAVE_ERROR);
        }

        result = brandMapper.deleteCategoryBrandById(brand.getId());

        for (Long cid : cids) {

            result = brandMapper.insertCategoryBrand(cid,brand.getId());

            if (result != 1){
                throw new LyException(ExceptionEnum.BRAND_SAVE_ERROR);
            }
        }
    }

    @Transactional
    public void deleteBrandById(Long id) {
        int result = brandMapper.deleteByPrimaryKey(id);
        if (result != 1) {
            throw new LyException(ExceptionEnum.BRAND_DELETE_ERROR);
        }

        int result2 = brandMapper.deleteCategoryBrandById(id);
        if (result2 != 1) {
            throw new LyException(ExceptionEnum.BRAND_DELETE_ERROR);
        }
    }

    public List<Brand> queryBrandByCid(Long cid) {
        List<Brand> brands = brandMapper.queryByCategoryId(cid);
        if (CollectionUtils.isEmpty(brands)){
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }

        return brands;
    }

    public Brand queryBrandById(Long id) {
        Brand brand = brandMapper.selectByPrimaryKey(id);
        if (brand == null) {
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }

        return brand;
    }

    public List<Brand> queryBrandByIds(List<Long> ids) {
        List<Brand> brandList = brandMapper.selectByIdList(ids);
        if (CollectionUtils.isEmpty(brandList)) {
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }

        return brandList;
    }
}
