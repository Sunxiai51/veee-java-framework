# grpc-spring-boot-starter

集成 `net.devh:grpc-spring-boot-starter`，同时添加一些自定义的功能。

> 请参考： [Github Page](https://github.com/yidongnan/grpc-spring-boot-starter)

## Feature

- 引入grpc-spring-boot-starter
- 提供拦截器，用于统一的日志打印、错误处理等

## Restriction

- 仅支持springboot项目，验证过的springboot版本为`2.2.1.RELEASE`

## Usage

### Maven依赖引入

```xml

<dependency>
    <groupId>com.sunveee.framework</groupId>
    <artifactId>grpc-spring-boot-starter</artifactId>
</dependency>
```

### 按需配置

```properties
# 启用，必选
veee.grpc.starter.enabled=true
# 服务端
veee.grpc.starter.server-interceptor.verbose=false
veee.grpc.starter.server-interceptor.print-request-message=true
# 客户端
veee.grpc.starter.client-interceptor.name=${spring.application.name}
veee.grpc.starter.client-interceptor.verbose=false
veee.grpc.starter.client-interceptor.print-request-message=true
veee.grpc.starter.client-interceptor.print-response-message=true
veee.grpc.starter.client-interceptor.print-response-header=false

```

```java

@Configuration
@ConditionalOnProperty(GrpcStarterProperties.GRPC_STARTER_ENABLED)
@EnableConfigurationProperties(GrpcStarterProperties.class)
public class GrpcInterceptorConfig {

    @GrpcGlobalServerInterceptor
    @ConditionalOnMissingBean
    public GrpcServerInterceptor grpcServerInterceptor(GrpcStarterProperties properties) {
        return new GrpcServerInterceptor(properties.getServerInterceptor());
    }

    @GrpcGlobalClientInterceptor
    @ConditionalOnMissingBean
    public GrpcClientInterceptor grpcClientInterceptor(GrpcStarterProperties properties) {
        return new GrpcClientInterceptor(properties.getClientInterceptor());
    }


}
```

## Reference

> [Docs](https://yidongnan.github.io/grpc-spring-boot-starter/)

