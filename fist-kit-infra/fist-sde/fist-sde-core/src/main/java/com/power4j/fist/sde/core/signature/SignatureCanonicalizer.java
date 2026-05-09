package com.power4j.fist.sde.core.signature;

import com.power4j.fist.sde.core.SecureEnvelope;
import com.power4j.fist.sde.core.SecureExchangeContext;

/**
 * SDE 签名输入规范化契约。
 * <p>
 * 实现必须基于 envelope 的逻辑字段名生成稳定字节序列，字段排序和缺失字段处理应保持跨端一致。
 */
public interface SignatureCanonicalizer {

	/**
	 * 生成稳定签名输入。
	 * @param envelope envelope 协议对象
	 * @param context 交换上下文
	 * @return 稳定签名输入字节
	 */
	byte[] canonicalize(SecureEnvelope envelope, SecureExchangeContext context);

}
