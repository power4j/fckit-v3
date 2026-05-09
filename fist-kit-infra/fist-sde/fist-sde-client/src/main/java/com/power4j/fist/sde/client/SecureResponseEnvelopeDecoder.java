package com.power4j.fist.sde.client;

/**
 * 响应 envelope 解码器门面。
 * <p>
 * 适合非 HTTP 客户端或自定义客户端在收到响应后直接复用 SDE 客户端解码能力。
 */
public class SecureResponseEnvelopeDecoder {

	private final SecureExchangeOperations operations;

	public SecureResponseEnvelopeDecoder(SecureExchangeOperations operations) {
		this.operations = operations;
	}

	/**
	 * 解码响应 envelope。
	 * @param envelope 响应 envelope 字节
	 * @param context 客户端交换上下文
	 * @return 响应明文字节
	 */
	public byte[] decode(byte[] envelope, SecureExchangeClientContext context) {
		return this.operations.decodeResponse(envelope, context);
	}

}
