package com.power4j.fist.sde.extra.digest;

import com.power4j.fist.sde.core.exception.SecureAlgorithmUnavailableException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * SM3 摘要工具。
 * <p>
 * 运行环境必须提供 {@code SM3} 摘要算法，通常由 Bouncy Castle Provider 提供。
 */
public class Sm3Digest {

	/**
	 * 计算 SM3 摘要。
	 * @param input 输入字节
	 * @return 摘要字节
	 */
	public byte[] digest(byte[] input) {
		try {
			return MessageDigest.getInstance("SM3").digest(input);
		}
		catch (NoSuchAlgorithmException ex) {
			throw new SecureAlgorithmUnavailableException("SM3 algorithm unavailable", ex);
		}
	}

}
