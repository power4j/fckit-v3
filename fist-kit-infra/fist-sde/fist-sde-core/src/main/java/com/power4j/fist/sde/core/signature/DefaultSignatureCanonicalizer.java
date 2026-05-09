package com.power4j.fist.sde.core.signature;

import com.power4j.fist.sde.core.SecureEnvelope;
import com.power4j.fist.sde.core.SecureExchangeContext;
import com.power4j.fist.sde.core.SecureScope;
import com.power4j.fist.sde.core.exception.SecureEnvelopeException;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;

public class DefaultSignatureCanonicalizer implements SignatureCanonicalizer {

	@Override
	public byte[] canonicalize(SecureEnvelope envelope, SecureExchangeContext context) {
		TreeMap<String, String> fields = new TreeMap<>();
		fields.put("keyRef", required("keyRef", envelope.getKeyRef()));
		fields.put("nonce", required("nonce", envelope.getNonce()));
		fields.put("payload", required("payload", envelope.getPayload()));
		fields.put("scope", required("scope", envelope.getScope()));
		fields.put("timestamp", required("timestamp", envelope.getTimestamp()));
		fields.put("version", required("version", envelope.getVersion()));
		SecureScope.fromValue(envelope.getScope());
		optional(fields, "algorithm", envelope.getAlgorithm());
		optional(fields, "policyId", envelope.getPolicyId());
		String metadata = metadata(envelope.getMetadata());
		optional(fields, "metadata", metadata);
		StringBuilder builder = new StringBuilder();
		for (Map.Entry<String, String> entry : fields.entrySet()) {
			if (builder.length() > 0) {
				builder.append('\n');
			}
			builder.append(entry.getKey()).append('=').append(entry.getValue());
		}
		return builder.toString().getBytes(StandardCharsets.UTF_8);
	}

	private static String required(String name, String value) {
		if (value == null || value.length() == 0) {
			throw new SecureEnvelopeException("required envelope field is blank: " + name);
		}
		rejectLineBreak(name, value);
		return value;
	}

	private static void optional(Map<String, String> fields, String name, String value) {
		if (value != null && value.length() > 0) {
			rejectLineBreak(name, value);
			fields.put(name, value);
		}
	}

	private static void rejectLineBreak(String name, String value) {
		if (value.indexOf('\r') >= 0 || value.indexOf('\n') >= 0) {
			throw new SecureEnvelopeException("envelope field contains line break: " + name);
		}
	}

	private static String metadata(Map<String, String> metadata) {
		if (metadata == null || metadata.isEmpty()) {
			return null;
		}
		TreeMap<String, String> sorted = new TreeMap<>(metadata);
		StringBuilder builder = new StringBuilder();
		for (Map.Entry<String, String> entry : sorted.entrySet()) {
			if (entry.getValue() == null || entry.getValue().length() == 0) {
				continue;
			}
			rejectLineBreak("metadata." + entry.getKey(), entry.getValue());
			if (builder.length() > 0) {
				builder.append('&');
			}
			builder.append(entry.getKey()).append('=').append(entry.getValue());
		}
		return builder.length() == 0 ? null : builder.toString();
	}

}
