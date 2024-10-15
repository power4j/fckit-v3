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

import java.io.Serializable;
import java.util.Iterator;
import java.util.function.Function;

/**
 * @author CJ (power4j@outlook.com)
 * @since 1.0
 */
public class ImportPipelineBuilder<P, T, ID extends Serializable> {

	private final Repository<T, ID> repository;

	private final Function<P, T> mapper;

	private UniqueResolveEnum uniqueResolve;

	private UniqueHandler<T> uniqueHandler;

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

	public ImportPipelineBuilder<P, T, ID> uniqueDetector(UniqueHandler<T> uniqueHandler) {
		this.uniqueHandler = uniqueHandler;
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
		return new ImportPipeline<>(uniqueResolve, uniqueHandler, repository, mapper, cleanAll, maxImportCount);
	}

	@RequiredArgsConstructor
	static class ImportPipeline<P, T, ID extends Serializable> implements DataImporter<P> {

		private final UniqueResolveEnum uniqueResolve;

		private final UniqueHandler<T> uniqueHandler;

		private final Repository<T, ID> repository;

		private final Function<P, T> mapper;

		private final boolean cleanAll;

		private final long maxImportCount;

		private final ImportStatistic statistic = ImportStatistic.of();

		@Override
		public void handleData(Iterable<P> itr) {
			Iterator<P> it = itr.iterator();
			while (it.hasNext() && statistic.getInsertCount() < maxImportCount) {
				P p = it.next();
				T t = mapper.apply(p);
				if (!cleanAll && uniqueHandler.exists(t)) {
					switch (uniqueResolve) {
						case SKIP:
							statistic.addSkipCount(1);
							continue;
						case REMOVE:
							uniqueHandler.remove(t);
							statistic.addDeleteCount(1);
							break;
					}
				}
				repository.saveOne(t);
				statistic.addInsertCount(1);
			}
		}

		@Override
		public ImportStatistic statistic() {
			return statistic;
		}

		@Override
		public void beforeImport() {
			if (cleanAll) {
				long deleteCount = repository.countAll();
				repository.deleteAll();
				statistic.setDeleteCount(deleteCount);
			}
		}

	}

}
