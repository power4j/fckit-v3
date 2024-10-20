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
import com.power4j.fist.data.migrate.ImportDataHandler;
import com.power4j.fist.data.migrate.ImportStatistic;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.Collection;
import java.util.function.Function;

/**
 * @author CJ (power4j@outlook.com)
 * @since 1.0
 */
@Slf4j
@RequiredArgsConstructor
public class ImportPipeline<P, T, ID extends Serializable> implements ImportDataHandler<P> {

	private final UniqueResolveEnum uniqueResolve;

	private final UniqueResolver<T> uniqueResolver;

	private final Repository<T, ID> repository;

	private final Function<P, T> mapper;

	private final boolean cleanAll;

	private final long maxImportCount;

	private final ImportStatistic statistic = ImportStatistic.empty();

	@Override
	public void accept(Collection<P> data) {
		final int size = data.size();
		if (log.isDebugEnabled()) {
			log.debug("receive data,count :{},total :{}", size, statistic.getReceiveCount());
		}
		statistic.addReceiveCount(data.size());
		handleData(data);
	}

	private void handleData(Collection<P> data) {
		for (P p : data) {
			if (statistic.getInsertCount() >= maxImportCount && maxImportCount > 0) {
				log.info("Import max count reached: {},skip", maxImportCount);
				return;
			}
			T entity = mapper.apply(p);
			if (!cleanAll && uniqueResolver.exists(entity)) {
				switch (uniqueResolve) {
					case SKIP:
						statistic.addSkipCount(1);
						return;
					case REMOVE:
						uniqueResolver.remove(entity);
						statistic.addDeleteCount(1);
						break;
					default:
						throw new IllegalStateException("Unsupported unique resolve: " + uniqueResolve);
				}
			}
			repository.saveOne(entity);
			statistic.addInsertCount(1);
		}

	}

	@Override
	public ImportStatistic statistic() {
		return statistic;
	}

	@Override
	public void beforeAll() {
		statistic.reset();
		if (cleanAll) {
			long deleteCount = repository.countAll();
			repository.deleteAll();
			statistic.setDeleteCount(deleteCount);
		}
	}

}
