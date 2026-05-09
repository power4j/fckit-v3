package com.power4j.fist.sde.client;

import org.jspecify.annotations.Nullable;

/**
 * 客户端 SDE 处理过程日志扩展点。
 * <p>
 * 实现应自行控制日志级别、脱敏策略和输出格式。生产环境不建议直接输出明文或完整 envelope。
 */
public interface SecureExchangeClientLogger {

	SecureExchangeClientLogger NONE = new SecureExchangeClientLogger() {
		@Override
		public void requestPlain(byte[] body, @Nullable SecureExchangeClientContext context) {
		}

		@Override
		public void requestEnvelope(byte[] envelope, @Nullable SecureExchangeClientContext context) {
		}

		@Override
		public void responseEnvelope(byte[] envelope, @Nullable SecureExchangeClientContext context) {
		}

		@Override
		public void responsePlain(byte[] body, @Nullable SecureExchangeClientContext context) {
		}
	};

	/**
	 * 记录请求明文。
	 * @param body 请求明文字节
	 * @param context 客户端交换上下文
	 */
	void requestPlain(byte[] body, @Nullable SecureExchangeClientContext context);

	/**
	 * 记录请求 envelope。
	 * @param envelope 请求 envelope 字节
	 * @param context 客户端交换上下文
	 */
	void requestEnvelope(byte[] envelope, @Nullable SecureExchangeClientContext context);

	/**
	 * 记录响应 envelope。
	 * @param envelope 响应 envelope 字节
	 * @param context 客户端交换上下文
	 */
	void responseEnvelope(byte[] envelope, @Nullable SecureExchangeClientContext context);

	/**
	 * 记录响应明文。
	 * @param body 响应明文字节
	 * @param context 客户端交换上下文
	 */
	void responsePlain(byte[] body, @Nullable SecureExchangeClientContext context);

}
