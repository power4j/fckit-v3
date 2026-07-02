package com.power4j.fist.trace.boot.autoconfigure;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 方法级 span 边界。
 * <p>
 * 与 {@link TraceSpanGroup} 同时使用时，普通值会拼接 group 前缀；以 {@code .} 开头的值视为绝对 span 名。
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.15
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TraceSpan {

	/**
	 * span 名称。以 {@code .} 开头时不拼接类级 {@link TraceSpanGroup} 前缀。
	 * @return span 名称
	 */
	String value();

}
