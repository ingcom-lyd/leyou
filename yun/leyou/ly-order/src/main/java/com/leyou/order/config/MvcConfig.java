package com.leyou.order.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.leyou.order.interceptor.UserInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.List;

@Configuration
@EnableWebMvc
@EnableConfigurationProperties(JwtProperties.class)
public class MvcConfig implements WebMvcConfigurer {

    @Autowired
    private JwtProperties jwtProperties;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new UserInterceptor(jwtProperties)).addPathPatterns("/order/**");
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        //定义json转换器
        MappingJackson2HttpMessageConverter jackson2HttpMessageConverter =
                new MappingJackson2HttpMessageConverter();

        //定义对象映射器
        ObjectMapper objectMapper = new ObjectMapper();
        //定义对象模型
        SimpleModule simpleModule = new SimpleModule();
        //添加转换关系
        simpleModule.addSerializer(BigInteger.class, ToStringSerializer.instance);
        simpleModule.addSerializer(Long.class, ToStringSerializer.instance);
        simpleModule.addSerializer(Long.TYPE, ToStringSerializer.instance);
        //将对象模型添加到对象映射器中
        objectMapper.registerModule(simpleModule);
        //将对象映射器添加到json映射器中
        jackson2HttpMessageConverter.setObjectMapper(objectMapper);


        MappingJackson2XmlHttpMessageConverter jackson2XmlHttpMessageConverter = new MappingJackson2XmlHttpMessageConverter();
        XmlMapper xmlMapper = new XmlMapper();
        jackson2XmlHttpMessageConverter.setObjectMapper(xmlMapper);
        converters.add(jackson2XmlHttpMessageConverter);


        //在转换器列表中添加自定义的json转换器
        converters.add(jackson2HttpMessageConverter);
        //添加utf-8的默认String转换器
        converters.add(new StringHttpMessageConverter(Charset.forName("UTF-8")));
    }
}
