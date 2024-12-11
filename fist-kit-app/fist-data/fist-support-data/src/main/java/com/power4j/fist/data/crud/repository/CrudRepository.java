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

package com.power4j.fist.data.crud.repository;

import java.util.List;
import java.util.Optional;

/**
 * @author CJ (power4j@outlook.com)
 * @date 2021/9/18
 * @since 1.0
 * @param <T> 实体类型
 * @param <ID> 主键类型
 */
public interface CrudRepository<T, ID> {

	/**
	 * 保存
	 * @param entity 实体
	 * @return 返回保存后的实体
	 */
	T saveOne(T entity);

	/**
	 * 批量保存
	 * @param entities 实体集合
	 * @return 返回保存后的实体集合
	 */
	List<T> saveAll(Iterable<T> entities);

	/**
	 * 批量保存
	 * @param entities 实体集合
	 * @param batchSize 批量大小
	 * @return 返回保存后的实体集合
	 */
	List<T> saveAll(Iterable<T> entities, int batchSize);

	/**
	 * 更新(根据ID)
	 * @param entity 实体
	 * @return 返回更新后的实体
	 */
	T updateOneById(T entity);

	/**
	 * 批量更新(根据ID)
	 * @param entities 实体集合
	 * @return 返回更新后的实体集合
	 */
	List<T> updateAllById(Iterable<T> entities);

	/**
	 * 查询(根据ID)
	 * @param id ID
	 * @return Optional
	 */
	Optional<T> findOneById(ID id);

	/**
	 * 查询是否存在(根据ID)
	 * @param id ID
	 * @return true 表示存在
	 */
	boolean existsById(ID id);

	/**
	 * 查询全部,<b>大表请注意！</b>
	 * @return 实体集合
	 */
	List<T> findAll();

	/**
	 * 批量查询(根据ID)
	 * @param ids ID集合
	 * @return 返回实体集合
	 */
	List<T> findAllById(Iterable<ID> ids);

	/**
	 * 统计数量
	 * @return 返回全部实体的数量
	 */
	long countAll();

	/**
	 * 删除(根据ID)
	 * @param id ID
	 */
	void deleteOneById(ID id);

	/**
	 * 批量删除(根据ID)
	 * @param ids ID集合
	 */
	void deleteAllById(Iterable<? extends ID> ids);

	/**
	 * 全部删除,<b>请谨慎使用！</b>
	 */
	void deleteAll();

}
