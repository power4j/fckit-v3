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

package com.power4j.fist.boot.common.utils;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * copy form <a href="https://github.com/callicoder/java-snowflake">java-snowflake</a>
 *
 * @author CJ (power4j@outlook.com)
 */
class SnowflakeTest {

	@Test
	public void nextIdShouldGenerateIdWithCorrectBitsFilled() {
		Snowflake snowflake = new Snowflake(784);

		long beforeTimestamp = Instant.now().toEpochMilli();

		long id = snowflake.nextId();

		// Validate different parts of the Id
		long[] attrs = snowflake.parse(id);
		assertTrue(attrs[0] >= beforeTimestamp);
		assertEquals(784, attrs[1]);
		assertEquals(0, attrs[2]);
	}

	@Test
	public void nextIdShouldGenerateUniqueId() {
		Snowflake snowflake = new Snowflake(234);
		int iterations = 5000;

		// Validate that the IDs are not same even if they are generated in the same ms
		long[] ids = new long[iterations];
		for (int i = 0; i < iterations; i++) {
			ids[i] = snowflake.nextId();
		}

		for (int i = 0; i < ids.length; i++) {
			for (int j = i + 1; j < ids.length; j++) {
				assertFalse(ids[i] == ids[j]);
			}
		}
	}

	@Test
	public void nextIdShouldGenerateUniqueIdIfCalledFromMultipleThreads()
			throws InterruptedException, ExecutionException {
		int numThreads = 50;
		ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
		CountDownLatch latch = new CountDownLatch(numThreads);

		Snowflake snowflake = new Snowflake(234);
		int iterations = 10000;

		// Validate that the IDs are not same even if they are generated in the same ms in
		// different threads
		Future<Long>[] futures = new Future[iterations];
		for (int i = 0; i < iterations; i++) {
			futures[i] = executorService.submit(() -> {
				long id = snowflake.nextId();
				latch.countDown();
				;
				return id;
			});
		}

		latch.await();
		for (int i = 0; i < futures.length; i++) {
			for (int j = i + 1; j < futures.length; j++) {
				assertFalse(futures[i].get() == futures[j].get());
			}
		}
	}

}
