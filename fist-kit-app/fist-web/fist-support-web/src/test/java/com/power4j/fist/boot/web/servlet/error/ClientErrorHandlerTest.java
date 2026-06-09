package com.power4j.fist.boot.web.servlet.error;

import com.power4j.coca.kit.common.lang.Result;
import com.power4j.fist.boot.common.error.ErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link ClientErrorHandler} 测试。
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.14
 */
class ClientErrorHandlerTest {

	@Test
	void shouldHandleMissingStaticResourceAsClientNotFound() {
		ClientErrorHandler handler = new ClientErrorHandler();

		Result<Object> result = handler
			.handleException(new NoResourceFoundException(HttpMethod.POST, "iam/index.html"));

		assertThat(result.getCode()).isEqualTo(ErrorCode.A9900);
		assertThat(result.getMessage()).isEqualTo("请求资源不存在");
		assertThat(result.getMetaInfo()).containsEntry("hint", "POST iam/index.html");
	}

}
