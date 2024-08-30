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

package com.power4j.fist.boot.autoconfigure.data;

import com.power4j.fist.data.crud.validate.DefaultObjectValidator;
import com.power4j.fist.data.crud.validate.ObjectValidator;
import com.power4j.fist.data.crud.validate.ValidateUtil;
import com.power4j.fist.data.tenant.InTenantAspect;
import com.power4j.fist.data.tenant.TenantContextFilter;
import com.power4j.fist.data.tenant.TenantInvokeAspect;
import jakarta.validation.ValidatorFactory;
import org.hibernate.validator.HibernateValidator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.Ordered;

import jakarta.servlet.Filter;
import jakarta.servlet.Servlet;

import static jakarta.servlet.DispatcherType.ASYNC;
import static jakarta.servlet.DispatcherType.REQUEST;

/**
 * @author CJ (power4j@outlook.com)
 * @date 2021/11/1
 * @since 1.0
 */
@AutoConfiguration
@EnableAspectJAutoProxy
public class DataAutoConfiguration {

	@Bean
	public InTenantAspect inTenantAspect() {
		return new InTenantAspect();
	}

	@Bean
	public TenantInvokeAspect tenantInvokeAspect() {
		return new TenantInvokeAspect();
	}

	@ConditionalOnClass({ Servlet.class })
	@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
	@AutoConfigureAfter(name = { "org.springframework.boot.autoconfigure.security.SecurityFilterAutoConfiguration",
			"org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration" })
	static class TenantServletFilterConfiguration {

		private static final String FILTER_NAME = "tenantContextFilter";

		@Bean
		@ConditionalOnMissingBean(name = { "tenantContextFilter" })
		public FilterRegistrationBean<TenantContextFilter> tenantContextFilter() {
			return createFilter(new TenantContextFilter(), FILTER_NAME, Ordered.HIGHEST_PRECEDENCE);
		}

		<T extends Filter> FilterRegistrationBean<T> createFilter(T filter, String filterName, int order) {
			FilterRegistrationBean<T> registration = new FilterRegistrationBean<>(filter);
			registration.setName(filterName);
			registration.setDispatcherTypes(REQUEST, ASYNC);
			registration.setOrder(order);
			return registration;
		}

	}

	@Configuration
	static class ValidationConfiguration {

		@Bean
		@ConditionalOnMissingBean
		public ValidatorFactory validatorFactory() {
			return ValidateUtil.createValidatorFactory();
		}

		@Bean
		@ConditionalOnMissingBean
		public ObjectValidator objectValidator(ValidatorFactory validatorFactory) {
			return new DefaultObjectValidator(validatorFactory.getValidator());
		}

	}

}
