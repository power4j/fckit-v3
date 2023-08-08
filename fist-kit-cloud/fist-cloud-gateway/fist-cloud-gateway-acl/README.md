网关鉴权插件,网关扮演资源服务服务器角色

## 使用方式

### 依赖

```XML
  <dependencies>
    <dependency>
      <groupId>org.springframework.cloud</groupId>
      <artifactId>spring-cloud-starter-gateway</artifactId>
    </dependency>
    <dependency>
      <groupId>org.springframework.cloud</groupId>
      <artifactId>spring-cloud-starter-loadbalancer</artifactId>
    </dependency>
    <dependency>
      <groupId>com.power4j.fist3</groupId>
      <artifactId>fist-kit-cloud-gateway-resource-server</artifactId>
    </dependency>
  </dependencies>
```


### 代码

1. 实现 `ReactivePermissionDefinitionService`服务
2. 添加`@EnableRouteGuard` 注解


### 配置


```YAML
fist:
  authorization.global:
    # 跳过鉴权的url,注意这里是原始入站url
    skip:
      - /fauth/oauth/**
      - /actuator/**
    filters:
      # 关闭租户鉴权
      tenant.enabled: false
    # 安全模式,打开后所有API都有权访问,可以配合白名单使用  
    safe-mode:
      enabled: false
      whitelist: 127.*,192.*,172.*
  # 基于 oauth2 来读取用户信息    
  oauth2:
    resourceserver:
      opaque-token:
        introspection-uri: "lb://fist-auth/v1/token/auth-user/details"
        client-id: client
        client-secret: 123456  

# Spring Cloud Gateway 的路由信息
spring:
  cloud:
    gateway:
      routes:
        # 
        - id: demo
          uri: lb://demo-service
          predicates:
            - Path=/demo/**

```
