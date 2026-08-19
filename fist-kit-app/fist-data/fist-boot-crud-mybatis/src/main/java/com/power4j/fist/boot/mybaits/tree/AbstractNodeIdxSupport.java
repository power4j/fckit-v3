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

package com.power4j.fist.boot.mybaits.tree;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.power4j.fist.boot.mybaits.crud.repository.Repository;
import com.power4j.fist.data.tree.domain.NodeIdx;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 负责维护数据库中树形结构的节点关系
 *
 * @author CJ (power4j@outlook.com)
 * @date 2021/9/23
 * @since 1.0
 * @param <T> NodeIdx 的子类
 * @param <ID> ID 类型
 * @param <R> Repository
 */
public abstract class AbstractNodeIdxSupport<T extends NodeIdx<ID, T>, ID extends Serializable, R extends Repository<T, ?>>
		implements TreePathSupport<T, ID> {

	/**
	 * Link Repository
	 * @return Repository
	 */
	protected abstract R getRepository();

	/**
	 * 创建对象
	 * @param ancestor 祖先节点ID
	 * @param descendant 后代节点ID
	 * @param distance 层距离
	 * @return C
	 */
	protected abstract T createObject(ID ancestor, ID descendant, int distance);

	/**
	 * 创建一个指向自己的 Path
	 * @param id 节点ID
	 * @return C
	 */
	protected T createObject(ID id) {
		return createObject(id, id, 0);
	}

	@Override
	public long countAllDescendant(ID id, @Nullable Integer distanceMin, @Nullable Integer distanceMax) {
		// @formatter:off
		LambdaQueryWrapper<T> wrapper = getRepository().lambdaWrapper()
				.eq(T::getAncestor,id)
				.ge(Objects.nonNull(distanceMin), T::getDistance,  distanceMin)
				.le(Objects.nonNull(distanceMax), T::getDistance,  distanceMax);
		return getRepository().countBy(wrapper);
		// @formatter:on
	}

	@Override
	public List<T> findAllDescendant(ID id, @Nullable Integer distanceMin, @Nullable Integer distanceMax) {
		// @formatter:off
		LambdaQueryWrapper<T> wrapper = getRepository().lambdaWrapper()
				.eq(T::getAncestor,id)
				.ge(Objects.nonNull(distanceMin), T::getDistance,  distanceMin)
				.le(Objects.nonNull(distanceMax), T::getDistance,  distanceMax);
		return getRepository().findAllBy(wrapper);
		// @formatter:on
	}

	@Override
	public List<T> findAllDescendant(Collection<ID> ids, @Nullable Integer distanceMin, @Nullable Integer distanceMax) {
		// @formatter:off
		LambdaQueryWrapper<T> wrapper = getRepository().lambdaWrapper()
				.in(T::getAncestor,ids)
				.ge(Objects.nonNull(distanceMin), T::getDistance,  distanceMin)
				.le(Objects.nonNull(distanceMax), T::getDistance,  distanceMax);
		return getRepository().findAllBy(wrapper);
		// @formatter:on
	}

	@Override
	public long countAllAncestor(ID id, @Nullable Integer distanceMin, @Nullable Integer distanceMax) {
		// @formatter:off
		LambdaQueryWrapper<T> wrapper = getRepository().lambdaWrapper()
				.eq(T::getAncestor,id)
				.ge(Objects.nonNull(distanceMin), T::getDistance,  distanceMin)
				.le(Objects.nonNull(distanceMax), T::getDistance,  distanceMax);
		return getRepository().countBy(wrapper);
		// @formatter:on
	}

	@Override
	public List<T> findAllAncestor(ID id, @Nullable Integer distanceMin, @Nullable Integer distanceMax) {
		// @formatter:off
		LambdaQueryWrapper<T> wrapper = getRepository().lambdaWrapper()
				.eq(T::getDescendant,id)
				.ge(Objects.nonNull(distanceMin), T::getDistance,  distanceMin)
				.le(Objects.nonNull(distanceMax), T::getDistance,  distanceMax);
		return getRepository().findAllBy(wrapper);
		// @formatter:on
	}

	@Override
	public List<T> findAllAncestor(Collection<ID> ids, @Nullable Integer distanceMin, @Nullable Integer distanceMax) {
		// @formatter:off
		LambdaQueryWrapper<T> wrapper = getRepository().lambdaWrapper()
				.in(T::getDescendant,ids)
				.ge(Objects.nonNull(distanceMin), T::getDistance,  distanceMin)
				.le(Objects.nonNull(distanceMax), T::getDistance,  distanceMax);
		return getRepository().findAllBy(wrapper);
		// @formatter:on
	}

	@Override
	public List<T> getAll(@Nullable Integer distanceMin, @Nullable Integer distanceMax) {
		// @formatter:off
		LambdaQueryWrapper<T> wrapper = getRepository().lambdaWrapper()
				.ge(Objects.nonNull(distanceMin), T::getDistance,  distanceMin)
				.le(Objects.nonNull(distanceMax), T::getDistance,  distanceMax);
		return getRepository().findAllBy(wrapper);
		// @formatter:on
	}

	/**
	 * 删除节点关系,当节点删除后应该使用此方法
	 * @param id 节点ID,与该节点相关的所有路径会被删除
	 */
	@Override
	public void removeAllPath(ID id) {
		// @formatter:off
		LambdaQueryWrapper<T> wrapper = getRepository().lambdaWrapper()
				.eq(T::getAncestor,id)
				.or()
				.eq(T::getDescendant,id);
		getRepository().deleteAllBy(wrapper);
		// @formatter:on
	}

	/**
	 * 添加路径信息,新增节点时应该使用此方法
	 * @param newNode 新增节点的ID
	 * @param parent 父节点,可以为空
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void generatePath(ID newNode, @Nullable ID parent) {
		Assert.notNull(newNode, "new node can not null");
		// @formatter:off
		if(Objects.nonNull(parent)){
			List<T> pathList = findAllAncestor(parent,null,null)
					.stream().map(o -> createObject(o.getAncestor(),newNode,o.getDistance() + 1))
					.collect(Collectors.toList());

			getRepository().saveAll(pathList);
		}
		// @formatter:on
		getRepository().saveOne(createObject(newNode));
	}

	/**
	 * 移动节点及其子树，维护闭包关系。
	 * @param nodeId 待移动节点
	 * @param newParentId 新父节点；为空表示移动到顶层
	 * <p>
	 * 调用前提是闭包索引与节点主表的父子关系一致；该方法只维护一次移动产生的路径，不负责重建历史脏数据。
	 * @throws IllegalArgumentException 当节点或新父节点不存在，或新父节点属于待移动节点的子树时抛出
	 */
	@Transactional(rollbackFor = Exception.class)
	public void moveSubtree(ID nodeId, @Nullable ID newParentId) {
		Assert.notNull(nodeId, "node id can not null");

		List<T> subtreePaths = findAllDescendant(nodeId, null, null);
		Assert.isTrue(
				subtreePaths.stream()
					.anyMatch(path -> Objects.equals(path.getAncestor(), nodeId)
							&& Objects.equals(path.getDescendant(), nodeId) && Objects.equals(path.getDistance(), 0)),
				"node path not found");
		Set<ID> subtreeNodeIds = subtreePaths.stream().map(NodeIdx::getDescendant).collect(Collectors.toSet());
		Assert.isTrue(!subtreeNodeIds.contains(newParentId), "new parent can not be a descendant of node");

		Set<ID> oldAncestorIds = findAllAncestor(nodeId, 1, null).stream()
			.map(NodeIdx::getAncestor)
			.collect(Collectors.toSet());
		if (!oldAncestorIds.isEmpty() && !subtreeNodeIds.isEmpty()) {
			LambdaQueryWrapper<T> wrapper = getRepository().lambdaWrapper()
				.in(T::getAncestor, oldAncestorIds)
				.in(T::getDescendant, subtreeNodeIds);
			getRepository().deleteAllBy(wrapper);
		}

		if (Objects.nonNull(newParentId)) {
			List<T> newParentPaths = findAllAncestor(newParentId, null, null);
			Assert.isTrue(newParentPaths.stream()
				.anyMatch(path -> Objects.equals(path.getAncestor(), newParentId)
						&& Objects.equals(path.getDescendant(), newParentId) && Objects.equals(path.getDistance(), 0)),
					"new parent path not found");
			List<T> newPathList = newParentPaths.stream()
				.flatMap(ancestorPath -> subtreePaths.stream()
					.map(descendantPath -> createObject(ancestorPath.getAncestor(), descendantPath.getDescendant(),
							ancestorPath.getDistance() + 1 + descendantPath.getDistance())))
				.collect(Collectors.toList());
			getRepository().saveAll(newPathList);
		}
	}

	@Override
	public Set<ID> subTreeNodes(Collection<ID> ids) {
		if (ObjectUtils.isEmpty(ids)) {
			return Collections.emptySet();
		}
		List<T> paths = findAllDescendant(ids, null, null);
		// @formatter:off
		return paths.stream()
				.map(NodeIdx::getDescendant)
				.collect(Collectors.toSet());
		// @formatter:on
	}

	@Override
	public Set<ID> findSharedRoot(Collection<ID> ids) {
		if (ObjectUtils.isEmpty(ids)) {
			return Collections.emptySet();
		}
		List<T> paths = findAllAncestor(ids, null, null);
		Map<ID, List<T>> map = paths.stream().collect(Collectors.groupingBy(NodeIdx::getDescendant));
		Set<ID> roots = new HashSet<>(8);
		// @formatter:off
		map.values().forEach(list -> {
			list.stream().max(Comparator.comparing(NodeIdx::getDistance))
					.ifPresent(o -> roots.add(o.getAncestor()));
		});
		// @formatter:on
		return roots;
	}

	protected LambdaQueryWrapper<T> allEq(@Nullable Long ancestor, @Nullable Long descendant,
			@Nullable Integer distance) {
		// @formatter:off
		return getRepository().lambdaWrapper()
				.eq(Objects.nonNull(ancestor), T::getAncestor,ancestor)
				.eq(Objects.nonNull(descendant), T::getDescendant,  descendant)
				.eq(Objects.nonNull(distance), T::getDistance,  distance);
		// @formatter:on
	}

}
