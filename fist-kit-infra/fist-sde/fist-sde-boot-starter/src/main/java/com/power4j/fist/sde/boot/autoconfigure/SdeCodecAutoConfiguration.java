package com.power4j.fist.sde.boot.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.power4j.fist.sde.core.codec.JacksonSecureEnvelopeCodec;
import com.power4j.fist.sde.core.codec.SecureEnvelopeCodec;
import com.power4j.fist.sde.core.json.JacksonSecureJsonCodec;
import com.power4j.fist.sde.core.json.SecureJsonCodec;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@AutoConfiguration(after = SdeCoreAutoConfiguration.class)
@ConditionalOnProperty(prefix = "fist.sde", name = "enabled", havingValue = "true")
public class SdeCodecAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public ObjectMapper secureEnvelopeObjectMapper() {
		return new ObjectMapper();
	}

	@Bean
	@ConditionalOnMissingBean
	public SecureEnvelopeCodec secureEnvelopeCodec(ObjectMapper objectMapper) {
		return new JacksonSecureEnvelopeCodec(objectMapper);
	}

	@Bean
	@ConditionalOnMissingBean
	public SecureJsonCodec secureJsonCodec(ObjectMapper objectMapper) {
		return new JacksonSecureJsonCodec(objectMapper);
	}

}
