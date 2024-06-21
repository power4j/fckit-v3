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

package com.power4j.fist.boot.common.jackson;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.power4j.fist.boot.common.jackson.module.DateTimeModule;
import com.power4j.fist.boot.common.jackson.module.NumberStrModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.time.ZoneId;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;

/**
 * @author CJ (power4j@outlook.com)
 * @date 2021/12/16
 * @since 1.0
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(ObjectMapper.class)
@ConditionalOnProperty(prefix = JacksonCustomizeProperties.PROP_PREFIX, name = "enabled", matchIfMissing = true)
@EnableConfigurationProperties(JacksonCustomizeProperties.class)
@AutoConfigureBefore(JacksonAutoConfiguration.class)
@RequiredArgsConstructor
public class JacksonConfig {

	private final JacksonCustomizeProperties jacksonCustomizeProperties;

	private final static Map<JacksonCustomizeProperties.ModuleName, Module> MODULE_MAP = new HashMap<>(2);
	static {
		MODULE_MAP.put(JacksonCustomizeProperties.ModuleName.Date, new DateTimeModule());
		MODULE_MAP.put(JacksonCustomizeProperties.ModuleName.NumberToStr, new NumberStrModule());
	}

	@Bean
	public Jackson2ObjectMapperBuilderCustomizer customizer() {
		return builder -> {
			builder.locale(Locale.CHINA);
			applyTimeZone(builder);
			applySimpleDateFormat(builder);
			applyModules(builder);
		};
	}

	private void applyTimeZone(Jackson2ObjectMapperBuilder builder) {
		final String timeZoneId = jacksonCustomizeProperties.getTimeZoneId();
		if (StringUtils.isEmpty(timeZoneId)) {
			return;
		}
		log.info("Set time zone :{}", timeZoneId);
		if (JacksonCustomizeProperties.TZ_SYSTEM.equalsIgnoreCase(timeZoneId)) {
			builder.timeZone(TimeZone.getTimeZone(ZoneId.systemDefault()));
			return;
		}
		builder.timeZone(TimeZone.getTimeZone(ZoneId.of(timeZoneId)));
	}

	private void applySimpleDateFormat(Jackson2ObjectMapperBuilder builder) {
		final String format = jacksonCustomizeProperties.getSimpleDateFormat();
		if (!StringUtils.isEmpty(format)) {
			log.info("Set simple date format :{}", format);
			builder.simpleDateFormat(format);
		}
	}

	private void applyModules(Jackson2ObjectMapperBuilder builder) {
		List<JacksonCustomizeProperties.ModuleName> excludeModules = ObjectUtils
			.defaultIfNull(jacksonCustomizeProperties.getExcludeModules(), Collections.emptyList());
		List<Module> modules = MODULE_MAP.entrySet()
			.stream()
			.filter(kv -> !excludeModules.contains(kv.getKey()))
			.map(Map.Entry::getValue)
			.toList();
		if (!modules.isEmpty()) {
			List<String> names = modules.stream().map(Module::getModuleName).collect(Collectors.toList());
			log.info("Install modules: {}", names);
			builder.modulesToInstall(modules.toArray(modules.toArray(new Module[0])));
		}
	}

}
