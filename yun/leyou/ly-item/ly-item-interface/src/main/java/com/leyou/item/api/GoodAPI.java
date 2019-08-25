package com.leyou.item.api;

import com.leyou.common.dto.CartDTO;
import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuDetail;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface GoodAPI {
    @GetMapping("spu/page")
    PageResult<Spu> querySpuByPage(
            @RequestParam(value = "page",defaultValue = "1")Integer page,
            @RequestParam(value = "rows",defaultValue = "5")Integer rows,
            @RequestParam(value = "saleable",required = false)Boolean saleable,
            @RequestParam(value = "key",required = false)String key
    );

    /**
     * 商品新增
     * @param spu
     * @return
     */
    @PostMapping("goods")
    Void saveGoods(@RequestBody Spu spu);

    /**
     * 商品更新
     * @param spu
     * @return
     */
    @PutMapping("goods")
    Void updateGoods(@RequestBody Spu spu);

    /**
     * 根据spu的id查询spuDetail
     * @param spuId
     * @return
     */
    @GetMapping("goods/spu/detail/{sid}")
    SpuDetail queryDetailById(@PathVariable("sid")Long spuId);

    /**
     * 根据spu查询下面的所有sku
     * @param spuId
     * @return
     */
    @GetMapping("goods/sku/list")
    List<Sku> querySkuBySpuId(@RequestParam("id")Long spuId);

    /**
     * 根据spuId删除商品
     * @param spuId
     * @return
     */
    @DeleteMapping("goods")
    Void deleteGoodsBySpuId(@RequestParam("id")Long spuId);

    /**d
     * 根据spuId上下架商品
     * @param spuId
     * @return
     */
    @PutMapping("goods/shelf")
    Void shelfBySpuId(@RequestParam("id")Long spuId,@RequestParam("saleable")Boolean seable);

    /**
     * 根据spu的id查询spu
     * @param id
     * @return
     */
    @GetMapping("spu/{id}")
    public Spu querySpuById(@PathVariable("id") Long id);

    /**
     * 根据spu的id集合查询所有sku
     * @param ids
     * @return
     */
    @GetMapping("goods/sku/list/ids")
    List<Sku> querySkuByIds(@RequestParam("ids")List<Long> ids);

    /**
     * 减库存
     * @param carts
     * @return
     */
    @PostMapping("stock/decrease")
    Void decreaseStock(@RequestBody List<CartDTO> carts);
}
