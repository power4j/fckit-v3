package com.power4j.fist.cloud.rpc.feign.sde.prototype;

import feign.codec.Decoder;
import feign.codec.Encoder;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@AutoConfiguration
@ConditionalOnClass({ Encoder.class, Decoder.class })
@ConditionalOnProperty(prefix = "fist.sde.feign.prototype", name = "enabled", havingValue = "true")
class SdeFeignPrototypeConfiguration {

	@Bean
	@ConditionalOnMissingBean
	PrototypeSecureFeignSupport prototypeSecureFeignSupport() {
		return PrototypeSecureFeignSupport.defaults();
	}

	@Bean
	@Primary
	PrototypeSecureFeignEncoder prototypeSecureFeignEncoder(ObjectProvider<Encoder> encoders,
			PrototypeSecureFeignSupport support) {
		return new PrototypeSecureFeignEncoder(delegateEncoder(encoders), support);
	}

	@Bean
	@Primary
	PrototypeSecureFeignDecoder prototypeSecureFeignDecoder(ObjectProvider<Decoder> decoders,
			PrototypeSecureFeignSupport support) {
		return new PrototypeSecureFeignDecoder(delegateDecoder(decoders), support);
	}

	private Encoder delegateEncoder(ObjectProvider<Encoder> encoders) {
		return encoders.stream()
			.filter((encoder) -> !(encoder instanceof PrototypeSecureFeignEncoder))
			.findFirst()
			.orElseThrow(() -> new IllegalStateException("delegate feign encoder not found"));
	}

	private Decoder delegateDecoder(ObjectProvider<Decoder> decoders) {
		return decoders.stream()
			.filter((decoder) -> !(decoder instanceof PrototypeSecureFeignDecoder))
			.findFirst()
			.orElseThrow(() -> new IllegalStateException("delegate feign decoder not found"));
	}

}
