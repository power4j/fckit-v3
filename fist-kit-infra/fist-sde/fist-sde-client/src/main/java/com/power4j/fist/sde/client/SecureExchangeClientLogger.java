package com.power4j.fist.sde.client;

import org.jspecify.annotations.Nullable;

public interface SecureExchangeClientLogger {

	SecureExchangeClientLogger NONE = new SecureExchangeClientLogger() {
		@Override
		public void requestPlain(byte[] body, @Nullable SecureExchangeClientContext context) {
		}

		@Override
		public void requestEnvelope(byte[] envelope, @Nullable SecureExchangeClientContext context) {
		}

		@Override
		public void responseEnvelope(byte[] envelope, @Nullable SecureExchangeClientContext context) {
		}

		@Override
		public void responsePlain(byte[] body, @Nullable SecureExchangeClientContext context) {
		}
	};

	void requestPlain(byte[] body, @Nullable SecureExchangeClientContext context);

	void requestEnvelope(byte[] envelope, @Nullable SecureExchangeClientContext context);

	void responseEnvelope(byte[] envelope, @Nullable SecureExchangeClientContext context);

	void responsePlain(byte[] body, @Nullable SecureExchangeClientContext context);

}
