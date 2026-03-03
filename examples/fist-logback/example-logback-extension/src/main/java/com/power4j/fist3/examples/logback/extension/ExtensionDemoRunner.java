package com.power4j.fist3.examples.logback.extension;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class ExtensionDemoRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ExtensionDemoRunner.class);

    @Override
    public void run(String... args) {
        log.info("实名认证: 姓名=张三, 身份证=110101199001011234");
        log.info("身份证号码: 44010119900101123X");
        log.warn("多字段: 手机=13812345678, 证件=320101198506150012");
    }

}
