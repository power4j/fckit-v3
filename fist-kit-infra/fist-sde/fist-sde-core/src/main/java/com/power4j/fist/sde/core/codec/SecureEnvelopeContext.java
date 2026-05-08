package com.power4j.fist.sde.core.codec;

import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class SecureEnvelopeContext {

	private final String envelopeName;

	private final SecureEnvelopeFieldMapping fieldMapping;

	private final Charset charset;

	private final String mediaType;

	private final Type targetBodyType;

	private final Class<?> selectedConverterType;

	public SecureEnvelopeContext(String envelopeName, SecureEnvelopeFieldMapping fieldMapping, Charset charset,
			String mediaType, Type targetBodyType, Class<?> selectedConverterType) {
		this.envelopeName = envelopeName;
		this.fieldMapping = fieldMapping == null ? SecureEnvelopeFieldMapping.defaults() : fieldMapping;
		this.charset = charset == null ? StandardCharsets.UTF_8 : charset;
		this.mediaType = mediaType;
		this.targetBodyType = targetBodyType;
		this.selectedConverterType = selectedConverterType;
	}

	public static SecureEnvelopeContext defaults() {
		return new SecureEnvelopeContext("default", SecureEnvelopeFieldMapping.defaults(), StandardCharsets.UTF_8,
				"application/json", null, null);
	}

	public SecureEnvelopeContext withSelectedConverterType(Class<?> selectedConverterType) {
		return new SecureEnvelopeContext(this.envelopeName, this.fieldMapping, this.charset, this.mediaType,
				this.targetBodyType, selectedConverterType);
	}

	public String getEnvelopeName() {
		return this.envelopeName;
	}

	public SecureEnvelopeFieldMapping getFieldMapping() {
		return this.fieldMapping;
	}

	public Charset getCharset() {
		return this.charset;
	}

	public String getMediaType() {
		return this.mediaType;
	}

	public Type getTargetBodyType() {
		return this.targetBodyType;
	}

	public Class<?> getSelectedConverterType() {
		return this.selectedConverterType;
	}

}
