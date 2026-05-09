package com.power4j.fist.sde.core.codec;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.power4j.fist.sde.core.SecureEnvelope;
import com.power4j.fist.sde.core.exception.SecureEnvelopeException;

import java.util.LinkedHashMap;
import java.util.Map;

public class JacksonSecureEnvelopeCodec implements SecureEnvelopeCodec {

	private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<Map<String, Object>>() {
	};

	private final ObjectMapper objectMapper;

	public JacksonSecureEnvelopeCodec() {
		this(new ObjectMapper());
	}

	public JacksonSecureEnvelopeCodec(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Override
	public SecureEnvelope decode(byte[] input, SecureEnvelopeContext context) {
		try {
			Map<String, Object> body = this.objectMapper.readValue(input, MAP_TYPE);
			SecureEnvelopeFieldMapping mapping = actual(context).getFieldMapping();
			SecureEnvelope envelope = new SecureEnvelope();
			envelope.setVersion(text(body.get(mapping.getVersionField())));
			envelope.setScope(text(body.get(mapping.getScopeField())));
			envelope.setPayload(text(body.get(mapping.getPayloadField())));
			envelope.setSignature(text(body.get(mapping.getSignatureField())));
			envelope.setTimestamp(text(body.get(mapping.getTimestampField())));
			envelope.setNonce(text(body.get(mapping.getNonceField())));
			envelope.setKeyRef(text(body.get(mapping.getKeyRefField())));
			envelope.setAlgorithm(text(body.get(mapping.getAlgorithmField())));
			envelope.setPolicyId(text(body.get(mapping.getPolicyIdField())));
			Object metadata = body.get(mapping.getMetadataField());
			if (metadata instanceof Map) {
				Map<String, String> converted = new LinkedHashMap<>();
				for (Map.Entry<?, ?> entry : ((Map<?, ?>) metadata).entrySet()) {
					converted.put(String.valueOf(entry.getKey()), text(entry.getValue()));
				}
				envelope.setMetadata(converted);
			}
			return envelope;
		}
		catch (Exception ex) {
			throw new SecureEnvelopeException("failed to decode secure envelope", ex);
		}
	}

	@Override
	public byte[] encodeToBytes(SecureEnvelope envelope, SecureEnvelopeContext context) {
		try {
			return this.objectMapper.writeValueAsBytes(encodeToBody(envelope, context));
		}
		catch (Exception ex) {
			throw new SecureEnvelopeException("failed to encode secure envelope", ex);
		}
	}

	@Override
	public Object encodeToBody(SecureEnvelope envelope, SecureEnvelopeContext context) {
		SecureEnvelopeFieldMapping mapping = actual(context).getFieldMapping();
		Map<String, Object> body = new LinkedHashMap<>();
		put(body, mapping.getVersionField(), envelope.getVersion());
		put(body, mapping.getScopeField(), envelope.getScope());
		put(body, mapping.getPayloadField(), envelope.getPayload());
		put(body, mapping.getSignatureField(), envelope.getSignature());
		put(body, mapping.getTimestampField(), envelope.getTimestamp());
		put(body, mapping.getNonceField(), envelope.getNonce());
		put(body, mapping.getKeyRefField(), envelope.getKeyRef());
		put(body, mapping.getAlgorithmField(), envelope.getAlgorithm());
		put(body, mapping.getPolicyIdField(), envelope.getPolicyId());
		if (envelope.getMetadata() != null && !envelope.getMetadata().isEmpty()) {
			body.put(mapping.getMetadataField(), new LinkedHashMap<>(envelope.getMetadata()));
		}
		return body;
	}

	private static SecureEnvelopeContext actual(SecureEnvelopeContext context) {
		return context == null ? SecureEnvelopeContext.defaults() : context;
	}

	private static void put(Map<String, Object> body, String field, String value) {
		if (field != null && value != null) {
			body.put(field, value);
		}
	}

	private static String text(Object value) {
		return value == null ? null : String.valueOf(value);
	}

}
