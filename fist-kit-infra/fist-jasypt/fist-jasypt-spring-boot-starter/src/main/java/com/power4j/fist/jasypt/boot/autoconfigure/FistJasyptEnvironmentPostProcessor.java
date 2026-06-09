package com.power4j.fist.jasypt.boot.autoconfigure;

import com.power4j.fist.jasypt.core.GmEncryptedValue;

import org.springframework.boot.BootstrapContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 将 `fist.jasypt` 密文边界映射到 Jasypt 标准属性。
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.14
 */
public class FistJasyptEnvironmentPostProcessor implements EnvironmentPostProcessor {

	private static final String PROPERTY_SOURCE_NAME = "fistJasyptProperties";

	private static final String FIST_PREFIX = "fist.jasypt.cipher-prefix";

	private static final String FIST_SUFFIX = "fist.jasypt.cipher-suffix";

	private static final String JASYPT_PREFIX = "jasypt.encryptor.property.prefix";

	private static final String JASYPT_SUFFIX = "jasypt.encryptor.property.suffix";

	@Override
	public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
		addJasyptProperties(environment);
	}

	public void postProcessEnvironment(ConfigurableEnvironment environment, BootstrapContext bootstrapContext) {
		addJasyptProperties(environment);
	}

	private static void addJasyptProperties(ConfigurableEnvironment environment) {
		Map<String, Object> properties = new LinkedHashMap<>();
		if (!environment.containsProperty(JASYPT_PREFIX)) {
			properties.put(JASYPT_PREFIX, environment.getProperty(FIST_PREFIX, GmEncryptedValue.PREFIX));
		}
		if (!environment.containsProperty(JASYPT_SUFFIX)) {
			properties.put(JASYPT_SUFFIX, environment.getProperty(FIST_SUFFIX, GmEncryptedValue.SUFFIX));
		}
		if (!properties.isEmpty()) {
			environment.getPropertySources().addFirst(new MapPropertySource(PROPERTY_SOURCE_NAME, properties));
		}
	}

}
