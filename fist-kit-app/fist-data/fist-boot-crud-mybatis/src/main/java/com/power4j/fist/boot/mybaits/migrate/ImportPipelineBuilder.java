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

import java.io.Serializable;
import java.util.function.Function;

/**
 * @author CJ (power4j@outlook.com)
 * @since 1.0
 */
public class ImportPipelineBuilder<P, T, ID extends Serializable> {

	private final Repository<T, ID> repository;

	private final Function<P, T> mapper;

	private UniqueResolveEnum uniqueResolve;

	private BatchUniqueResolver<T> batchUniqueResolver;

	private UniqueResolver<T> uniqueResolver;

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

	public ImportPipelineBuilder<P, T, ID> batchUniqueResolver(BatchUniqueResolver<T> resolver) {
		this.uniqueResolver = null;
		this.batchUniqueResolver = resolver;
		return this;
	}

	public ImportPipelineBuilder<P, T, ID> uniqueResolver(UniqueResolver<T> resolver) {
		this.batchUniqueResolver = null;
		this.uniqueResolver = resolver;
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
		if (!cleanAll && uniqueResolve == null) {
			throw new IllegalStateException("must set unique resolve strategy when cleanAll is false");
		}
		if (uniqueResolver == null && batchUniqueResolver == null) {
			throw new IllegalStateException("must set uniqueResolver or batchUniqueResolver");
		}
		if (batchUniqueResolver != null) {
			return new BatchImportPipeline<>(uniqueResolve, batchUniqueResolver, repository, mapper, cleanAll,
					maxImportCount);
		}
		return new ImportPipeline<>(uniqueResolve, uniqueResolver, repository, mapper, cleanAll, maxImportCount);
	}

}
