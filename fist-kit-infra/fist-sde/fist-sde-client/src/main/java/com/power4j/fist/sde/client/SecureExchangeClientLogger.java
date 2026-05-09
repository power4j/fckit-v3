package com.power4j.fist.sde.client;

public interface SecureExchangeClientLogger {

	SecureExchangeClientLogger NONE = new SecureExchangeClientLogger() {
		@Override
		public void requestPlain(byte[] body, SecureExchangeClientContext context) {
		}

		@Override
		public void requestEnvelope(byte[] envelope, SecureExchangeClientContext context) {
		}

		@Override
		public void responseEnvelope(byte[] envelope, SecureExchangeClientContext context) {
		}

		@Override
		public void responsePlain(byte[] body, SecureExchangeClientContext context) {
		}
	};

	void requestPlain(byte[] body, SecureExchangeClientContext context);

	void requestEnvelope(byte[] envelope, SecureExchangeClientContext context);

	void responseEnvelope(byte[] envelope, SecureExchangeClientContext context);

	void responsePlain(byte[] body, SecureExchangeClientContext context);

}
