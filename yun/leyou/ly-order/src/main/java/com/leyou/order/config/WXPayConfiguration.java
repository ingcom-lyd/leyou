package com.leyou.order.config;

import com.github.wxpay.sdk.WXPay;
import com.github.wxpay.sdk.WXPayConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WXPayConfiguration {

    @Autowired
    private PayConfig config;

    @Bean
    public WXPay wxPay(){
        return new WXPay(config, WXPayConstants.SignType.HMACSHA256);
    }
}
