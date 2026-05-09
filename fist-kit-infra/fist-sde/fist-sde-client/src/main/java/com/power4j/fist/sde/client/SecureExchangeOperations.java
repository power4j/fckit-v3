package com.power4j.fist.sde.client;

import org.jspecify.annotations.Nullable;

/**
 * 客户端 SDE envelope 编码和解码操作契约。
 */
public interface SecureExchangeOperations {

	/**
	 * 将请求明文字节编码为 SDE 请求 envelope。
	 * @param body 请求明文字节
	 * @param context 客户端交换上下文；为 {@code null} 时使用默认客户端配置
	 * @return 请求 envelope 字节
	 */
	byte[] encodeRequest(byte[] body, @Nullable SecureExchangeClientContext context);

	/**
	 * 将响应明文字节编码为 SDE 响应 envelope。
	 * @param body 响应明文字节
	 * @param context 客户端交换上下文；为 {@code null} 时使用默认客户端配置
	 * @return 响应 envelope 字节
	 */
	byte[] encodeResponse(byte[] body, @Nullable SecureExchangeClientContext context);

	/**
	 * 将 SDE 响应 envelope 解码为明文字节。
	 * @param envelope 响应 envelope 字节
	 * @param context 客户端交换上下文；为 {@code null} 时使用默认客户端配置
	 * @return 解码后的明文字节
	 */
	byte[] decodeResponse(byte[] envelope, @Nullable SecureExchangeClientContext context);

}
