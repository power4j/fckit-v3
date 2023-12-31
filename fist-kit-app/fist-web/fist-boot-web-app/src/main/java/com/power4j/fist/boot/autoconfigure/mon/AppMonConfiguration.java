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

package com.power4j.fist.boot.autoconfigure.mon;

import com.power4j.fist.boot.mon.SmartApiDescriptionResolver;
import com.power4j.fist.boot.mon.aspect.ApiLogAspect;
import com.power4j.fist.boot.mon.aspect.ReportErrorAspect;
import com.power4j.fist.boot.mon.event.ApiLogEvent;
import com.power4j.fist.boot.mon.event.ServerErrorEvent;
import com.power4j.fist.boot.mon.info.DefaultExceptionTranslator;
import com.power4j.fist.boot.mon.info.ExceptionTranslator;
import com.power4j.fist.boot.mon.listener.AbstractEventListener;
import com.power4j.fist.boot.mon.listener.DefaultApiLogEventListener;
import com.power4j.fist.boot.mon.listener.DefaultServerErrorEventListener;
import com.power4j.fist.boot.security.core.UserInfoAccessor;
import org.aspectj.lang.Aspects;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.MessageSourceAccessor;

import java.util.Objects;

/**
 * @author CJ (power4j@outlook.com)
 * @date 2022/1/18
 * @since 1.0
 */
@Configuration(proxyBeanMethods = false)
public class AppMonConfiguration {

	@Bean
	@ConditionalOnClass(Aspects.class)
	public ReportErrorAspect reportErrorAspect() {
		return new ReportErrorAspect();
	}

	@Bean
	@ConditionalOnMissingBean(value = ServerErrorEvent.class, parameterizedContainer = AbstractEventListener.class)
	public DefaultServerErrorEventListener defaultServerErrorEventListener() {
		return new DefaultServerErrorEventListener();
	}

	@Bean
	@ConditionalOnMissingBean(ExceptionTranslator.class)
	public ExceptionTranslator defaultExceptionTranslator(ObjectProvider<MessageSource> messageSourceObjectProvider) {
		final MessageSource source = messageSourceObjectProvider.getIfAvailable();
		MessageSourceAccessor messageSourceAccessor = Objects.nonNull(source) ? new MessageSourceAccessor(source)
				: null;
		return new DefaultExceptionTranslator(messageSourceAccessor);
	}

	@Bean
	@ConditionalOnClass(Aspects.class)
	@ConditionalOnProperty(prefix = "fist.mon.api-log", name = "enabled", matchIfMissing = true)
	public ApiLogAspect apiLogAspect(ExceptionTranslator translator,
			ObjectProvider<UserInfoAccessor> userInfoAccessor) {
		return new ApiLogAspect(new SmartApiDescriptionResolver(), translator, userInfoAccessor.getIfAvailable());
	}

	@Bean
	@ConditionalOnBean(ApiLogAspect.class)
	@ConditionalOnMissingBean(value = ApiLogEvent.class, parameterizedContainer = AbstractEventListener.class)
	public DefaultApiLogEventListener defaultApiLogEventListener() {
		return new DefaultApiLogEventListener();
	}

}
