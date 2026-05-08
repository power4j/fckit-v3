package com.power4j.fist.sde.extra.key;

import com.power4j.fist.sde.core.SecureKey;
import com.power4j.fist.sde.core.SecureKeyUsage;
import com.power4j.fist.sde.core.exception.SecureKeyResolveException;
import com.power4j.fist.sde.core.key.SecureKeyContext;
import com.power4j.fist.sde.core.key.SecureKeyResolver;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class StaticSecureKeyResolver implements SecureKeyResolver {

	private final Map<String, Map<SecureKeyUsage, SecureKey>> keys;

	public StaticSecureKeyResolver(Map<String, Map<SecureKeyUsage, SecureKey>> keys) {
		this.keys = new LinkedHashMap<>(keys);
	}

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
