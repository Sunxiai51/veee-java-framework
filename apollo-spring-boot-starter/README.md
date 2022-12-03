# apollo-spring-boot-starter

引入apollo客户端。



## Feature

由于`1.7.0`及以上版本的apollo-client，本身包含了`spring.factories`，提供了基于spring-boot的接入方式，故该工程仅是简单的将其依赖引入，相关特性与详细配置请参考[Apollo官方文档 - Java客户端使用指南](https://github.com/ctripcorp/apollo/wiki/Java客户端使用指南)。



## Restriction

- 仅支持springboot项目，验证过的springboot版本为`2.2.1.RELEASE`
- 验证过的apollo客户端版本为`1.7.0`



## Usage

为了同时支持生产体验版部署Apollo场景和赋能的无Apollo场景，达到一套代码多场景部署的目的，在`2.0.0-SNAPSHOT`及之后的版本，对Apollo客户端的集成方式进行以下约定：

- 应该通过服务启动参数控制是否开启Apollo，当`-Dapollo.bootstrap.enabled=true`时表示启用apollo配置中心，`-Dapollo.bootstrap.enabled=false`时表示不启用apollo配置中心
- 在启用apollo时，需要通过启动参数同时指定apollo相关配置，例如`-Dapollo.meta`、`-Dapp.id`等配置，不启用apollo则无需指定这些配置。详细的配置列表见下文。
- 由于应用访问的namespace与代码强相关，相关内容由开发人员维护在代码中，在classpath下使用`bootstrap.properties`文件指定`apollo.bootstrap.namespaces`。该配置支持以bootstrap.properties或application.properties文件指定，为了避免在无apollo场景占用application.properties文件名，建议使用bootstrap.properties作为该配置文件名。

以下为详细的接入步骤：



### 1. 引入依赖

```xml
<dependency>
    <groupId>com.sunveee.framework</groupId>
    <artifactId>apollo-spring-boot-starter</artifactId>
</dependency>
```



### 2. 指定Namespace

在管理页面创建好相关的应用与Namespace后，可以通过以下方式指定本应用需要访问的Namespace：

在classpath下新增`bootstrap.properties`文件：

```properties
apollo.bootstrap.namespaces=application,config,logback-config.xml
```

> 注意：`.properties`类型的Namespace可以省略后缀，但其余格式的namespace均不能省略后缀，例如"logback-config.xml"如果写为"logback-config"将无法拉取。



### 3. 获取配置

使用`@Value`等注解读取配置，相关支持的注解请参考Apollo官方文档



### 4. 启动项目

启动时需要配置Apollo相关属性，包括以下参数（以启动参数的方式为例）：

```properties
## 必须配置的参数
-Dapp.id=xxx
-Dapollo.cacheDir=/opt/data
-Denv=OFFICE 
-Dapollo.meta=http://xxx/config 
-Dapollo.bootstrap.enabled=true 

## 可选参数（建议添加）
-Dapollo.bootstrap.eagerLoad.enabled=true
```

