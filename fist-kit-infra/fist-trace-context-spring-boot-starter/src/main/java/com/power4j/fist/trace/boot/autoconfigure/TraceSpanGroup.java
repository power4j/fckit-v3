package com.power4j.fist.trace.boot.autoconfigure;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 类级 span 前缀。
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.15
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface TraceSpanGroup {

	String value();

}
