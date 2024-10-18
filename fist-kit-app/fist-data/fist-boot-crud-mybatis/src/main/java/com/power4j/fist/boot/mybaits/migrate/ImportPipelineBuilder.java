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
import com.power4j.fist.data.migrate.DataImporter;
import com.power4j.fist.data.migrate.ImportStatistic;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * @author CJ (power4j@outlook.com)
 * @since 1.0
 */
public class ImportPipelineBuilder<P, T, ID extends Serializable> {

	private final Repository<T, ID> repository;

	private final Function<P, T> mapper;

	private UniqueResolveEnum uniqueResolve;

	private BatchUniqueResolver<T> uniqueResolver;

	private boolean cleanAll;

	private long maxImportCount;

	public static <P, T, ID extends Serializable> ImportPipelineBuilder<P, T, ID> of(Repository<T, ID> repository,
			Function<P, T> mapper) {
		return new ImportPipelineBuilder<>(repository, mapper);
	}

	public ImportPipelineBuilder(Repository<T, ID> repository, Function<P, T> mapper) {
		this.repository = repository;
		this.mapper = mapper;
	}

	public ImportPipelineBuilder<P, T, ID> conflictResolve(UniqueResolveEnum conflictResolve) {
		this.uniqueResolve = conflictResolve;
		return this;
	}

	public ImportPipelineBuilder<P, T, ID> uniqueDetector(BatchUniqueResolver<T> uniqueResolver) {
		this.uniqueResolver = uniqueResolver;
		return this;
	}

	public ImportPipelineBuilder<P, T, ID> cleanAll(boolean cleanAll) {
		this.cleanAll = cleanAll;
		return this;
	}

	public ImportPipelineBuilder<P, T, ID> maxImportCount(long maxImportCount) {
		this.maxImportCount = maxImportCount;
		return this;
	}

	public DataImporter<P> build() {
		return new ImportPipeline<>(uniqueResolve, uniqueResolver, repository, mapper, cleanAll, maxImportCount);
	}

	@Slf4j
	@RequiredArgsConstructor
	static class ImportPipeline<P, T, ID extends Serializable> implements DataImporter<P> {

		private final UniqueResolveEnum uniqueResolve;

		private final BatchUniqueResolver<T> uniqueResolver;

		private final Repository<T, ID> repository;

		private final Function<P, T> mapper;

		private final boolean cleanAll;

		private final long maxImportCount;

		private final ImportStatistic statistic = ImportStatistic.empty();

		@Override
		public void accept(Collection<P> data) {
			statistic.addReceiveCount(data.size());
			handleData(data);
		}

		private void handleData(Collection<P> data) {
			// 不需要精确控制条数
			if (statistic.getInsertCount() >= maxImportCount && maxImportCount > 0) {
				log.info("Import max count reached: {},skip", maxImportCount);
				return;
			}
			long exists;
			List<T> entities = data.stream().map(mapper).toList();
			if (!cleanAll && (exists = uniqueResolver.exists(entities)) > 0L) {
				switch (uniqueResolve) {
					case SKIP:
						statistic.addSkipCount((int) exists);
						return;
					case REMOVE:
						uniqueResolver.removeExists(entities);
						statistic.addDeleteCount((int) exists);
						break;
					default:
						throw new IllegalStateException("Unsupported unique resolve: " + uniqueResolve);
				}
			}
			repository.saveAll(entities);
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

}
