package com.power4j.fist.sde.core.codec;

import lombok.Builder;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * envelope 编解码上下文。
 * <p>
 * 上下文用于描述字段映射、字符集、媒体类型和 Spring MVC 选中的消息转换器类型。
 */
public class SecureEnvelopeContext {

	private final String envelopeName;

	private final SecureEnvelopeFieldMapping fieldMapping;

	private final Charset charset;

	private final String mediaType;

	private final Type targetBodyType;

	private final Class<?> selectedConverterType;

	@Builder
	public SecureEnvelopeContext(String envelopeName, @Nullable SecureEnvelopeFieldMapping fieldMapping,
			@Nullable Charset charset, @Nullable String mediaType, @Nullable Type targetBodyType,
			@Nullable Class<?> selectedConverterType) {
		this.envelopeName = envelopeName;
		this.fieldMapping = fieldMapping == null ? SecureEnvelopeFieldMapping.defaults() : fieldMapping;
		this.charset = charset == null ? StandardCharsets.UTF_8 : charset;
		this.mediaType = mediaType;
		this.targetBodyType = targetBodyType;
		this.selectedConverterType = selectedConverterType;
	}

	/**
	 * 创建默认 envelope 编解码上下文。
	 * @return 默认上下文
	 */
	public static SecureEnvelopeContext defaults() {
		return SecureEnvelopeContext.builder()
			.envelopeName("default")
			.fieldMapping(SecureEnvelopeFieldMapping.defaults())
			.charset(StandardCharsets.UTF_8)
			.mediaType("application/json")
			.build();
	}

	/**
	 * 基于当前上下文复制出带消息转换器类型的新上下文。
	 * @param selectedConverterType Spring MVC 选中的消息转换器类型
	 * @return 新上下文
	 */
	public SecureEnvelopeContext withSelectedConverterType(@Nullable Class<?> selectedConverterType) {
		return SecureEnvelopeContext.builder()
			.envelopeName(this.envelopeName)
			.fieldMapping(this.fieldMapping)
			.charset(this.charset)
			.mediaType(this.mediaType)
			.targetBodyType(this.targetBodyType)
			.selectedConverterType(selectedConverterType)
			.build();
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

	public @Nullable String getMediaType() {
		return this.mediaType;
	}

	public @Nullable Type getTargetBodyType() {
		return this.targetBodyType;
	}

	public @Nullable Class<?> getSelectedConverterType() {
		return this.selectedConverterType;
	}

}
