package com.leyou.order.service;

import com.leyou.auth.entity.UserInfo;
import com.leyou.common.dto.CartDTO;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.IdWorker;
import com.leyou.item.pojo.Sku;
import com.leyou.order.client.AddressClient;
import com.leyou.order.client.GoodsClient;
import com.leyou.order.dto.AddressDTO;
import com.leyou.order.dto.OrderDTO;
import com.leyou.order.enums.OrderStatusEnum;
import com.leyou.order.enums.PayState;
import com.leyou.order.interceptor.UserInterceptor;
import com.leyou.order.mapper.OrderDetailMapper;
import com.leyou.order.mapper.OrderMapper;
import com.leyou.order.mapper.OrderStatusMapper;
import com.leyou.order.pojo.Order;
import com.leyou.order.pojo.OrderDetail;
import com.leyou.order.pojo.OrderStatus;
import com.leyou.order.utils.PayHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private OrderStatusMapper orderStatusMapper;
    @Autowired
    private IdWorker idWorker;
    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private PayHelper payHelper;
    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "cart:user:id";

    @Transactional
    public Long createOrder(OrderDTO orderDTO) {
        //1 新增订单
        Order order = new Order();
        //1.1 订单编号，基本信息
        Long orderId = idWorker.nextId();
        order.setOrderId(orderId);
        order.setCreateTime(new Date());

        //1.2 用户信息
        UserInfo user = UserInterceptor.getUser();
        order.setUserId(user.getId());
        order.setBuyerNick(user.getUsername());
        order.setBuyerRate(false);

        //1.3 收货人地址
        AddressDTO addressDTO = AddressClient.findById(orderDTO.getAddressId());

        order.setReceiver(addressDTO.getName());
        order.setReceiverAddress(addressDTO.getAddress());
        order.setReceiverState(addressDTO.getState());
        order.setReceiverCity(addressDTO.getCity());
        order.setReceiverDistrict(addressDTO.getDistrict());
        order.setReceiverMobile(addressDTO.getPhone());
        order.setReceiverZip(addressDTO.getZipCode());

        //1.4 金额
        //把CartDTO转为一个map
        Map<Long, Integer> numMap = orderDTO.getCarts().stream()
                .collect(Collectors.toMap(CartDTO::getSkuId, CartDTO::getNum));
        //获取所有sku的id
        Set<Long> ids = numMap.keySet();
        //根据id查询sku
        List<Sku> skus = goodsClient.querySkuByIds(new ArrayList<>(ids));

        //准备orderDetail集合
        List<OrderDetail> details = new ArrayList<>();

        Long totalPay = 0L;
        for (Sku sku : skus) {
            //计算商品总金额
            totalPay += sku.getPrice() * numMap.get(sku.getId());
            //封装orderDeatail
            OrderDetail detail = new OrderDetail();
            detail.setImage(StringUtils.substringBefore(sku.getImages(),","));
            detail.setNum(numMap.get(sku.getId()));
            detail.setOrderId(orderId);
            detail.setOwnSpec(sku.getOwnSpec());
            detail.setPrice(sku.getPrice());
            detail.setSkuId(sku.getId());
            detail.setTitle(sku.getTitle());
            details.add(detail);
        }

        order.setTotalPay(totalPay);
        //实付金额 = 总金额 + 邮费 - 优惠
        order.setActualPay(totalPay + order.getPostFee() - 0);
        order.setPaymentType(orderDTO.getPaymentType());

        //1.5 把order写入数据库
        int count = orderMapper.insertSelective(order);
        if (count != 1) {
            log.error("[创建订单] 创建订单失败，orderId:{}",orderId);
            throw new LyException(ExceptionEnum.CREATE_ORDER_ERROR);
        }

        //2 新增订单详情
        count = orderDetailMapper.insertList(details);
        if (count != details.size()) {
            log.error("[创建订单] 创建订单失败，orderId:{}",orderId);
            throw new LyException(ExceptionEnum.CREATE_ORDER_ERROR);
        }

        //3 新增订单状态
        OrderStatus orderStatus = new OrderStatus();
        orderStatus.setCreateTime(new Date());
        orderStatus.setOrderId(orderId);
        orderStatus.setStatus(OrderStatusEnum.UN_PAY.value());

        count = orderStatusMapper.insertSelective(orderStatus);
        if (count != 1) {
            log.error("[创建订单] 创建订单失败，orderId:{}",orderId);
            throw new LyException(ExceptionEnum.CREATE_ORDER_ERROR);
        }

        //4 减库存
        List<CartDTO> carts = orderDTO.getCarts();
        goodsClient.decreaseStock(carts);
        goodsClient.decreaseStock(carts);

        System.out.println("orderId = " + orderId);
        return orderId;
    }

    public Order queryOrderById(Long id) {
        Order order = orderMapper.selectByPrimaryKey(id);
        if (order == null) {
            throw new LyException(ExceptionEnum.ORDER_NOT_FOUND);
        }

        OrderDetail detail = new OrderDetail();
        detail.setOrderId(id);
        List<OrderDetail> orderDetailList = orderDetailMapper.select(detail);
        if (CollectionUtils.isEmpty(orderDetailList)) {
            throw new LyException(ExceptionEnum.ORDER_DETAIL_NOT_FOUND);

        }

        OrderStatus orderStatus = orderStatusMapper.selectByPrimaryKey(id);
        if (orderStatus == null) {
            throw new LyException(ExceptionEnum.ORDER_STATUS_NOT_FOUND);
        }

        order.setOrderDetails(orderDetailList);
        order.setOrderStatus(orderStatus);

        return order;
    }

    public String createPayUrl(Long orderId) {
        //查询订单
        Order order = queryOrderById(orderId);
        //判断订单状态
        Integer status = order.getOrderStatus().getStatus();
        if (status != OrderStatusEnum.UN_PAY.value()) {
            //订单状态异常
            throw new LyException(ExceptionEnum.ORDER_STATUS_ERROR);
        }
        //支付金额
//        Long actualPay = order.getActualPay();
        Long actualPay = 1L;
        //商品描述
        OrderDetail detail = order.getOrderDetails().get(0);
        String desc = detail.getTitle();

        String url = payHelper.createOrder(orderId, actualPay, desc);
        return url;
    }

    public void handleNotify(Map<String, String> result) {
        //数据校验
        payHelper.isSuccess(result);

        //校验签名
        payHelper.isValidSign(result);

        //校验金额
        String totalFeeStr = result.get("total_fee");
        String tradeNo = result.get("out_trade_no");
        if (StringUtils.isBlank(totalFeeStr) || StringUtils.isBlank(tradeNo)) {
            throw new LyException(ExceptionEnum.INVALID_ORDER_PARAM);
        }
        //获取结果金额
        Long totalFee = Long.valueOf(totalFeeStr);
        //获取订单号
        Long orderId = Long.valueOf(tradeNo);
        Order order = queryOrderById(orderId);
        if (totalFee != /*order.getActualPay()*/ 1L){
            //金额不符合
            throw new LyException(ExceptionEnum.INVALID_ORDER_PARAM);
        }

        //修改订单状态
        OrderStatus orderStatus = order.getOrderStatus();
        orderStatus.setStatus(OrderStatusEnum.PAYED.value());
        orderStatus.setPaymentTime(new Date());
        int count = orderStatusMapper.updateByPrimaryKeySelective(orderStatus);
        if (count != 1) {
            throw new LyException(ExceptionEnum.UPDATE_ORDER_STATUS_ERROR);
        }

        log.info("[支付回调] 订单支付成功，订单编号：{}",orderId);
    }

    public PayState queryOrderState(Long orderId) {
        //查询订单状态
        OrderStatus orderStatus = orderStatusMapper.selectByPrimaryKey(orderId);
        //判断是否支付
        if (orderStatus.getStatus() != OrderStatusEnum.UN_PAY.value()) {
            //如果已支付，那就是真的支付了

            //删除redis的购物车数据
            UserInfo user = UserInterceptor.getUser();

            String key = KEY_PREFIX + user.getId();

            if (!redisTemplate.hasKey(key)) {
                throw new LyException(ExceptionEnum.CART_NOT_FOUND);
            }

            BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(key);

            //获取商品id
            OrderDetail detail = new OrderDetail();
            detail.setOrderId(orderId);
            List<OrderDetail> details = orderDetailMapper.select(detail);
            if (CollectionUtils.isEmpty(details)) {
                throw new LyException(ExceptionEnum.ORDER_DETAIL_NOT_FOUND);

            }
            List<Long> ids = details.stream().map(c -> c.getSkuId()).collect(Collectors.toList());
            for (Long id : ids) {
                if (!operations.hasKey(id.toString())) {
                    throw new LyException(ExceptionEnum.CART_NOT_FOUND);
                }

                operations.delete(id.toString());
            }

            return PayState.SUCCESS;
        }
        //如果未支付，但其实不一定未支付,必须去微信查询支付状态
        return payHelper.queryPayState(orderId);
    }
}
