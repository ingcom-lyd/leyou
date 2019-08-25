package com.leyou.item.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("spec")
public interface SpecificationAPI {
    /**
     * 根据分类id查询规格
     * @param cid
     * @return
     */
    @GetMapping("{cid}")
//    @GetMapping("groups/{cid}")
//    public ResponseEntity<List<Specification>> queryGroupById(@PathVariable("cid")Long cid){
    String querySpecificationById(
            @PathVariable("cid")Long cid,
            @RequestParam(value = "searching",required = false)Boolean searching);
}
