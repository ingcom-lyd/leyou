package com.leyou.cart.service;

import com.leyou.auth.entity.UserInfo;
import com.leyou.cart.interceptor.UserInterceptor;
import com.leyou.cart.pojo.Cart;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CartService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "cart:user:id";

    public void addCart(Cart cart) {
        //获取登陆用户
        UserInfo user = UserInterceptor.getUser();
        //key
        String key = KEY_PREFIX + user.getId();
        //hashkey
        String hashKey = cart.getSkuId().toString();

        BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(key);
        //判断当前购物车商品，是否存在
        if (operations.hasKey(hashKey)) {
            //存在，修改数量
            String json = operations.get(hashKey).toString();
            Cart cacheCart = JsonUtils.parse(json, Cart.class);
            cacheCart.setNum(cacheCart.getNum() + cart.getNum());
            //写回redis
            operations.put(hashKey,JsonUtils.serialize(cacheCart));
        }else {
            //不存在，新增
            operations.put(hashKey,JsonUtils.serialize(cart));
        }
    }

    public List<Cart> queryCartList(List<Cart> cart) {
        //获取登陆用户
        UserInfo user = UserInterceptor.getUser();
        //key
        String key = KEY_PREFIX + user.getId();

        if (!redisTemplate.hasKey(key)) {
            //key不存在，返回404
            throw new LyException(ExceptionEnum.CART_NOT_FOUND);
        }

        BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(key);

        List<Cart> carts = operations.values().stream().map(o -> JsonUtils.parse(o.toString(), Cart.class)).collect(Collectors.toList());
        Map<Long, Cart> cartsMap = carts.stream().collect(Collectors.toMap(c -> c.getSkuId(), c -> c));

        //判断当前购物车商品，是否存在
        if (CollectionUtils.isEmpty(cart)) {
            return carts;
        }
        //本地购物车存在
        List<Cart> localCarts = cart;
        for (Cart localCart : localCarts) {
            if (cartsMap.containsKey(localCart.getSkuId())) {
                    Cart c = cartsMap.get(localCart.getSkuId());

                    carts.remove(c);

                    c.setNum(c.getNum() + localCart.getNum());
                    carts.add(c);

                    //写回redis
                    operations.put(c.getSkuId().toString(),JsonUtils.serialize(c));

            } else {
                carts.add(localCart);

                //写回redis
                operations.put(localCart.getSkuId().toString(),JsonUtils.serialize(localCart));
            }
        }

        return carts;
    }

    public List<Cart> queryCartList() {
        //获取登陆用户
        UserInfo user = UserInterceptor.getUser();
        //key
        String key = KEY_PREFIX + user.getId();

        if (!redisTemplate.hasKey(key)) {
            //key不存在，返回404
            throw new LyException(ExceptionEnum.CART_NOT_FOUND);
        }

        BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(key);

        List<Cart> carts = operations.values().stream().map(o -> JsonUtils.parse(o.toString(), Cart.class)).collect(Collectors.toList());
        Map<Long, Cart> cartsMap = carts.stream().collect(Collectors.toMap(c -> c.getSkuId(), c -> c));


        return carts;
    }

    public void updateCartNum(Long skuId, Integer num) {
        UserInfo user = UserInterceptor.getUser();

        String key = KEY_PREFIX + user.getId();

        if (!redisTemplate.hasKey(key)) {
            throw new LyException(ExceptionEnum.CART_NOT_FOUND);
        }

        BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(key);

        if (!operations.hasKey(skuId.toString())) {
            throw new LyException(ExceptionEnum.CART_NOT_FOUND);
        }

        Cart cacheCart = JsonUtils.parse(operations.get(skuId.toString()).toString(), Cart.class);
        cacheCart.setNum(num);

        operations.put(skuId.toString(), JsonUtils.serialize(cacheCart));
    }

    public void deleteCart(Long skuId) {
        UserInfo user = UserInterceptor.getUser();

        String key = KEY_PREFIX + user.getId();

        if (!redisTemplate.hasKey(key)) {
            throw new LyException(ExceptionEnum.CART_NOT_FOUND);
        }

        BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(key);

        operations.delete(skuId.toString());
    }
}
