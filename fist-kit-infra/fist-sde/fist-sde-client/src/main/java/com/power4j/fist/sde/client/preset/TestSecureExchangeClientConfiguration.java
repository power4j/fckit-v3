package com.power4j.fist.sde.client.preset;

import com.power4j.fist.sde.core.key.SecureKeyResolver;
import com.power4j.fist.sde.core.replay.ReplayGuard;
import com.power4j.fist.sde.extra.key.StaticSecureKeyResolver;
import com.power4j.fist.sde.extra.replay.InMemoryReplayGuard;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * 测试级 SDE 客户端组件配置。
 * <p>
 * 该配置注册静态密钥解析器和内存重放校验器，适合示例、测试和本地验证，不建议直接用于生产应用。
 */
@Configuration(proxyBeanMethods = false)
public class TestSecureExchangeClientConfiguration {

	/**
	 * 提供测试级静态密钥解析器。
	 * @param keyRef 测试密钥引用
	 * @param secret 测试密钥明文
	 * @return 静态密钥解析器
	 */
	@Bean("testStaticSecureKeyResolver")
	@ConditionalOnMissingBean(name = "testStaticSecureKeyResolver")
	public SecureKeyResolver testStaticSecureKeyResolver(@Value("${fist.sde.client.test.key-ref:test}") String keyRef,
			@Value("${fist.sde.client.test.secret:0123456789abcdef}") String secret) {
		return StaticSecureKeyResolver.symmetric(keyRef, secret.getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * 提供测试级内存重放校验器。
	 * @param replayWindow ISO-8601 Duration 格式的重放窗口
	 * @return 内存重放校验器
	 */
	@Bean("testInMemoryReplayGuard")
	@ConditionalOnMissingBean(name = "testInMemoryReplayGuard")
	public ReplayGuard testInMemoryReplayGuard(
			@Value("${fist.sde.client.test.replay-window:PT5M}") String replayWindow) {
		return new InMemoryReplayGuard(Duration.parse(replayWindow));
	}

}
