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

package com.power4j.fist.boot.autoconfigure.mybatis;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import com.power4j.fist.boot.mybaits.handler.AuditEntityFiller;
import com.power4j.fist.boot.mybaits.tenant.DynamicTenantHandler;
import com.power4j.fist.boot.mybaits.tenant.TenantProperties;
import com.power4j.fist.boot.security.core.UserInfoSupplier;
import com.power4j.fist.mybatis.extension.meta.MetaHandlerCompose;
import com.power4j.fist.mybatis.extension.meta.ValueHandlerResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.lang.Nullable;

import java.util.Objects;

/**
 * @author CJ (power4j@outlook.com)
 * @date 2021/7/1
 * @since 1.0
 */
@Slf4j
@AutoConfiguration
@ConditionalOnClass(MybatisConfiguration.class)
@EnableConfigurationProperties(TenantProperties.class)
@RequiredArgsConstructor
public class MybatisAutoConfiguration implements ApplicationContextAware {

	private final TenantProperties tenantProperties;

	@Nullable
	private ApplicationContext applicationContext;

	@Nullable
	private UserInfoSupplier userInfoSupplier;

	@Autowired(required = false)
	public void setUserInfoSupplier(UserInfoSupplier userInfoSupplier) {
		this.userInfoSupplier = userInfoSupplier;
	}

	@Bean
	@ConditionalOnMissingBean
	public MybatisPlusInterceptor mybatisPlusInterceptor(ObjectProvider<InnerInterceptor> innerInterceptors) {
		MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
		innerInterceptors.orderedStream().forEach(interceptor::addInnerInterceptor);
		return interceptor;
	}

	@Bean
	@ConditionalOnMissingBean
	public ValueHandlerResolver valueHandlerResolver() {
		if (applicationContext == null) {
			throw new IllegalStateException("ApplicationContext is null");
		}
		return new ValueHandlerBeanResolver(applicationContext);
	}

	@Bean
	@ConditionalOnMissingBean
	public MetaHandlerCompose metaHandlerCompose(ValueHandlerResolver valueHandlerResolver) {
		if (Objects.isNull(userInfoSupplier)) {
			userInfoSupplier = UserInfoSupplier.NONE;
		}
		AuditEntityFiller auditEntityFiller = new AuditEntityFiller(userInfoSupplier);
		return new MetaHandlerCompose(valueHandlerResolver, auditEntityFiller);
	}

	@Bean
	@Order(1000)
	@ConditionalOnProperty(prefix = TenantProperties.PROP_PREFIX, name = "enabled", havingValue = "true")
	public TenantLineInnerInterceptor tenantLineInnerInterceptor() {
		TenantLineInnerInterceptor innerInterceptor = new TenantLineInnerInterceptor();
		innerInterceptor
			.setTenantLineHandler(new DynamicTenantHandler(tenantProperties.getColumn(), tenantProperties.getTables()));
		return innerInterceptor;
	}

	@Order(1800)
	@Bean
	OptimisticLockerInnerInterceptor optimisticLockerInnerInterceptor() {
		return new OptimisticLockerInnerInterceptor();
	}

	@Order(2000)
	@Bean
	public PaginationInnerInterceptor paginationInnerInterceptor() {
		return new PaginationInnerInterceptor();
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

}
