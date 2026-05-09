package com.power4j.fist3.examples.sde.web;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = SdeWebExampleApplication.class,
		properties = { "sde.example.run-on-startup=false", "server.port=0" })
class SdeWebExampleApplicationTest {

	@Test
	void contextLoads() {
	}

}
