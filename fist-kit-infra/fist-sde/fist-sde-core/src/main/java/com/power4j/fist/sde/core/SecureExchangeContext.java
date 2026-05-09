package com.power4j.fist.sde.core;

import lombok.Builder;
import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * SDE 处理过程中的交换上下文。
 * <p>
 * 上下文在加解密、签名、密钥解析、nonce 和重放校验之间传递本次交换的方向、范围、策略和请求关联信息。
 */
public class SecureExchangeContext {

	private final SecureScope scope;

	private final SecureDirection direction;

	private final String policyId;

	private final String algorithm;

	private final String keyRef;

	private final Duration timestampWindow;

	private final Map<String, Object> requestContext;

	@Builder
	public SecureExchangeContext(SecureScope scope, SecureDirection direction, @Nullable String policyId,
			@Nullable String algorithm, @Nullable String keyRef, @Nullable Duration timestampWindow,
			@Nullable Map<String, Object> requestContext) {
		this.scope = scope;
		this.direction = direction;
		this.policyId = policyId;
		this.algorithm = algorithm;
		this.keyRef = keyRef;
		this.timestampWindow = timestampWindow;
		this.requestContext = requestContext == null ? Collections.<String, Object>emptyMap()
				: Collections.unmodifiableMap(new LinkedHashMap<>(requestContext));
	}

	/**
	 * 创建入站交换上下文。
	 * @param scope 受保护的数据范围
	 * @return 入站上下文
	 */
	public static SecureExchangeContext inbound(SecureScope scope) {
		return SecureExchangeContext.builder()
			.scope(scope)
			.direction(SecureDirection.INBOUND)
			.timestampWindow(Duration.ofMinutes(5))
			.build();
	}

	/**
	 * 创建出站交换上下文。
	 * @param scope 受保护的数据范围
	 * @return 出站上下文
	 */
	public static SecureExchangeContext outbound(SecureScope scope) {
		return SecureExchangeContext.builder()
			.scope(scope)
			.direction(SecureDirection.OUTBOUND)
			.timestampWindow(Duration.ofMinutes(5))
			.build();
	}

	/**
	 * 基于当前上下文复制出带策略信息的新上下文。
	 * @param policyId 策略 ID
	 * @param algorithm 算法标识
	 * @param keyRef 密钥引用
	 * @param timestampWindow 时间戳窗口
	 * @return 带策略信息的新上下文
	 */
	public SecureExchangeContext withPolicy(@Nullable String policyId, @Nullable String algorithm,
			@Nullable String keyRef, @Nullable Duration timestampWindow) {
		return SecureExchangeContext.builder()
			.scope(this.scope)
			.direction(this.direction)
			.policyId(policyId)
			.algorithm(algorithm)
			.keyRef(keyRef)
			.timestampWindow(timestampWindow == null ? this.timestampWindow : timestampWindow)
			.requestContext(this.requestContext)
			.build();
	}

	public SecureScope getScope() {
		return this.scope;
	}

	public SecureDirection getDirection() {
		return this.direction;
	}

	public @Nullable String getPolicyId() {
		return this.policyId;
	}

	public @Nullable String getAlgorithm() {
		return this.algorithm;
	}

	public @Nullable String getKeyRef() {
		return this.keyRef;
	}

	public @Nullable Duration getTimestampWindow() {
		return this.timestampWindow;
	}

	public Map<String, Object> getRequestContext() {
		return this.requestContext;
	}

}
