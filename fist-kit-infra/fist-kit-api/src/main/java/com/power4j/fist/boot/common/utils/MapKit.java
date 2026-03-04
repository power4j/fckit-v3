package com.power4j.fist.boot.common.utils;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.apache.commons.lang3.reflect.Typed;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author CJ (power4j@outlook.com)
 * @date 2022/9/4
 * @since 1.0
 */
@UtilityClass
public class MapKit {

	/**
	 * 读取泛型类型,此方法不能安全转换泛型容器的元素
	 * @param map 数据源
	 * @param key 键
	 * @param typed 类型原语
	 * @return 值不存在或者是null返回empty
	 * @throws ClassCastException 值存在，但是类型无法转换
	 * @param <K>
	 * @param <T>
	 */
	@SuppressWarnings("unchecked")
	public <K, T> Optional<T> useValue(@Nullable Map<? extends K, ?> map, K key, Typed<T> typed) {
		// @formatter:off
		return Optional.ofNullable(map)
				.map(m -> m.get(key))
				.map(value -> {
					final Type type = typed.getType();
					if(!TypeUtils.isInstance(value,type)){
						String msg = String.format("Can not cast %s to %s", value.getClass().getName(),type.getTypeName());
						throw new ClassCastException(msg);
					}
					return (T)value;
				});
		// @formatter:on
	}

	/**
	 * 读取为 List类型
	 * @param map 数据源
	 * @param key 键
	 * @param mapper 转换器
	 * @return 以下情况返回只读空集合: 值不存在,值为null;如果值不是集合类型也不是数组,则将其放入集合并返回
	 * @param <K>
	 * @param <V>
	 * @param <R>
	 */
	public <K, V, R> List<R> readAsList(@Nullable Map<K, V> map, K key, Function<Object, R> mapper) {
		// @formatter:off
		return Optional.ofNullable(map)
				.map(m -> m.get(key))
				.map(value -> {
					if(value instanceof Collection){
						return ((Collection<?>)value).stream().map(mapper).collect(Collectors.toList());
					}
					if(value.getClass().isArray()){
						int length = Array.getLength(value);
						List<R> list = new ArrayList<>(length);
						for (int i = 0; i < length; i ++) {
							Object arrayElement = Array.get(value, i);
							list.add(mapper.apply(arrayElement));
						}
						return list;
					}
					return Collections.singletonList(mapper.apply(value));
				})
				.orElseGet(Collections::emptyList);
		// @formatter:on
	}

	/**
	 * 读取为 Set 类型
	 * @param map 数据源
	 * @param key 键
	 * @param mapper 转换器
	 * @return 以下情况返回只读空集合: 值不存在,值为null;如果值不是集合类型也不是数组,则将其放入集合并返回
	 * @param <K>
	 * @param <V>
	 * @param <R>
	 */
	public <K, V, R> Set<R> readAsSet(@Nullable Map<K, V> map, K key, Function<Object, R> mapper) {
		return new HashSet<>(readAsList(map, key, mapper));
	}

}
