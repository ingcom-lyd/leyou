package com.leyou.auth.service;

import com.leyou.auth.client.UserClient;
import com.leyou.auth.config.JwtProperties;
import com.leyou.auth.entity.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.user.pojo.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import java.security.PrivateKey;

@Slf4j
@EnableConfigurationProperties(JwtProperties.class)
@Service
public class AuthService {

    @Autowired
    private UserClient userClient;
    @Autowired
    private JwtProperties jwtProperties;

    public String login(String username, String password) {
        try {
            //校验用户名和密码
            User user = userClient.queryUserByUsernameAndPassword(username, password);
            //判断
            if (user == null) {
                throw new LyException(ExceptionEnum.INVALID_USERNAME_PASSWORD);
            }
            //生成token
            int expire = jwtProperties.getExpire();
            PrivateKey privateKey = jwtProperties.getPrivateKey();

            String token = JwtUtils.generateToken(new UserInfo(user.getId(),username),privateKey,expire);

            return token;
        }catch (Exception e){
            log.error("[授权中心] 用户名或密码出错，用户名称：{}",username,e);
            throw new LyException(ExceptionEnum.INVALID_USERNAME_PASSWORD);
        }
    }
}
