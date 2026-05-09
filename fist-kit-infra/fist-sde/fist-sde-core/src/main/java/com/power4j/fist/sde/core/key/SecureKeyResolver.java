package com.power4j.fist.sde.core.key;

import com.power4j.fist.sde.core.SecureKey;

/**
 * SDE 密钥解析器契约。
 * <p>
 * 实现根据 keyRef、密钥用途和交换上下文返回实际算法处理所需的密钥材料。
 */
public interface SecureKeyResolver {

	/**
	 * 解析密钥。
	 * @param context 密钥解析上下文
	 * @return 密钥材料
	 */
	SecureKey resolve(SecureKeyContext context);

}
