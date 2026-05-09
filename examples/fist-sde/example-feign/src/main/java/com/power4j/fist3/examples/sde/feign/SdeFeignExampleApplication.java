package com.power4j.fist3.examples.sde.feign;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients(clients = RemoteOrderClient.class)
@SpringBootApplication
public class SdeFeignExampleApplication {

	public static void main(String[] args) {
		SpringApplication.run(SdeFeignExampleApplication.class, args);
	}

}
