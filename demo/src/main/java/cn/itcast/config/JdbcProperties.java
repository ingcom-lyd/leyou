package cn.itcast.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

//@ConfigurationProperties(prefix = "jdbc")
@Data
public class JdbcProperties {

    private String driverClassName;
    private String url;
    private String username;
    private String password;
}
