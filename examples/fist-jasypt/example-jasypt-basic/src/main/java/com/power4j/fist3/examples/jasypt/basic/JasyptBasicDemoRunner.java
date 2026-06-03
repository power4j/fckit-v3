package com.power4j.fist3.examples.jasypt.basic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 国密配置加密示例输出。
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.14
 */
@Component
class JasyptBasicDemoRunner implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(JasyptBasicDemoRunner.class);

	private final String hmacKey;

	JasyptBasicDemoRunner(@Value("${demo.hmac-key}") String hmacKey) {
		this.hmacKey = hmacKey;
	}

	@Override
	public void run(String... args) {
		log.info("demo.hmac-key.length={}", this.hmacKey.length());
	}

}
