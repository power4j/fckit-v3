package com.power4j.fist.sde.core;

/**
 * 表示解析密钥时的具体用途。
 */
public enum SecureKeyUsage {

	/**
	 * 生成签名。
	 */
	SIGN,

	/**
	 * 验证签名。
	 */
	VERIFY,

	/**
	 * 加密明文 payload。
	 */
	ENCRYPT,

	/**
	 * 解密密文 payload。
	 */
	DECRYPT

}
