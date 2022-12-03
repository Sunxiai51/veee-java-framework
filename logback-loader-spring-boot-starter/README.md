# logback-loader-spring-boot-starter

用于logback配置加载。

## Feature

- 支持启动时静态加载日志配置-从本地日志配置文件
- 支持启动时静态加载日志配置-从apollo指定的xml配置
- 支持启动时静态加载日志配置-从apollo指定namespace的指定key

## Usage

### Step 1. 引入依赖

```xml

<dependency>
    <groupId>com.sunveee.framework</groupId>
    <artifactId>logback-loader-spring-boot-starter</artifactId>
</dependency>
```

### Step 2. 调用方法

```java
        public static void main(String[]args){
        LogbackConfigInitializer.initialize();
        SpringApplication.run(Xxx.class,args);
        }
```

### Step 3. 设置环境变量

#### 通用变量

```properties
-Dlogbackloader.enabled=true # 必须，设置为true以启用
-Dlogbackloader.initialize.name=<log name> # 当需要替换placeholder时必须，指定要替换的值
-Dlogbackloader.initialize.name.placeholder=_REPLACE_LOG_NAME_HERE_ # 当需要替换placeholder时必须，指定占位符
```

#### 从本地日志配置文件加载

```properties
-Dlogbackloader.initialize.type=LOCAL_FILE # 必须，指定加载类型为本地文件
-Dlogbackloader.initialize.localfile.path=<your logback config file path> # 必须，指定日志配置文件绝对路径
```

#### 从apollo指定的xml配置加载

```properties
-Dlogbackloader.initialize.type=APOLLO_XML_CONFIG # 必须，指定加载类型为apolloXml
-Dlogbackloader.initialize.apollo.xml.namespace=logback-config # 非必须，指定apolloNamespace，默认值为"logback-config"
```

#### 从apollo指定namespace的指定key加载

```properties
-Dlogbackloader.initialize.type=APOLLO_KV_CONFIG # 必须，指定加载类型为apolloKeyValue
-Dlogbackloader.initialize.apollo.kv.namespace=common-logback-config # 非必须，指定apolloNamespace，默认值为"common-logback-config"
-Dlogbackloader.initialize.apollo.kv.key=default # 非必须，指定apolloKey，默认值为"default"
```

#### 从nacos指定namespace加载

```properties
-Dlogbackloader.initialize.type=NACOS_CONFIG # 必须，指定加载类型为Nacos
-Dlogbackloader.initialize.nacos.server-addr=127.0.0.1:80 # 必须，指定nacos连接地址
-Dlogbackloader.initialize.nacos.namespace=da98b559-8cea-476e-9533-03d322a8b281 # 必须，指定配置所在的namespace
-Dlogbackloader.initialize.nacos.data-id=common-logback-config # 非必须，默认值common-logback-config
-Dlogbackloader.initialize.nacos.group=COMMON_GROUP # 非必须，默认值COMMON_GROUP
```
