package com.power4j.fist.sde.core.annotation;

import com.power4j.fist.sde.core.SecureInputMode;
import com.power4j.fist.sde.core.SecureResponseMode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记控制器类或方法需要按 SDE body 规则处理请求体和响应体。
 * <p>
 * 该注解是面向 MVC 端点的便捷契约，适合只需要声明 body 加密模式的场景。方法级声明优先于类级声明。
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface SecureBody {

	/**
	 * 指定使用的策略 ID。
	 * @return 策略 ID；空字符串表示使用默认策略
	 */
	String value() default "";

	/**
	 * 指定请求体处理模式。
	 * @return 请求体输入模式
	 */
	SecureInputMode request() default SecureInputMode.INHERIT;

	/**
	 * 指定响应体处理模式。
	 * @return 响应体输出模式
	 */
	SecureResponseMode response() default SecureResponseMode.INHERIT;

}
