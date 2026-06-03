package com.power4j.fist.jasypt.boot.autoconfigure;

import com.power4j.fist.jasypt.core.GmTextEncryptor;

import org.jasypt.encryption.StringEncryptor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * FIST 国密 Jasypt 自动配置。
 */
@AutoConfiguration
@EnableConfigurationProperties(FistJasyptProperties.class)
@ConditionalOnProperty(prefix = "fist.jasypt", name = "enabled", havingValue = "true")
public class FistJasyptAutoConfiguration {

	@Bean("jasyptStringEncryptor")
	@ConditionalOnMissingBean(name = "jasyptStringEncryptor")
	StringEncryptor jasyptStringEncryptor(FistJasyptProperties properties, Environment environment) {
		return new FistJasyptStringEncryptor(new GmTextEncryptor(), resolveMasterKey(properties, environment),
				properties.getCipherPrefix(), properties.getCipherSuffix());
	}

	private static String resolveMasterKey(FistJasyptProperties properties, Environment environment) {
		if (StringUtils.hasText(properties.getMasterKeyFile())) {
			return readMasterKey(properties.getMasterKeyFile(), properties.getMasterKeyEnv());
		}
		String envName = properties.getMasterKeyEnv();
		String value = environment.getProperty(envName);
		if (StringUtils.hasText(value)) {
			return value.trim();
		}
		throw new IllegalStateException(
				"FIST Jasypt master key is missing. Set " + envName + " or fist.jasypt.master-key-file.");
	}

	private static String readMasterKey(String masterKeyFile, String envName) {
		try {
			String value = Files.readString(Path.of(masterKeyFile), StandardCharsets.UTF_8).trim();
			if (StringUtils.hasText(value)) {
				return value;
			}
			throw new IllegalStateException(
					"FIST Jasypt master key file is empty. Set " + envName + " or fist.jasypt.master-key-file.");
		}
		catch (IOException ex) {
			throw new IllegalStateException(
					"FIST Jasypt master key file cannot be read. Set " + envName + " or fist.jasypt.master-key-file.",
					ex);
		}
	}

}
