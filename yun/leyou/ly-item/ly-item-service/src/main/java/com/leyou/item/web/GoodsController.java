package com.leyou.item.web;

import com.leyou.common.dto.CartDTO;
import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuDetail;
import com.leyou.item.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping
public class GoodsController {

    @Autowired
    private GoodsService goodsService;

    /**
     * 分页查询SPU
     * @param page
     * @param rows
     * @param saleable
     * @param key
     * @return
     */
    @GetMapping("spu/page")
    public ResponseEntity<PageResult<Spu>> querySpuByPage(
            @RequestParam(value = "page",defaultValue = "1")Integer page,
            @RequestParam(value = "rows",defaultValue = "5")Integer rows,
            @RequestParam(value = "saleable",required = false)Boolean saleable,
            @RequestParam(value = "key",required = false)String key
    ){
        return ResponseEntity.ok(goodsService.querySpuByPage(page,rows,saleable,key));
    }

    /**
     * 商品新增
     * @param spu
     * @return
     */
    @PostMapping("goods")
    public ResponseEntity<Void> saveGoods(@RequestBody Spu spu){
        goodsService.saveGoods(spu);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 商品更新
     * @param spu
     * @return
     */
    @PutMapping("goods")
    public ResponseEntity<Void> updateGoods(@RequestBody Spu spu){
        goodsService.updateGoods(spu);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 根据spu的id查询spuDetail
     * @param spuId
     * @return
     */
    @GetMapping("goods/spu/detail/{sid}")
    public ResponseEntity<SpuDetail> queryDetailById(@PathVariable("sid")Long spuId){
        return ResponseEntity.ok(goodsService.queryDetailById(spuId));
    }

    /**
     * 根据spu查询下面的所有sku
     * @param spuId
     * @return
     */
    @GetMapping("goods/sku/list")
    public ResponseEntity<List<Sku>> querySkuByIds(@RequestParam("id")Long spuId){
        return ResponseEntity.ok(goodsService.querySkuBySpuId(spuId));
    }

    /**
     * 根据spu的id集合查询所有sku
     * @param ids
     * @return
     */
    @GetMapping("goods/sku/list/ids")
    public ResponseEntity<List<Sku>> querySkuByIds(@RequestParam("ids")List<Long> ids){
        return ResponseEntity.ok(goodsService.querySkuBySpuIds(ids));
    }

    /**
     * 根据spuId删除商品
     * @param spuId
     * @return
     */
    @DeleteMapping("goods")
    public ResponseEntity<Void> deleteGoodsBySpuId(@RequestParam("id")Long spuId){
        goodsService.deleteGoodsBySpuId(spuId);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * 根据spuId上下架商品
     * @param spuId
     * @return
     */
    @PutMapping("goods/shelf")
    public ResponseEntity<Void> shelfBySpuId(@RequestParam("id")Long spuId,@RequestParam("saleable")Boolean seable){
        goodsService.shelfBySpuId(spuId,seable);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * 根据spu的id查询spu
     * @param id
     * @return
     */
    @GetMapping("spu/{id}")
    public ResponseEntity<Spu> querySpuById(@PathVariable("id") Long id){
        Spu spu = this.goodsService.querySpuById(id);

        return ResponseEntity.ok(spu);
    }

    /**
     * 减库存
     * @param carts
     * @return
     */
    @PostMapping("stock/decrease")
    public ResponseEntity<Void> decreaseStock(@RequestBody List<CartDTO> carts){
        goodsService.decreaseStock(carts);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
