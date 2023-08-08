/*
 *  Copyright 2021 ChenJun (power4j@outlook.com & https://github.com/John-Chan)
 *
 *  Licensed under the GNU LESSER GENERAL PUBLIC LICENSE 3.0;
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  <p>
 *  http://www.gnu.org/licenses/lgpl.html
 *  <p>
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.power4j.fist.boot.web.servlet.error;

import com.power4j.coca.kit.common.lang.Result;
import com.power4j.coca.kit.common.text.StringPool;
import com.power4j.fist.boot.common.api.Results;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import jakarta.validation.ValidationException;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 对常见的客户端错误进行处理
 *
 * @author CJ (power4j@outlook.com)
 * @date 2021/10/14
 * @since 1.0
 */
@Slf4j
@Order(1000)
@RestControllerAdvice
public class ClientErrorHandler {

	// ~ 客户端请求参数错误
	// ===================================================================================================

	@ExceptionHandler(MissingServletRequestParameterException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public Result<Object> handleException(MissingServletRequestParameterException e) {
		String hint = String.format("%s(%s)", e.getParameterName(), e.getParameterType());
		log.warn("缺少请求参数:{}", hint);
		return Results.requestParameterError("缺少请求参数", hint);
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public Result<Object> handleException(MethodArgumentTypeMismatchException e) {
		log.warn("参数类型不匹配:{}", e.getMessage());
		return Results.requestParameterError("参数类型不匹配", e.getName());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public Result<Object> handleException(MethodArgumentNotValidException e) {
		Optional<FieldError> error = Optional.ofNullable(e.getBindingResult().getFieldError());
		String errField = error.map(FieldError::getField).orElse(null);
		String msg = error.map(FieldError::getDefaultMessage).orElse("请求参数校验失败");
		log.warn("请求参数校验失败:{}", errField);
		return Results.requestParameterError(msg, errField);
	}

	@ExceptionHandler(BindException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public Result<Object> handleException(BindException e) {
		Optional<FieldError> error = Optional.ofNullable(e.getBindingResult().getFieldError());
		String errField = error.map(FieldError::getField).orElse(null);
		String msg = error.map(FieldError::getDefaultMessage).orElse("请求参数校验失败");
		log.warn("请求参数绑定失败:{}", errField);
		return Results.requestParameterError(msg, errField);
	}

	@ExceptionHandler(ConstraintViolationException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public Result<Object> handleException(ConstraintViolationException e) {
		ConstraintViolation<?> violation = e.getConstraintViolations().iterator().next();
		String path = ((Path) violation.getPropertyPath()).toString();
		log.warn("请求参数校验失败:{}", path);
		return Results.requestParameterError(violation.getMessage(), path);
	}

	@ExceptionHandler(ValidationException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public Result<Object> handleException(ValidationException e) {
		log.warn("请求参数校验失败:{}", e.getMessage());
		return Results.requestParameterError("请求参数校验失败", e.getMessage());
	}

	// ~ 客户端请求错误
	// ===================================================================================================

	@ExceptionHandler(HttpMessageNotReadableException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public Result<Object> handleException(HttpMessageNotReadableException e) {
		log.error("请求消息不能读取:{}", e.getMessage());
		return Results.clientError("请求消息不能读取", e.getMessage());
	}

	@ExceptionHandler(NoHandlerFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public Result<Object> handleException(NoHandlerFoundException e) {
		String hint = String.format("%s %s", e.getHttpMethod(), e.getRequestURL());
		log.error("请求资源不存在:{}", hint);
		return Results.clientError("请求资源不存在", hint);
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	@ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
	public Result<Object> handleException(HttpRequestMethodNotSupportedException e) {
		log.error("请求方法不支持:{}", e.getMessage());
		return Results.clientError("请求方法不支持", e.getMessage());
	}

	@ExceptionHandler(HttpMediaTypeNotSupportedException.class)
	@ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
	public Result<Object> handleException(HttpMediaTypeNotSupportedException e) {
		log.error("请求的媒体类型不支持:{}", e.getMessage());
		String hint = String.format("支持的媒体类型为: %s",
				e.getSupportedMediaTypes().stream().map(MediaType::toString).collect(Collectors.joining(",")));
		return Results.clientError("请求的媒体类型不支持", hint);
	}

	@ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
	@ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
	public Result<Object> handleException(HttpMediaTypeNotAcceptableException e) {
		String required = e.getMessage() + " " + StringUtils.join(StringPool.COMMA, e.getSupportedMediaTypes());
		log.error("无法响应客户端要求的媒体类型:{}", required);
		String hint = String.format("支持的媒体类型为: %s",
				e.getSupportedMediaTypes().stream().map(MediaType::toString).collect(Collectors.joining(",")));
		return Results.clientError("请求的媒体类型不支持", hint);
	}

}
