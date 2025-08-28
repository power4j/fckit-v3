# MyBatis/MyBatisPlus 的扩展

## MetaHandlerCompose 自动填充

是对原生`MetaObjectHandler`的扩展,优点:
- 基于`@FillWith`注解实现不同的字段由不同的类来填充
- 兼容原生注解:只有`@TableField(fill = XXX)` 但是没有`@FillWith`

核心原理:
- `@FillWith` 声明填充行为
- `ValueSupplier` 负责提供填充值
- `ValueSupplierResolver` 负责查找`ValueSupplier`

### 使用方法(示例)

ValueHandler

```java
public static class CountSupplier implements ValueHandler {

    private final AtomicInteger count;

    public CountHandler(int initValue) {
        this.count = new AtomicInteger(initValue);
    }

    @Override
    public Object getValue(Object root, String fieldName, Class<?> fieldType) {
        return String.valueOf(count.getAndIncrement());
    }

    public int getCount() {
        return count.get();
    }

}
```

实体

```java
public class MyEntity {

    // 注意,需要和 @TableField(fill = XXX) 搭配使用
    @FillWith(supplier = CountSupplier.class, order = FillWith.LOWEST_ORDER)
    @TableField(fill = FieldFill.INSERT)
    private String insertMeta;

    @FillWith(supplier = CountSupplier.class, order = FillWith.LOWEST_ORDER)
    @TableField(fill = FieldFill.UPDATE)
    private String updateMeta;

    // 没有写@FillWith 则使用globalHandler
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String allMeta;

}
```

### 自定义配置参考(Spring)

```java
@Configuration
@ConditionalOnClass(MybatisConfiguration.class)
public class MybatisAutoConfiguration implements ApplicationContextAware {

	@Nullable
	private ApplicationContext applicationContext;

	@Bean
	public ValueSupplierResolver valueSupplierResolver() {
		if (applicationContext == null) {
			throw new IllegalStateException("ApplicationContext is null");
		}
        // 使用Spring的Bean工厂查找 ValueSupplier
		return new ValueSupplierBeanResolver(applicationContext);
	}

	@Bean
	public MetaHandlerCompose metaHandlerCompose(ValueSupplierResolver resolver) {
        // globalHandler的用处: 兼容原生注解,也就是写了@TableField(fill = XXX) 但是没有@FillWith
        ValueSupplier globalHandler = ...;
		return new MetaHandlerCompose(resolver, globalHandler);
	}
    @Bean
    public ValueSupplierResolver valueSupplierResolver() {
        if (applicationContext == null) {
            throw new IllegalStateException("ApplicationContext is null");
        }
        return new ValueSupplierBeanResolver(applicationContext);
    }

}
```

### 迁移

如果你的项目原来用了MetaObjectHandler,现在只需要将相关处理逻辑放到新的`globalHandler`(MetaHandlerCompose的可选参数)就能完美兼容老项目
