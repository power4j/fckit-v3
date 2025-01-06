/*
 * Copyright 2025. ChenJun (power4j@outlook.com & https://github.com/John-Chan)
 *
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE 3.0;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.gnu.org/licenses/lgpl.html
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.power4j.fist.cloud.gateway.ratelimit;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.power4j.fist.cloud.gateway.test.BaseWebClientTests;
import io.github.bucket4j.caffeine.CaffeineProxyManager;
import io.github.bucket4j.distributed.proxy.AsyncProxyManager;
import io.github.bucket4j.distributed.remote.RemoteBucketState;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.RetryingTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.filter.ratelimit.RateLimiter;
import org.springframework.cloud.gateway.support.ConfigurationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;

import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * copy form
 * <a href="https://github.com/spring-cloud/spring-cloud-gateway">spring-cloud-gateway</a>
 *
 * @author CJ (power4j@outlook.com)
 * @since 1.0
 */
@SpringBootTest(webEnvironment = RANDOM_PORT, properties = "spring.cloud.gateway.redis.enabled=false")
@DirtiesContext
class Bucket4jRateLimiterTest extends BaseWebClientTests {

	@Autowired
	private Bucket4jRateLimiter rateLimiter;

	@RetryingTest(3)
	public void bucket4jRateLimiterGreedyWorks() throws Exception {
		bucket4jRateLimiterWorks(Bucket4jRateLimiter.RefillStyle.GREEDY);
	}

	@RetryingTest(3)
	public void bucket4jRateLimiterIntervallyWorks() throws Exception {
		bucket4jRateLimiterWorks(Bucket4jRateLimiter.RefillStyle.INTERVALLY);
	}

	public void bucket4jRateLimiterWorks(Bucket4jRateLimiter.RefillStyle refillStyle) throws Exception {
		String id = UUID.randomUUID().toString();

		long capacity = 10;
		int requestedTokens = 1;

		String routeId = "myroute";
		rateLimiter.getConfig()
			.put(routeId,
					new Bucket4jRateLimiter.Config().setRefillStyle(refillStyle)
						.setHeaderName("X-RateLimit-Custom")
						.setCapacity(capacity)
						.setRefillPeriod(Duration.ofSeconds(1)));

		checkLimitEnforced(id, capacity, requestedTokens, routeId);
	}

	@Test
	public void bucket4jRateLimiterIsAllowedFalseWorks() throws Exception {
		String id = UUID.randomUUID().toString();

		int capacity = 1;
		int requestedTokens = 2;

		String routeId = "zero_capacity_route";
		rateLimiter.getConfig()
			.put(routeId,
					new Bucket4jRateLimiter.Config().setCapacity(capacity)
						.setRefillPeriod(Duration.ofSeconds(1))
						.setRequestedTokens(requestedTokens));

		RateLimiter.Response response = rateLimiter.isAllowed(routeId, id).block();
		assertThat(response.isAllowed()).isFalse();
	}

	private void checkLimitEnforced(String id, long capacity, int requestedTokens, String routeId)
			throws InterruptedException {
		// Bursts work
		simulateBurst(id, capacity, requestedTokens, routeId);

		checkLimitReached(id, routeId, capacity);

		Thread.sleep(Math.max(1, requestedTokens / capacity) * 1000);

		// # After the burst is done, check the steady state
		checkSteadyState(id, capacity, routeId);
	}

	private void simulateBurst(String id, long capacity, int requestedTokens, String routeId) {
		long previousRemaining = capacity;
		for (int i = 0; i < capacity / requestedTokens; i++) {
			RateLimiter.Response response = rateLimiter.isAllowed(routeId, id).block();
			assertThat(response.isAllowed()).as("Burst # %s is allowed", i).isTrue();
			assertThat(response.getHeaders()).containsKey("X-RateLimit-Custom");
			System.err.println("response headers: " + response.getHeaders());
			long remaining = Long.parseLong(response.getHeaders().get("X-RateLimit-Custom"));
			assertThat(remaining).isLessThan(previousRemaining);
			previousRemaining = remaining;
			// TODO: assert additional headers
		}
	}

	private void checkLimitReached(String id, String routeId, long capacity) {
		RateLimiter.Response response = rateLimiter.isAllowed(routeId, id).block();
		if (response.isAllowed()) { // TODO: sometimes there is an off by one error
			response = rateLimiter.isAllowed(routeId, id).block();
		}
		assertThat(response.isAllowed()).as("capacity # %s is not allowed", capacity).isFalse();
	}

	private void checkSteadyState(String id, long capacity, String routeId) {
		RateLimiter.Response response;
		for (int i = 0; i < capacity; i++) {
			response = rateLimiter.isAllowed(routeId, id).block();
			assertThat(response.isAllowed()).as("steady state # %s is allowed", i).isTrue();
		}

		response = rateLimiter.isAllowed(routeId, id).block();
		assertThat(response.isAllowed()).as("steady state # %s is allowed", capacity).isFalse();
	}

	@EnableAutoConfiguration
	@SpringBootConfiguration
	@Import(DefaultTestConfig.class)
	public static class TestConfig {

		@Bean
		public AsyncProxyManager<String> caffeineProxyManager() {
			Caffeine<String, RemoteBucketState> builder = (Caffeine) Caffeine.newBuilder().maximumSize(100);
			return new CaffeineProxyManager<>(builder, Duration.ofMinutes(1)).asAsync();
		}

		@Bean
		public Bucket4jRateLimiter bucket4jRateLimiter(AsyncProxyManager<String> proxyManager,
				ConfigurationService configurationService) {
			return new Bucket4jRateLimiter(proxyManager, configurationService);
		}

	}

}
