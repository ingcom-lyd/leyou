package com.leyou.item.mapper;

import com.leyou.item.pojo.Brand;
import org.apache.ibatis.annotations.*;
import tk.mybatis.mapper.additional.idlist.IdListMapper;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface BrandMapper extends Mapper<Brand>, IdListMapper<Brand,Long> {

    @Insert("INSERT INTO tb_category_brand (category_id,brand_id) VALUES(#{cid},#{bid})")
    int insertCategoryBrand(@Param("cid")Long cid,@Param("bid")Long bid);

    @Update("update tb_category_brand set category_id=#{cid} where brand_id=#{bid}")
    int updateCategoryBrand(@Param("cid")Long cid,@Param("bid") Long bid);

    @Delete("delete from tb_category_brand where brand_id=#{id}")
    int deleteCategoryBrandById(@Param("id") Long id);

    @Select("select * from tb_brand b inner join tb_category_brand cb on b.id=cb.brand_id where cb.category_id=#{cid}")
    List<Brand> queryByCategoryId(@Param("cid")Long cid);

//    @Select("select category_id from tb_category_brand where brand_id=#{bid}")
//    List<Long> queryCategoryBrandByBid(@Param("bid")Long bid);
}
