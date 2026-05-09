package com.power4j.fist.sde.core.crypto;

/**
 * SDE payload 加解密处理器契约。
 */
public interface CryptoHandler {

	/**
	 * 加密明文 payload。
	 * @param plain 明文字节
	 * @param context 加密上下文
	 * @return 密文字节
	 */
	byte[] encrypt(byte[] plain, CryptoContext context);

	/**
	 * 解密密文 payload。
	 * @param cipher 密文字节
	 * @param context 解密上下文
	 * @return 明文字节
	 */
	byte[] decrypt(byte[] cipher, CryptoContext context);

}
