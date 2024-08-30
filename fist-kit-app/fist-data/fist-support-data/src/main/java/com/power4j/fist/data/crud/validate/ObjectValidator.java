package com.power4j.fist.data.crud.validate;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;

import java.util.Set;
import java.util.function.Function;

/**
 * @author CJ (power4j@outlook.com)
 * @since 1.0
 */
public interface ObjectValidator {

	/**
	 * 校验对象的约束条件(快速失败)
	 * @param object 被校验的对象
	 * @param groups 校验组
	 * @param <T> 被校验的对象
	 * @return 违例的约束,如果没有违例情况返回空的Set
	 */
	<T> Set<ConstraintViolation<T>> check(T object, Class<?>... groups);

	/**
	 * 校验对象
	 * @param exceptionTranslator 自定义异常转换
	 * @param object 待校验对象
	 * @param groups 待校验的组
	 * @param <T> 被校验的对象
	 */
	<T, E extends Exception> void validate(Function<Set<ConstraintViolation<T>>, E> exceptionTranslator, T object,
			Class<?>... groups) throws E;

	/**
	 * 校验对象
	 * @param object 待校验对象
	 * @param groups 待校验的组
	 * @throws ConstraintViolationException 校验失败
	 */
	<T> void validate(T object, Class<?>... groups) throws ConstraintViolationException;

	/**
	 * 校验对象的约束条件(完整校验)
	 * @param object 待校验对象
	 * @param groups 待校验的组
	 * @throws ValidationException 校验失败
	 */
	<T> void validateAll(T object, Class<?>... groups) throws ValidationException;

}
