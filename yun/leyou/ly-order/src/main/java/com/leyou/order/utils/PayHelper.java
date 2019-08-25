package com.leyou.order.utils;

import com.github.wxpay.sdk.WXPay;
import com.github.wxpay.sdk.WXPayConstants;
import com.github.wxpay.sdk.WXPayUtil;
import com.leyou.auth.entity.UserInfo;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.order.config.PayConfig;
import com.leyou.order.enums.OrderStatusEnum;
import com.leyou.order.enums.PayState;
import com.leyou.order.interceptor.UserInterceptor;
import com.leyou.order.mapper.OrderDetailMapper;
import com.leyou.order.mapper.OrderMapper;
import com.leyou.order.mapper.OrderStatusMapper;
import com.leyou.order.pojo.Order;
import com.leyou.order.pojo.OrderDetail;
import com.leyou.order.pojo.OrderStatus;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.github.wxpay.sdk.WXPayConstants.FAIL;
import static com.github.wxpay.sdk.WXPayConstants.SUCCESS;

@Slf4j
@Component
public class PayHelper {
    @Autowired
    private WXPay wxPay;

    @Autowired
    private PayConfig config;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private OrderStatusMapper orderStatusMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "cart:user:id";

    public String createOrder(Long orderId, Long totalPay, String desc) {
        try {
            Map<String, String> data = new HashMap<>();
            //商品描述
            data.put("body", desc);
            //订单号
            data.put("out_trade_no", orderId.toString());
            //金额，单位是分
            data.put("total_fee", totalPay.toString());
            //调用微信支付的终端IP
            data.put("spbill_create_ip", "127.0.0.1");
            //回调地址
            data.put("notify_url", config.getNotifyUrl());
            //交易类型为扫码支付
            data.put("trade_type", "NATIVE");

            //利用wxPay工具，完成下单
            Map<String, String> result = wxPay.unifiedOrder(data);

            //判断通信和业务标示
            isSuccess(result);

            //打印结果
            for (Map.Entry<String, String> entry : result.entrySet()) {
                String key = entry.getKey();
                System.out.println(key + (key.length() >= 0 ? "\t:" : "\t\t:") + entry.getValue());
            }
            System.out.println("-------------------------------------------------");
            //下单成功，获取支付连接
            String url = result.get("code_url");
            return url;


        } catch (Exception e) {
            log.error("[微信下单] 创建预交易订单异常失败", e);
            return null;
        }
    }

    public void isSuccess(Map<String, String> result) {
        //判断通信标示
        String returnCode = result.get("return_code");
        if (FAIL.equals(returnCode)) {
            //通信失败
            log.error("[微信下单] 微信下单通信失败，失败原因：{}", result.get("return_msg"));
            throw new LyException(ExceptionEnum.WX_PAY_ORDER_FAIL);
        }

        //判断业务标示
        String resultCode = result.get("result_code");
        if (FAIL.equals(resultCode)) {
            //通信失败
            log.error("[微信下单] 微信下单通信失败，错误码：{},失败原因：{}", result.get("err_code"), result.get("err_code_des"));
            throw new LyException(ExceptionEnum.WX_PAY_ORDER_FAIL);
        }
    }


    public void isValidSign(Map<String, String> data) {
        try {
            //验证签名
            String sign1 = WXPayUtil.generateSignature(data, config.getKey(), WXPayConstants.SignType.HMACSHA256);
            String sign2 = WXPayUtil.generateSignature(data, config.getKey(), WXPayConstants.SignType.MD5);

            //和传递过来的签名进行比较
            String sign = data.get("sign");

            if (!StringUtils.equals(sign, sign1) && !StringUtils.equals(sign, sign2)) {
                //签名有误，抛出异常
                throw new LyException(ExceptionEnum.INVALID_SIGN_ERROR);
            }

        } catch (Exception e) {
            throw new LyException(ExceptionEnum.INVALID_SIGN_ERROR);
        }
    }

    public PayState queryPayState(Long orderId) {
        try {
            //组织请求参数
            Map<String, String> data = new HashMap<>();
            //订单号
            data.put("out_trade_no", orderId.toString());
            //查询状态
            Map<String, String> result = wxPay.orderQuery(data);
            //校验通信状态
            isSuccess(result);
            //检验签名
            isValidSign(result);
            //校验金额
            String totalFeeStr = result.get("total_fee");
            String tradeNo = result.get("out_trade_no");
            if (StringUtils.isBlank(totalFeeStr) || StringUtils.isBlank(tradeNo)) {
                throw new LyException(ExceptionEnum.INVALID_ORDER_PARAM);
            }
            //获取结果金额
            Long totalFee = Long.valueOf(totalFeeStr);
            Order order = orderMapper.selectByPrimaryKey(orderId);
            if (totalFee != /*order.getActualPay()*/ 1L){
                //金额不符合
                throw new LyException(ExceptionEnum.INVALID_ORDER_PARAM);
            }

            /**
             * SUCCESS—支付成功
             *
             * REFUND—转入退款
             *
             * NOTPAY—未支付
             *
             * CLOSED—已关闭
             *
             * REVOKED—已撤销（刷卡支付）
             *
             * USERPAYING--用户支付中
             *
             * PAYERROR--支付失败(其他原因，如银行返回失败)
             */
            String tradeState = result.get("trade_state");
            if (SUCCESS.equals(tradeState)){
                //支付成功
                //修改订单状态
                OrderStatus orderStatus = orderStatusMapper.selectByPrimaryKey(orderId);
                orderStatus.setStatus(OrderStatusEnum.PAYED.value());
                orderStatus.setPaymentTime(new Date());
                int count = orderStatusMapper.updateByPrimaryKeySelective(orderStatus);
                if (count != 1) {
                    throw new LyException(ExceptionEnum.UPDATE_ORDER_STATUS_ERROR);
                }

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

                //返回成功
                return PayState.SUCCESS;
            }

            if ("NOTPAY".equals(tradeState) || "USERPAYING".equals(tradeState)) {
                return PayState.NOT_PAY;
            }

            return PayState.FAIL;

        } catch (Exception e) {
            return PayState.NOT_PAY;
        }
    }
}
