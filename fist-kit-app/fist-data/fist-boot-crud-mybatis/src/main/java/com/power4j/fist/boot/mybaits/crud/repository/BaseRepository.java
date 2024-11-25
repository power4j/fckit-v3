/*
 *  Copyright 2021 ChenJun (power4j@outlook.com & https://github.com/John-Chan)
 *
 *  Licensed under the GNU LESSER GENERAL PUBLIC LICENSE 3.0;
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  <p>
 *  http://www.gnu.org/licenses/lgpl.html
 *  <p>
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.power4j.fist.boot.mybaits.crud.repository;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.repository.CrudRepository;
import com.power4j.fist.boot.mybaits.crud.repository.matcher.Eq;
import com.power4j.fist.boot.mybaits.util.LambdaHelper;
import com.power4j.fist.boot.mybaits.util.PageUtil;
import com.power4j.fist.data.domain.Pageable;
import com.power4j.fist.data.domain.Paged;
import com.power4j.fist.data.domain.Sort;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author CJ (power4j@outlook.com)
 * @date 2021/9/18
 * @since 1.0
 * @param <M> Mapper类型
 * @param <T> 实体类型
 * @param <ID> 主键类型
 */
@Transactional(rollbackFor = Exception.class)
public class BaseRepository<M extends BaseMapper<T>, T, ID extends Serializable> extends CrudRepository<M, T>
		implements Repository<T, ID> {

	@Nullable
	private LambdaHelper<T> lambdaHelper;

	@Override
	public T saveOne(T entity) {
		save(entity);
		return entity;
	}

	@Override
	public List<T> saveAll(Iterable<T> entities) {
		List<T> list = toList(entities);
		saveBatch(list, Repository.DEFAULT_BATCH_SIZE);
		return list;
	}

	@Override
	public T updateOneById(T entity) {
		updateById(entity);
		return entity;
	}

	@Override
	public List<T> updateAllById(Iterable<T> entities) {
		return updateAllById(entities, Repository.DEFAULT_BATCH_SIZE);
	}

	@Override
	public Optional<T> findOneById(ID id) {
		return Optional.ofNullable(getById(id));
	}

	@Override
	public boolean existsById(ID id) {
		QueryWrapper<T> wrapper = new QueryWrapper<>();
		TableInfo tableInfo = getTableInfo();
		wrapper.eq(tableInfo.getKeyColumn(), id);
		return getBaseMapper().selectCount(wrapper) > 0;
	}

	@Override
	public List<T> findAll() {
		return list();
	}

	@Override
	public List<T> findAllById(Iterable<ID> ids) {
		if (ObjectUtils.isEmpty(ids)) {
			return Collections.emptyList();
		}
		return listByIds(toList(ids));
	}

	@Override
	public long countAll() {
		return count();
	}

	@Override
	public void deleteOneById(ID id) {
		removeById(id);
	}

	@Override
	public void deleteAllById(Iterable<? extends ID> ids) {
		if (ObjectUtils.isEmpty(ids)) {
			return;
		}
		removeByIds(toList(ids));
	}

	@Override
	public void deleteAll() {
		remove(Wrappers.emptyWrapper());
	}

	@Override
	public Paged<T> findAll(Pageable pageable) {
		IPage<T> page = page(PageUtil.toPage(pageable));
		return PageUtil.toPaged(page);
	}

	@Override
	public List<T> saveAll(Iterable<T> entities, int batchSize) {
		List<T> list = toList(entities);
		saveBatch(list, batchSize);
		return list;
	}

	@Override
	public List<T> updateAllById(Iterable<T> entities, int batchSize) {
		List<T> list = toList(entities);
		updateBatchById(list, batchSize);
		return list;
	}

	@Override
	public Optional<T> findOneBy(@Nullable Wrapper<T> queryWrapper) {
		return Optional.ofNullable(getBaseMapper().selectOne(queryWrapper));
	}

	@Override
	public List<T> findAllBy(@Nullable Wrapper<T> queryWrapper) {
		return list(queryWrapper);
	}

	@Override
	public Paged<T> findAllBy(Pageable pageable, @Nullable Wrapper<T> queryWrapper) {
		IPage<T> page = page(PageUtil.toPage(pageable), queryWrapper);
		return PageUtil.toPaged(page);
	}

	@Override
	public List<T> findAllSorted(Sort sort) {
		if (sort.isUnsorted()) {
			return list();
		}
		QueryWrapper<T> wrapper = new QueryWrapper<>();
		sort.getOrders().forEach(o -> wrapper.orderBy(true, o.getDirection().isAscending(), o.getProp()));
		return list(wrapper);
	}

	@Override
	public long deleteAllBy(@Nullable Wrapper<T> queryWrapper) {
		return getBaseMapper().delete(queryWrapper);
	}

	@Override
	public long countBy(@Nullable Wrapper<T> queryWrapper) {
		return getBaseMapper().selectCount(queryWrapper);
	}

	@Override
	public LambdaQueryChainWrapper<T> querying() {
		return super.lambdaQuery();
	}

	@Override
	public LambdaUpdateChainWrapper<T> updating() {
		return super.lambdaUpdate();
	}

	@Override
	public LambdaQueryWrapper<T> lambdaWrapper() {
		return Wrappers.lambdaQuery(getEntityClass());
	}

	@Override
	public long lambdaCount(Eq<T> expr, @Nullable ID exclude) {
		return countByColumn(getLambdaHelper().colToStr(expr.getColumn(), true), expr.getValue(), exclude);
	}

	@Override
	public long lambdaCount(List<Eq<T>> expr, @Nullable ID exclude) {
		Map<String, Object> parsed = expr.stream()
			.collect(Collectors.toMap(o -> getLambdaHelper().colToStr(o.getColumn(), true),
					o -> Objects.requireNonNull(o.getValue())));
		return countByColumns(parsed, exclude);
	}

	@Override
	public T fetchVersion(T entity, boolean required) {
		TableInfo tableInfo = getTableInfo();

		Object currentVersion = loadVersionValueById(entity, tableInfo);
		if (required && Objects.isNull(currentVersion)) {
			throw new IllegalStateException("乐观锁字段未初始化");
		}
		TableFieldInfo versionField = tableInfo.getVersionFieldInfo();
		setFieldValue(entity, versionField.getField(), currentVersion);
		return entity;
	}

	@Nullable
	Object loadVersionValueById(T entity, TableInfo tableInfo) {
		if (!tableInfo.isWithVersion()) {
			throw new IllegalStateException("该实体类型不支持乐观锁");
		}
		TableFieldInfo versionField = tableInfo.getVersionFieldInfo();
		if (!tableInfo.havePK()) {
			throw new UnsupportedOperationException("不支持没有ID字段的实体类型");
		}
		TableFieldInfo idField = tableInfo.getFieldList()
			.stream()
			.filter(o -> tableInfo.getKeyColumn().equals(o.getColumn()))
			.findFirst()
			.orElse(null);
		assert idField != null;

		Object idValue = getFieldValue(entity, idField.getField());

		QueryWrapper<T> wrapper = new QueryWrapper<>();
		wrapper.select(versionField.getColumn());
		wrapper.eq(tableInfo.getKeyColumn(), idValue);

		T found = getBaseMapper().selectOne(wrapper);
		if (Objects.isNull(found)) {
			return null;
		}
		return getFieldValue(found, versionField.getField());
	}
	// ~ Util
	// ===================================================================================================

	protected long countByColumn(String column, @Nullable Object value, @Nullable ID ignoreId) {
		Map<String, Object> map = new HashMap<>(1);
		map.put(column, value);
		return countByColumns(map, ignoreId);
	}

	protected long countByColumns(Map<String, Object> columns, @Nullable ID ignoreId) {
		QueryWrapper<T> wrapper = new QueryWrapper<>();
		wrapper.allEq(columns);
		if (ignoreId != null) {
			TableInfo tableInfo = getTableInfo();
			wrapper.ne(tableInfo.getKeyColumn(), ignoreId);
		}
		return getBaseMapper().selectCount(wrapper);
	}

	protected LambdaHelper<T> getLambdaHelper() {
		if (lambdaHelper == null) {
			lambdaHelper = new LambdaHelper<>(getEntityClass());
		}
		return lambdaHelper;
	}

	public static <U> List<U> toList(@Nullable Iterable<U> src) {
		if (Objects.isNull(src)) {
			return Collections.emptyList();
		}
		return StreamSupport.stream(src.spliterator(), false).collect(Collectors.toList());
	}

	protected TableInfo getTableInfo() {
		Class<?> entityType = getEntityClass();
		TableInfo tableInfo = TableInfoHelper.getTableInfo(entityType);
		if (tableInfo == null) {
			throw new IllegalStateException(String.format("为找到实体类(%s)的元信息,请检查mybatis配置", entityType.getSimpleName()));
		}
		return tableInfo;
	}

	@Nullable
	Object getFieldValue(Object obj, Field field) {
		try {
			field.setAccessible(true);
			return field.get(obj);
		}
		catch (IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
	}

	void setFieldValue(Object obj, Field field, @Nullable Object value) {
		try {
			field.setAccessible(true);
			field.set(obj, value);
		}
		catch (IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
	}

}
