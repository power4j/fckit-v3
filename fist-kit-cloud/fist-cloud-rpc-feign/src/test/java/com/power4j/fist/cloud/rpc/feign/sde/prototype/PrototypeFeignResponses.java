package com.power4j.fist.cloud.rpc.feign.sde.prototype;

import feign.Response;

class PrototypeFeignResponses {

	private PrototypeFeignResponses() {
	}

	static Response replaceBody(Response response, byte[] body) {
		return response.toBuilder().body(body).build();
	}

}
