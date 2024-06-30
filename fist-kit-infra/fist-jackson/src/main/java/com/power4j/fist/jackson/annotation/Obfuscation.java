package com.power4j.fist.jackson.annotation;

import com.fasterxml.jackson.annotation.JacksonAnnotation;
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
@JacksonAnnotation
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface Obfuscation {

	Class<? extends StringObfuscate> processor() default StringObfuscate.class;

}
