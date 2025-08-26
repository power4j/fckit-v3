# MyBatis/MyBatisPlus 的扩展

## SignFieldMetaObjectHandler

> 用于对签名列的自动填充

### 使用方法
第一步,实现SignFieldMetaObjectHandler

在实体类注解

```java
public class UserEntity {

    private String name;

    private String role;

    @SignField(signer = MockSigner.class, when = { SignField.When.Insert })
    private String signValue;

}
```
