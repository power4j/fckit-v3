package com.power4j.fist.sde.boot.autoconfigure.client;

import com.power4j.fist.sde.client.SecureExchangeOperations;
import com.power4j.fist.sde.client.feign.SecureFeignDecoder;
import com.power4j.fist.sde.client.feign.SecureFeignEncoder;
import feign.codec.Decoder;
import feign.codec.Encoder;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration(after = SdeClientAutoConfiguration.class)
@ConditionalOnClass(name = "feign.codec.Encoder")
@ConditionalOnBean(SecureExchangeOperations.class)
public class SdeFeignClientAutoConfiguration {

	@Bean
	@ConditionalOnBean(Encoder.class)
	@ConditionalOnMissingBean(SecureFeignEncoder.class)
	public SecureFeignEncoder secureFeignEncoder(Encoder delegate, SecureExchangeOperations operations) {
		return new SecureFeignEncoder(delegate, operations);
	}

	@Bean
	@ConditionalOnBean(Decoder.class)
	@ConditionalOnMissingBean(SecureFeignDecoder.class)
	public SecureFeignDecoder secureFeignDecoder(Decoder delegate, SecureExchangeOperations operations) {
		return new SecureFeignDecoder(delegate, operations);
	}

}
