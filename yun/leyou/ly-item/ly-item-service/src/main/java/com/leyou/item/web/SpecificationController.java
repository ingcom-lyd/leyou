package com.leyou.item.web;

import com.leyou.item.service.SpecificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("spec")
public class SpecificationController {
    @Autowired
    private SpecificationService specService;

    /**
     * 根据分类id查询规格
     * @param cid
     * @return
     */
    @GetMapping("{cid}")
//    @GetMapping("groups/{cid}")
//    public ResponseEntity<List<Specification>> queryGroupById(@PathVariable("cid")Long cid){
    public ResponseEntity<String> querySpecificationById(
            @PathVariable("cid")Long cid,
            @RequestParam(value = "searching",required = false)Boolean searching){
        return ResponseEntity.ok(specService.querySpecificationById(cid));
    }

    /**
     * 新增规格参数
     * @param categoryId
     * @param specifications
     * @return
     */
    @PostMapping
    public ResponseEntity<Void> saveSpecification(@RequestParam("categoryId")Long categoryId,@RequestParam("specifications")String specifications){
        specService.saveSpecification(categoryId,specifications);

        return  ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * 更新规格参数
     * @param categoryId
     * @param specifications
     * @return
     */
    @PutMapping
    public ResponseEntity<Void> updateSpecification(@RequestParam("categoryId")Long categoryId,@RequestParam("specifications")String specifications){
        specService.updateSpecification(categoryId,specifications);

        return  ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * 删除规格参数
     * @param categoryId
     * @return
     */
    @DeleteMapping("{categoryId}")
    public ResponseEntity<Void> deleteSpecificationById(@PathVariable("categoryId")Long categoryId){
        specService.deleteSpecificationById(categoryId);

        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
