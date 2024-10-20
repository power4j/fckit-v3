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

import com.power4j.fist.boot.mybaits.crud.repository.Repository;

import java.io.Serializable;
import java.util.function.Function;

/**
 * @author CJ (power4j@outlook.com)
 * @since 1.0
 */
public class PrimaryResolver<T, ID extends Serializable> implements UniqueResolver<T> {

	private final Repository<T, ID> repository;

	private final Function<T, ID> idExtractor;

	public PrimaryResolver(Repository<T, ID> repository, Function<T, ID> idExtractor) {
		this.repository = repository;
		this.idExtractor = idExtractor;
	}

	@Override
	public boolean exists(T example) {
		ID id = idExtractor.apply(example);
		return repository.existsById(id);
	}

	@Override
	public void remove(T example) {
		ID id = idExtractor.apply(example);
		repository.deleteOneById(id);
	}

}
