/*
 * Copyright 2024. ChenJun (power4j@outlook.com & https://github.com/John-Chan)
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

package com.power4j.fist.boot.mybaits.migrate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author CJ (power4j@outlook.com)
 * @since 1.0
 */
class ImportPipelineBuilderTest {

	@Test
	void mustSetOneResolver() {
		ImportPipelineBuilder<?, ?, ?> builder = ImportPipelineBuilder.of(null, null)
			.batchUniqueResolver(null)
			.uniqueResolver(null);
		Assertions.assertThrows(IllegalStateException.class, builder::build);
	}

	@Test
	void mustSetUniqueResolve() {
		ImportPipelineBuilder<?, ?, ?> builder = ImportPipelineBuilder.of(null, null);
		Assertions.assertThrows(IllegalStateException.class, builder::build);
	}

}
