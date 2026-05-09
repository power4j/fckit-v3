package com.power4j.fist.sde.boot.autoconfigure.client;

import com.power4j.fist.sde.client.SecureExchangeOperations;
import com.power4j.fist.sde.client.webclient.SecureWebClientExchangeFilterFunction;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.reactive.function.client.WebClientCustomizer;
import org.springframework.context.annotation.Bean;

/**
 * SDE WebClient 自动配置。
 * <p>
 * 注册 ExchangeFilterFunction 并通过 {@link WebClientCustomizer} 挂载到 Spring Boot 管理的
 * WebClient.Builder。
 */
@AutoConfiguration(after = SdeClientAutoConfiguration.class)
@ConditionalOnClass(name = "org.springframework.web.reactive.function.client.WebClient")
@ConditionalOnBean(SecureExchangeOperations.class)
public class SdeWebClientAutoConfiguration {

	/**
	 * 提供 WebClient 交换过滤器。
	 * @param operations SDE 客户端操作入口
	 * @return WebClient 过滤器
	 */
	@Bean
	@ConditionalOnMissingBean
	public SecureWebClientExchangeFilterFunction secureWebClientExchangeFilterFunction(
			SecureExchangeOperations operations) {
		return new SecureWebClientExchangeFilterFunction(operations);
	}

	/**
	 * 将 SDE 过滤器挂载到 WebClient.Builder。
	 * @param filterFunction WebClient 过滤器
	 * @return WebClient 定制器
	 */
	@Bean
	@ConditionalOnMissingBean(name = "secureSdeWebClientCustomizer")
	public WebClientCustomizer secureSdeWebClientCustomizer(SecureWebClientExchangeFilterFunction filterFunction) {
		return (builder) -> builder.filter(filterFunction);
	}

}
