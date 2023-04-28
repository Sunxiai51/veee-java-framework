# keycloak-oidc-spring-boot-starter

集成Keycloak OIDC（OpenID Connect）的spring-boot-starter。

> 请参考： [Keycloak - Securing Applications and Services Guide](https://www.keycloak.org/docs/latest/securing_apps/#overview)



## Feature

- 引入keycloak-spring-boot-starter
- 提供工具类根据当前Token获取用户信息（用户id、角色、用户组等）



## Restriction

- 仅支持springboot项目，验证过的springboot版本为`2.2.1.RELEASE`



## Usage

> 完整示例可参考keycloak-oidc-spring-boot-starter-example

### Maven依赖引入

```xml
<dependency>
    <groupId>com.sunveee.framework</groupId>
    <artifactId>keycloak-oidc-spring-boot-starter</artifactId>
</dependency>
```

### 按需配置

> 配置项含义参考：[Java Adapter Config](https://www.keycloak.org/docs/latest/securing_apps/#_java_adapter_config)

```properties
keycloak.realm = framework-example
keycloak.resource = client-example
keycloak.credentials.secret = f1c2bab1-4485-4601-a0ab-f732b8bb17d6
keycloak.auth-server-url = ${your_url}
keycloak.ssl-required = none
#keycloak.enable-cors = true
keycloak.autodetect-bearer-only = true
keycloak.use-resource-role-mappings = true
keycloak.disable-trust-manager = true
keycloak.allow-any-hostname = true
keycloak.always-refresh-token = false

keycloak.securityConstraints[0].authRoles[0] = super_admin
keycloak.securityConstraints[0].securityCollections[0].name = super_admin
keycloak.securityConstraints[0].securityCollections[0].patterns[0] = /admin/*
```

### 获取当前用户信息

示例代码：

```java
@RequestMapping("/ping")
public String ping() {
    KeycloakTokenDetail keycloakTokenDetail = KeycloakTokenParser.keycloakTokenDetail("groups");
    log.info("userId: {}.", keycloakTokenDetail.getUserId());
    log.info("用户角色: {}.", keycloakTokenDetail.getRealmUserRoles());
    log.info("用户所在的用户组: {}.", keycloakTokenDetail.getRealmUserGroups());
    log.info("用户所关联的客户端角色: {}.", keycloakTokenDetail.getClientUserRolesMap());
    log.info("Access Token: {}.", keycloakTokenDetail.getAccessTokenString());
    log.info("解码后的token信息: {}.", JSONUtils.toJSONString(keycloakTokenDetail.getAccessToken()));
    return "Call /admin/ping success.";
}
```

请注意：使用`KeycloakTokenParser.keycloakTokenDetail()`方法获取用户信息时，需要传入一个入参`groupsTokenClaimName`，这个入参用于从accessToken中获取用户组信息，它的值对应于Keycloak中client所配置的Mapper。



## Reference

> [Keycloak docs](https://www.keycloak.org/docs/latest/)

