package com.power4j.fist.sde.core.nonce;

/**
 * SDE nonce 生成器契约。
 */
public interface NonceGenerator {

	/**
	 * 生成本次交换使用的 nonce。
	 * @param context nonce 生成上下文
	 * @return nonce 字符串
	 */
	String generate(NonceContext context);

}
