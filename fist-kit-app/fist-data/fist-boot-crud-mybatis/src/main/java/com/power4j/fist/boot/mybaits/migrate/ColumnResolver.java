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

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.power4j.fist.boot.mybaits.crud.repository.Repository;

import java.io.Serializable;
import java.util.function.BiConsumer;

/**
 * @author CJ (power4j@outlook.com)
 * @since 1.0
 */
public class ColumnResolver<T> implements UniqueResolver<T> {

	private final Repository<T, ? extends Serializable> repository;

	private final BiConsumer<T, LambdaQueryWrapper<T>> queryBuilder;

	public static <T> ColumnResolver<T> of(Repository<T, ? extends Serializable> repository,
			BiConsumer<T, LambdaQueryWrapper<T>> queryBuilder) {
		return new ColumnResolver<>(repository, queryBuilder);
	}

	public ColumnResolver(Repository<T, ? extends Serializable> repository,
			BiConsumer<T, LambdaQueryWrapper<T>> queryBuilder) {
		this.queryBuilder = queryBuilder;
		this.repository = repository;
	}

	@Override
	public boolean exists(T example) {
		return repository.countBy(applyQuery(example)) > 0;
	}

	@Override
	public void remove(T example) {
		repository.deleteAllBy(applyQuery(example));
	}

	protected LambdaQueryWrapper<T> applyQuery(T example) {
		LambdaQueryWrapper<T> queryWrapper = repository.lambdaWrapper();
		queryBuilder.accept(example, queryWrapper);
		return queryWrapper;
	}

}
