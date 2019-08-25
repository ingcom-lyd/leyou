package com.leyou.item.web;

import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.Brand;
import com.leyou.item.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("brand")
public class BrandController {

    @Autowired
    private BrandService brandService;

    /**
     * 分页查询品牌
     * @param page
     * @param rows
     * @param sortBy
     * @param desc
     * @param key
     * @return
     */
    @GetMapping("page")
    public ResponseEntity<PageResult<Brand>> queryBrandByPage(
            @RequestParam(value = "page",defaultValue = "1")Integer page,
            @RequestParam(value = "rows",defaultValue = "5")Integer rows,
            @RequestParam(value = "sortBy",required = false)String sortBy,
            @RequestParam(value = "desc",defaultValue = "false")Boolean desc,
            @RequestParam(value = "key",required = false)String key)
    {
        PageResult<Brand> result = brandService.queryBrandByPage(page,rows,sortBy,desc,key);

        return ResponseEntity.ok(result);
    }

    /**
     * 新增品牌
     * @param brand
     * @param cids
     * @return
     */
    @PostMapping
//    public ResponseEntity<Void> saveBrand(Brand brand, Long[] categories,@RequestParam("categories[0]")Long cid, @RequestParam("id")List<Long> id){
    public ResponseEntity<Void> saveBrand(Brand brand,@RequestParam("categories")List<Long> cids){

        brandService.saveBrand(brand,cids);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 更新品牌
     * @param brand
     * @param cids
     * @return
     */
    @PutMapping
    public ResponseEntity<Void> updateBrand(Brand brand,@RequestParam("categories")List<Long> cids){
        brandService.updateBrand(brand,cids);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * 删除品牌
     * @param id
     * @return
     */
    @DeleteMapping
    public ResponseEntity<Void> deleteBrandById(@RequestParam("id")Long id){
        brandService.deleteBrandById(id);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * 根据分类id查询品牌
     * @param cid
     * @return
     */
    @GetMapping("cid/{cid}")
    public ResponseEntity<List<Brand>> queryBrandByCid(@PathVariable("cid")Long cid){
        return ResponseEntity.ok(brandService.queryBrandByCid(cid));
    }

    /**
     * 根据id查询品牌
     * @param id
     * @return
     */
    @GetMapping("{id}")
    public ResponseEntity<Brand> queryBrandById(@PathVariable("id")Long id){
        return ResponseEntity.ok(brandService.queryBrandById(id));
    }

    /**
     * 根据多个id查询品牌
     * @param ids
     * @return
     */
    @GetMapping("list")
    public ResponseEntity<List<Brand>> queryBrandByIds(@RequestParam("ids")List<Long> ids){

        return ResponseEntity.ok(brandService.queryBrandByIds(ids));
    }
}
