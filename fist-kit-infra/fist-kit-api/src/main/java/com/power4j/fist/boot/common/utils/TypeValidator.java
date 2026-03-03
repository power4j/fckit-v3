package com.power4j.fist.boot.common.utils;

import lombok.Getter;
import org.apache.commons.lang3.ClassUtils;
import org.jspecify.annotations.Nullable;

/**
 * @author CJ (power4j@outlook.com)
 * @since 1.0
 */
@Getter
public class TypeValidator {

	private final Class<?> target;

	TypeValidator(Class<?> target) {
		this.target = target;
	}

	public static TypeValidator of(Class<?> target) {
		return new TypeValidator(target);
	}

	/**
	 * 类型转换检查
	 * @param obj 被检查的对象
	 * @return 返回是否可以进行类型转换
	 * @see TypeValidator#castCheck(Object, Class)
	 */
	public boolean castCheck(@Nullable Object obj) {
		return castCheck(obj, target);
	}

	/**
	 * 类型转换检查 <pre>
	 *     castCheck("str",CharSequence.class); // true
	 *     castCheck("str",Object.class);       // true
	 *     castCheck(0,CharSequence.class);     // false
	 *     castCheck(0,Integer.class);          // false
	 *     castCheck(null,Integer.class);       // true
	 * </pre>
	 * @param obj 被检查的对象
	 * @param cls 目标类型
	 * @return true: obj可以转换为cls类型
	 */
	public static boolean castCheck(@Nullable Object obj, Class<?> cls) {
		if (null == obj) {
			return true;
		}
		return ClassUtils.isAssignable(obj.getClass(), cls);
	}

}
