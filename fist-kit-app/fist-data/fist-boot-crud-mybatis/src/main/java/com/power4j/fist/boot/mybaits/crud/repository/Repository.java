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
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.power4j.fist.boot.mybaits.crud.repository.matcher.Eq;
import com.power4j.fist.data.crud.repository.PagingAndSortingRepository;
import com.power4j.fist.data.domain.Pageable;
import com.power4j.fist.data.domain.Paged;
import org.springframework.lang.Nullable;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/**
 * @author CJ (power4j@outlook.com)
 * @date 2021/9/18
 * @since 1.0
 * @param <T> 实体类型
 * @param <ID> 主键类型
 */
public interface Repository<T, ID extends Serializable> extends PagingAndSortingRepository<T, ID> {

	int DEFAULT_BATCH_SIZE = 1_000;

	/**
	 * 批量保存
	 * @param entities 实体对象
	 * @param batchSize 批处理大小
	 * @return 返回记录列表
	 */
	List<T> saveAll(Iterable<T> entities, int batchSize);

	/**
	 * 批量更新
	 * @param entities 实体对象
	 * @param batchSize 批处理大小
	 * @return 返回记录列表
	 */
	List<T> updateAllById(Iterable<T> entities, int batchSize);

	/**
	 * 查询单个
	 * @param queryWrapper 查询条件
	 * @return 返回单个记录
	 */
	Optional<T> findOneBy(@Nullable Wrapper<T> queryWrapper);

	/**
	 * 查询集合
	 * @param queryWrapper 查询条件
	 * @return 返回记录集
	 */
	List<T> findAllBy(@Nullable Wrapper<T> queryWrapper);

	/**
	 * 分页查询
	 * @param pageable 页信息
	 * @param queryWrapper 查询条件
	 * @return 返回分页数据
	 */
	Paged<T> findAllBy(Pageable pageable, @Nullable Wrapper<T> queryWrapper);

	/**
	 * 删除
	 * @param queryWrapper 查询条件
	 * @return 返回记录条数
	 */
	long deleteAllBy(@Nullable Wrapper<T> queryWrapper);

	/**
	 * 统计
	 * @param queryWrapper 查询条件
	 * @return 返回记录条数
	 */
	long countBy(@Nullable Wrapper<T> queryWrapper);

	/**
	 * 执行自定义查询
	 * @return 返回一个新的LambdaQueryWrapper实例
	 */
	LambdaQueryChainWrapper<T> querying();

	/**
	 * 执行自定义更新
	 * @return 返回一个新的LambdaQueryWrapper实例
	 */
	LambdaUpdateChainWrapper<T> updating();

	/**
	 * 自定义查询条件
	 * @return 返回一个新的LambdaQueryWrapper实例
	 */
	LambdaQueryWrapper<T> lambdaWrapper();

	/**
	 * 根据某个字段进行统计
	 * @param expr 匹配字段
	 * @param exclude 排除的ID
	 * @return 记录行数
	 */
	long lambdaCount(Eq<T> expr, @Nullable ID exclude);

	/**
	 * 根据某些字段进行统计
	 * @param expr 匹配字段
	 * @param exclude 排除的ID
	 * @return 记录行数
	 */
	long lambdaCount(List<Eq<T>> expr, @Nullable ID exclude);

	/**
	 * 拉取乐观锁版本号,此方法主要用于乐观锁辅助
	 * <ul>
	 * <li>数据库中没有该记录时,忽略</li>
	 * </ul>
	 * @param entity 被更新实体对象
	 * @param required 版本号空值(null)检查,注意:空值会导致乐观锁机制失效
	 * @return 返回传入的实体,将它的版本号会更新位当前数据库中的值
	 * @throws IllegalStateException 数据库中的版本号是空值,并且required参数为true
	 * @throws IllegalStateException 实体类型没有乐观锁字段
	 * @see com.baomidou.mybatisplus.annotation.Version
	 */
	T fetchVersion(T entity, boolean required);

}
