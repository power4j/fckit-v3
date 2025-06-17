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

package com.power4j.fist.logback.util;

import lombok.experimental.UtilityClass;
import org.slf4j.MDC;

import java.util.Collections;
import java.util.Map;

/**
 * @author CJ (power4j@outlook.com)
 * @since 3.8
 */
@UtilityClass
public class LogUtil {

	public static class WrappedException extends RuntimeException {

		public WrappedException(Throwable cause) {
			super(cause);
		}

	}

	public void runWithMdc(Map<String, String> pairs, Runnable runnable) {
		Map<String, String> map = Collections.unmodifiableMap(pairs);
		map.forEach(MDC::put);
		try {
			runnable.run();
		}
		catch (Throwable e) {
			if (e instanceof WrappedException) {
				throw (WrappedException) e;
			}
			else {
				throw new WrappedException(e);
			}
		}
		finally {
			map.keySet().forEach(MDC::remove);
		}
	}

}
