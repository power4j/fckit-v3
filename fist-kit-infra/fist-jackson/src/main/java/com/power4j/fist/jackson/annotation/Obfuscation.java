package com.power4j.fist.jackson.annotation;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.power4j.fist.jackson.support.obfuscation.ObfuscatedStringDeserializer;
import com.power4j.fist.jackson.support.obfuscation.ObfuscatedStringSerializer;
import com.power4j.fist.jackson.support.obfuscation.SimpleStringObfuscate;
import com.power4j.fist.jackson.support.obfuscation.StringObfuscate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Obfuscated value
 *
 * @author CJ (power4j@outlook.com)
 * @since 2022.1
 */
@JacksonAnnotationsInside
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.METHOD })
@JsonSerialize(using = ObfuscatedStringSerializer.class)
@JsonDeserialize(using = ObfuscatedStringDeserializer.class)
public @interface Obfuscation {

	Class<? extends StringObfuscate> processor() default SimpleStringObfuscate.class;

}
