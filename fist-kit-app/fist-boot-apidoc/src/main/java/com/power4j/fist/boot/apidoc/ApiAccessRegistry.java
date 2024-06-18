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

package com.power4j.fist.boot.apidoc;

import com.power4j.coca.kit.common.text.StringPool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpMethod;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author CJ (power4j@outlook.com)
 * @date 2022/2/21
 * @since 1.0
 */
@Slf4j
@RequiredArgsConstructor
public class ApiAccessRegistry implements InitializingBean {

	private final ApplicationContext applicationContext;

	private final MultiValuedMap<String, HttpMethod> pubAccess = new HashSetValuedHashMap<>(16);

	private final MultiValuedMap<String, HttpMethod> userAccess = new HashSetValuedHashMap<>(16);

	private final MultiValuedMap<String, HttpMethod> interAccess = new HashSetValuedHashMap<>(16);

	@Override
	public void afterPropertiesSet() throws Exception {
		RequestMappingHandlerMapping mapping = applicationContext.getBean(RequestMappingHandlerMapping.class);
		mapping.getHandlerMethods().forEach((k, v) -> {
			Method method = v.getMethod();
			Class<?> clazz = v.getBeanType();
			findMostSpecificAnnotation(method, clazz, ApiTrait.class).ifPresent(apiTrait -> process(apiTrait, k));
		});
	}

	public MultiValuedMap<String, HttpMethod> getPubAccess() {
		return pubAccess;
	}

	public MultiValuedMap<String, HttpMethod> getUserAccess() {
		return userAccess;
	}

	public MultiValuedMap<String, HttpMethod> getInterAccess() {
		return interAccess;
	}

	private void process(ApiTrait apiTrait, RequestMappingInfo info) {
		MultiValuedMap<String, HttpMethod> entry = null;
		switch (apiTrait.access()) {
			case DEFAULT:
				// ignore
				break;
			case INTERNAL:
				entry = interAccess;
				break;
			case USER:
				entry = userAccess;
				break;
			case PUBLIC:
				entry = pubAccess;
				break;
			default:
				throw new IllegalStateException("Unexpected value: " + apiTrait.access());
		}
		if (Objects.nonNull(entry)) {
			Set<String> patterns = info.getPatternValues();
			// @formatter:off
			Set<RequestMethod> requestMethods = info.getMethodsCondition().getMethods();
			if(requestMethods.isEmpty()){
				log.warn("empty request method on :{}",StringUtils.join(patterns, StringPool.COMMA));
				return;
			}
			Set<HttpMethod> methods = info.getMethodsCondition().getMethods()
					.stream().map(m -> HttpMethod.valueOf(m.name()))
					.collect(Collectors.toSet());
			for(String pattern: patterns){
				if(log.isDebugEnabled()){
					log.debug("[access = {}] pattern = {},method = {}",
							apiTrait.access().name(),
							pattern,
							StringUtils.join(methods, StringPool.COMMA));
				}
				if(entry.containsKey(pattern)){
					log.warn("path already exists: {}",pattern);
				}
				entry.putAll(pattern,methods);
			}
			// @formatter:on
		}
	}

	/**
	 * 查找注解,优先级:
	 * <ul>
	 * <li>子类方法上的注解</li>
	 * <li>父类方法上的注解</li>
	 * <li>Class上的注解</li>
	 * </ul>
	 * @param method 方法
	 * @param targetClass 类
	 * @param annotationClass 注解类
	 * @param <A> 注解类型
	 * @return Optional<A>
	 */
	static <A extends Annotation> Optional<A> findMostSpecificAnnotation(Method method, Class<?> targetClass,
			Class<A> annotationClass) {
		// The method may be on an interface, but we need attributes from the target
		// class.
		// If the target class is null, the method will be unchanged.
		Method specificMethod = ClassUtils.getMostSpecificMethod(method, targetClass);
		A annotation = AnnotationUtils.findAnnotation(specificMethod, annotationClass);
		if (annotation != null) {
			if (log.isDebugEnabled()) {
				log.debug("{} found on specific method: {}", annotation, specificMethod);
			}
			return Optional.of(annotation);
		}
		// Check the original (e.g. interface) method
		if (specificMethod != method) {
			annotation = AnnotationUtils.findAnnotation(method, annotationClass);
			if (annotation != null) {
				if (log.isDebugEnabled()) {
					log.debug("{} found on: {}", annotation, method);
				}
				return Optional.of(annotation);
			}
		}
		// Check the class-level (note declaringClass, not targetClass, which may not
		// actually implement the method)
		annotation = AnnotationUtils.findAnnotation(specificMethod.getDeclaringClass(), annotationClass);
		if (annotation != null) {
			if (log.isDebugEnabled()) {
				log.debug("{} found on: {}", annotation, specificMethod.getDeclaringClass().getName());
			}
			return Optional.of(annotation);
		}
		return Optional.empty();
	}

}
