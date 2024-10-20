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
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * @author CJ (power4j@outlook.com)
 * @since 1.0
 */
@Slf4j
public class BatchImportPipeline<P, T, ID extends Serializable> implements ImportDataHandler<P> {

	private final UniqueResolveEnum uniqueResolve;

	private final BatchUniqueResolver<T> uniqueResolver;

	private final Repository<T, ID> repository;

	private final Function<P, T> mapper;

	private final boolean cleanAll;

	private final long maxImportCount;

	private final ImportStatistic statistic = ImportStatistic.empty();

	public BatchImportPipeline(UniqueResolveEnum uniqueResolve, BatchUniqueResolver<T> uniqueResolver,
			Repository<T, ID> repository, Function<P, T> mapper, boolean cleanAll, long maxImportCount) {
		if (!cleanAll && uniqueResolve == UniqueResolveEnum.SKIP) {
			throw new IllegalArgumentException("cleanAll must be true when uniqueResolve is SKIP");
		}
		this.uniqueResolve = uniqueResolve;
		this.uniqueResolver = uniqueResolver;
		this.repository = repository;
		this.mapper = mapper;
		this.cleanAll = cleanAll;
		this.maxImportCount = maxImportCount;
	}

	@Override
	public void accept(Collection<P> data) {
		final int size = data.size();
		if (log.isDebugEnabled()) {
			log.debug("receive data,count :{},total :{}", size, statistic.getReceiveCount());
		}
		statistic.addReceiveCount(size);
		handleData(data);
	}

	private void handleData(Collection<P> data) {
		// 批量模式下不进行精确控制条数
		if (statistic.getInsertCount() >= maxImportCount && maxImportCount > 0) {
			log.info("Import max count reached: {},skip", maxImportCount);
			return;
		}
		long existCount;
		List<T> entities = data.stream().map(mapper).toList();
		int skipCount = 0;
		int deleteCount = 0;
		if (!cleanAll && (existCount = uniqueResolver.exists(entities)) > 0L) {
			switch (uniqueResolve) {
				case SKIP:
					skipCount = (int) existCount;
					break;
				case REMOVE:
					uniqueResolver.removeExists(entities);
					deleteCount = (int) existCount;
					break;
				default:
					throw new IllegalStateException("Unsupported unique resolve: " + uniqueResolve);
			}
		}
		repository.saveAll(entities);
		statistic.addSkipCount(skipCount);
		statistic.addDeleteCount(deleteCount);
		statistic.addInsertCount(entities.size() - skipCount);
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
