package com.power4j.fist.sde.extra.key;

import com.power4j.fist.sde.core.SecureKey;
import com.power4j.fist.sde.core.SecureKeyUsage;
import com.power4j.fist.sde.core.exception.SecureKeyResolveException;
import com.power4j.fist.sde.core.key.SecureKeyContext;
import com.power4j.fist.sde.core.key.SecureKeyResolver;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 基于内存固定映射的密钥解析器。
 * <p>
 * 适合测试、示例或本地固定密钥场景；生产环境需要根据实际密钥管理方案提供 {@link SecureKeyResolver} 实现。
 */
public class StaticSecureKeyResolver implements SecureKeyResolver {

	private final Map<String, Map<SecureKeyUsage, SecureKey>> keys;

	public StaticSecureKeyResolver(Map<String, Map<SecureKeyUsage, SecureKey>> keys) {
		this.keys = new LinkedHashMap<>(keys);
	}

	/**
	 * 创建同一密钥同时用于所有 SDE 密钥用途的解析器。
	 * @param keyRef 密钥引用
	 * @param encoded 原始密钥字节
	 * @return 静态密钥解析器
	 */
	public static StaticSecureKeyResolver symmetric(String keyRef, byte[] encoded) {
		Map<SecureKeyUsage, SecureKey> usages = new EnumMap<>(SecureKeyUsage.class);
		for (SecureKeyUsage usage : SecureKeyUsage.values()) {
			usages.put(usage, new SecureKey(keyRef, "RAW", encoded));
		}
		Map<String, Map<SecureKeyUsage, SecureKey>> keys = new LinkedHashMap<>();
		keys.put(keyRef, usages);
		return new StaticSecureKeyResolver(keys);
	}

	@Override
	public SecureKey resolve(SecureKeyContext context) {
		Map<SecureKeyUsage, SecureKey> usages = this.keys.get(context.getKeyRef());
		if (usages == null || usages.get(context.getUsage()) == null) {
			throw new SecureKeyResolveException(
					"secure key not found: " + context.getKeyRef() + " / " + context.getUsage());
		}
		return usages.get(context.getUsage());
	}

}
