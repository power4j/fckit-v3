package com.power4j.fist.data.crud.validate;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author CJ (power4j@outlook.com)
 * @since 1.0
 */
@RequiredArgsConstructor
public class DefaultObjectValidator implements ObjectValidator {

	private final Validator validator;

	@Override
	public <T> Set<ConstraintViolation<T>> check(T object, Class<?>... groups) {
		return validator.validate(object, groups);
	}

	@Override
	public <T, E extends Exception> void validate(Function<Set<ConstraintViolation<T>>, E> exceptionTranslator,
			T object, Class<?>... groups) throws E {
		Set<ConstraintViolation<T>> constraintViolations = check(object, groups);
		if (!constraintViolations.isEmpty()) {
			throw exceptionTranslator.apply(constraintViolations);
		}
	}

	@Override
	public <T> void validate(T object, Class<?>... groups) throws ConstraintViolationException {
		Set<ConstraintViolation<T>> constraintViolations = check(object, groups);
		if (!constraintViolations.isEmpty()) {
			throw new ConstraintViolationException(constraintViolations);
		}
	}

	@Override
	public <T> void validateAll(T object, Class<?>... groups) throws ValidationException {
		validate((constraintViolations) -> new ValidationException(
				constraintViolations.stream().map(ConstraintViolation::getMessage).collect(Collectors.joining(","))),
				object, groups);
	}

}
