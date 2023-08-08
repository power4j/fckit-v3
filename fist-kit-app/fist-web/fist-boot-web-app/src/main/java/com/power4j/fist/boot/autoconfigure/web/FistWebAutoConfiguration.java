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

package com.power4j.fist.boot.autoconfigure.web;

import com.power4j.fist.boot.web.constant.HttpConstant;
import com.power4j.fist.boot.mon.info.TraceInfo;
import com.power4j.fist.boot.mon.info.TraceInfoResolver;
import com.power4j.fist.boot.web.servlet.mvc.formatter.LocalDateFormatter;
import com.power4j.fist.boot.web.servlet.mvc.formatter.LocalDateTimeFormatter;
import com.power4j.fist.boot.web.servlet.mvc.formatter.LocalTimeFormatter;
import com.power4j.fist.boot.web.servlet.mvc.formatter.MonthDayFormatter;
import com.power4j.fist.boot.web.servlet.mvc.formatter.YearMonthFormatter;
import com.power4j.fist.support.spring.web.servlet.util.HttpServletRequestUtil;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.YearMonth;
import java.util.Optional;

/**
 * @author CJ (power4j@outlook.com)
 * @date 2021/6/16
 * @since 1.0
 */
@AutoConfiguration
@ComponentScan(basePackages = { "com.power4j.fist.boot.web.servlet.error" })
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class FistWebAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean(value = HttpServletRequest.class, parameterizedContainer = TraceInfoResolver.class)
	public TraceInfoResolver<HttpServletRequest> requestTraceInfoResolver() {
		return request -> {
			String reqId = HttpServletRequestUtil.getHeader(request, HttpConstant.Header.KEY_REQUEST_ID).orElse(null);
			TraceInfo info = new TraceInfo();
			info.setRequestId(reqId);
			// TODO uid?
			return Optional.of(info);
		};
	}

	@Configuration
	public static class WebMvcConfig implements WebMvcConfigurer {

		@Override
		public void addFormatters(FormatterRegistry registry) {
			registry.addFormatterForFieldType(YearMonth.class, new YearMonthFormatter());
			registry.addFormatterForFieldType(MonthDay.class, new MonthDayFormatter());
			registry.addFormatterForFieldType(LocalDate.class, new LocalDateFormatter());
			registry.addFormatterForFieldType(LocalTime.class, new LocalTimeFormatter());
			registry.addFormatterForFieldType(LocalDateTime.class, new LocalDateTimeFormatter());
		}

	}

}
