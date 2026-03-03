package com.power4j.fist3.examples.logback.basic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class BasicDemoRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(BasicDemoRunner.class);

    @Override
    public void run(String... args) {
        log.info("用户注册: 手机号=13812345678, 邮箱=user@example.com");
        log.info("登录成功: 13987654321");
        log.warn("异常访问: ip=192.168.1.1, user=admin@corp.com");
    }

}
