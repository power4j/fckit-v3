package com.power4j.fist.sde.core.annotation;

import com.power4j.fist.sde.core.SecureInputMode;
import com.power4j.fist.sde.core.SecureResponseMode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface SecureExchange {

	String value() default "";

	SecureInputMode requestBody() default SecureInputMode.INHERIT;

	SecureResponseMode responseBody() default SecureResponseMode.INHERIT;

}
