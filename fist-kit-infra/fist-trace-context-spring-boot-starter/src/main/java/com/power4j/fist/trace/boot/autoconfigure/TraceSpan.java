package com.power4j.fist.trace.boot.autoconfigure;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 方法级 span 边界。
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.15
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TraceSpan {

	String value();

}
