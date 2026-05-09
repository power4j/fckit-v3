package com.power4j.fist.sde.core.signature;

/**
 * SDE 签名和验签处理器契约。
 */
public interface SignatureHandler {

	/**
	 * 对稳定签名输入生成签名。
	 * @param input 稳定签名输入
	 * @param context 签名上下文
	 * @return 签名字节
	 */
	byte[] sign(byte[] input, SignContext context);

	/**
	 * 验证签名。
	 * @param input 稳定签名输入
	 * @param signature 待验证签名
	 * @param context 验签上下文
	 * @return 签名是否通过验证
	 */
	boolean verify(byte[] input, byte[] signature, SignContext context);

}
