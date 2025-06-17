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

package com.power4j.fist.boot.autoconfigure.security;

import com.power4j.fist.boot.security.core.UserInfoSupplier;
import com.power4j.fist.boot.security.inner.DefaultUserCodec;
import com.power4j.fist.boot.security.inner.SecurityUtil;
import com.power4j.fist.boot.security.inner.TrustedUserFilter;
import com.power4j.fist.boot.security.inner.UserDecoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

/**
 * @author CJ (power4j@outlook.com)
 * @date 2021/10/20
 * @since 1.0
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class InnerSecurityConfiguration {

	@Value("${fist.security.authentication.trusted.ip.whitelist:}")
	private List<String> whitelist;

	@Bean
	@ConditionalOnMissingBean(PasswordEncoder.class)
	public PasswordEncoder passwordEncoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}

	@Bean
	@ConditionalOnMissingBean
	public UserDecoder userDecoder() {
		return new DefaultUserCodec();
	}

	@Order(Ordered.HIGHEST_PRECEDENCE + 100_000)
	@Bean
	@ConditionalOnProperty(prefix = "fist.security.authentication.trusted", name = "enabled", havingValue = "true",
			matchIfMissing = true)
	public TrustedUserFilter trustedUserFilter(UserDecoder userDecoder) {
		TrustedUserFilter filter = new TrustedUserFilter(userDecoder);
		filter.setWhitelist(whitelist);
		return filter;
	}

	@Bean
	@ConditionalOnMissingBean
	public UserInfoSupplier userInfoSupplier() {
		return () -> SecurityUtil.getUser().orElse(null);
	}

}
