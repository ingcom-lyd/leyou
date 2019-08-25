package com.itheima.config;

import com.itheima.pojo.Student;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StudentBean {

    @Bean
    public Student getBean(){
        return new Student("liyande",27);
    }
}
