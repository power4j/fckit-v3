package com.power4j.fist.sde.client;

/**
 * 请求 envelope 编码器门面。
 * <p>
 * 适合非 HTTP 客户端或自定义客户端在发送请求前直接复用 SDE 客户端编码能力。
 */
public class SecureRequestEnvelopeEncoder {

	private final SecureExchangeOperations operations;

	public SecureRequestEnvelopeEncoder(SecureExchangeOperations operations) {
		this.operations = operations;
	}

	/**
	 * 编码请求明文字节。
	 * @param body 请求明文字节
	 * @param context 客户端交换上下文
	 * @return 请求 envelope 字节
	 */
	public byte[] encode(byte[] body, SecureExchangeClientContext context) {
		return this.operations.encodeRequest(body, context);
	}

}
