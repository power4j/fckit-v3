# MyBatis/MyBatisPlus 的扩展

## 自动填充

> 用于对签名列的自动填充

### 使用方法

第一步,实现你自己的 ValueHandler

第二步,在实体类注解

```java
public class MyEntity {

    @FillWith(handler = CountHandler.class, order = FillWith.LOWEST_ORDER)
    @TableField(fill = FieldFill.INSERT)
    private String insertMeta;

    @FillWith(handler = CountHandler.class, order = FillWith.LOWEST_ORDER)
    @TableField(fill = FieldFill.UPDATE)
    private String updateMeta;

    @FillWith(handler = CountHandler.class)
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String allMeta;

}
```
