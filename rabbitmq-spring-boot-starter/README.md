# rabbitmq-spring-boot-starter

针对RabbitMQ的两种具体使用场景（同步、异步），封装组件以提供相应的特性。

场景说明：

- 同步场景（RPC）

  类似于RPC远程过程调用的方式，生产者发送请求后阻塞等待消费者的响应。

- 异步场景（Simple）

  一般的异步消息发送与接收方式，生产者发送消息后立即返回，消费者异步消费消息。



## Feature

### RPC场景

- 提供`RPCRabbitTemplate`用于消息发送
  - 支持简单文本交互，提供通用API
  - 基于简单文本交互，支持common-rpc-model所定义的交互模型，提供通用API
  - 支持超时时间设置及超时处理逻辑（仅抛出异常）
  - 日志输出请求响应报文
- 提供`RPCSimpleErrorHandler`和`RPCBizErrorHandler`进行消费端全局的异常捕获与处理
- 提供切面进行消费端日志打印与耗时计算
- 使用common-rpc-model交互模型的场景，提供切面拦截消费端过期消息



### Simple场景

- 提供`SimpleRabbitTemplate`用于消息发送
  - 支持发送简单文本，提供通用API
  - 基于简单文本，支持发送对象（封装json转换），提供通用API
  - 开启消息return机制，并提供return callback处理逻辑（仅打印日志）
  - 开启消息confirm机制，并提供confirm callback处理逻辑（仅打印日志）
  - 支持发送超时时的同步阻塞重试，并支持指定重试次数与重试间隔
  - 日志输出消息发送记录
- 提供消费失败后自动重试的通用方案
- 提供切面进行消费端日志打印与耗时计算





## Usage

> 完整示例可参考rabbitmq-spring-boot-starter-example



### Maven依赖引入

```xml
<dependency>
    <groupId>com.sunveee.framework</groupId>
    <artifactId>rabbitmq-spring-boot-starter</artifactId>
</dependency>
```



### RPC场景

#### 生产端

依赖注入：

```java
@Autowired
private RPCRabbitTemplate rpcRabbitTemplate;
```

简单文本请求：

```java
String response = rpcRabbitTemplate.rpcInvoke("queue_test_rpc_string", content);
```

common-rpc-model请求：

```java
BizResponse<MyResponseData> response = rpcRabbitTemplate.rpcBizInvoke("queue_test_rpc_biz", request, MyResponseData.class);
```



#### 消费端

简单文本响应：

```java
@RabbitListener(queuesToDeclare = @Queue("queue_test_rpc_string"), errorHandler = "rpcSimpleErrorHandler")
public String receiveString(String content) {
    // 返回
    return "handle result ### " + content;
}
```

common-rpc-model响应：

```java
@RabbitListener(queuesToDeclare = @Queue("queue_test_rpc_biz"), errorHandler = "rpcBizErrorHandler")
public String receiveBizRequest(String content) {
    // 解析请求
    BizRequest<MyRequestParam> request = BizRequestBuilder.fromJson(content, MyRequestParam.class);
    // 返回
    MyResponseData data = new MyResponseData();
    return JSON.toJSONString(BizResponseBuilder.buildSuccessResponse(data));
}
```



### Simple场景

#### 生产端

依赖注入：

```java
@Autowired
private SimpleRabbitTemplate simpleRabbitTemplate;
```

发送消息：

```java
simpleRabbitTemplate.simpleSend(exchange, routingKey, message);
```

#### 消费端

```java
@RabbitListener(queuesToDeclare = @Queue("queue_test_simple"), containerFactory = "simpleConsumerContainerFactory")
public void simpleReceive(String content) {
	// 处理消息
}
```



## Reference

### 如何提供可靠性支持

#### RPC场景

##### RPC异常场景描述

RPC场景中，客户端发出调用后将阻塞当前线程并同步获取来自服务端的响应，响应是否成功由客户端判断，通常来说有以下几种响应结果：

a. 明确成功：客户端收到了来自服务端的响应，且根据约定的协议，响应内容被解析为处理成功

b. 明确失败：客户端收到了来自服务端的响应，且根据约定的协议，响应内容被解析为处理失败

c. 请求失败或超时：客户端未收到来自服务端的响应

其中，请求失败或超时在rabbitmq的同步交互场景中又有以下几种可能：

c1. 客户端发送请求消息到broker失败

c2. 客户端发送请求消息到broker成功，但无服务端响应（消息未被路由到任何一个队列）

c3. 客户端发送请求消息到broker成功，消息被分发至服务端队列，但指定时间内未收到服务端响应

> 对于RPC的交互场景，服务端消费失败或reply失败不再单独处理，它们的表现均会导致客户端等待超时，统一纳入c3由客户端处理。

##### 轮子如何处理RPC异常

本轮子仅封装底层交互，不关注具体业务，a、b异常场景由业务层处理，这里仅讨论c类场景下的可靠性措施。

生产端RPCRabbitTemplate将开启强制发送，并设置响应超时：

```java
this.setMandatory(true);
this.setReplyTimeout(rpcProperties.getReplyTimeout());
```

重写超时方法，发生超时时抛出异常：

```java
@Override
protected void replyTimedOut(String correlationId) {
    log.warn("Timeout of correlationId: {}.", correlationId);
    throw new ReplyTimeoutException(rpcProperties.getReplyTimeout(), correlationId);
}
```

发送消息时，使用RabbitTemplate中的convertSendAndReceive方法：

```java
String response = (String) this.convertSendAndReceive(routingKey, content);
```

c1场景，例如无法与rabbitmq建立连接时，convertSendAndReceive方法会抛出对应的AmqpException。

c2场景，例如队列未创建时，在设置`mandatory`为`true`的情况下，convertSendAndReceive会在消息无法路由到queue时捕获到消息return事件，并抛出AmqpMessageReturnedException异常。

c3场景，例如消费端消费超时时，将会抛出ReplyTimeoutException异常。

##### RPC异常场景总结与一致性保证

在RPC场景中，只是简单地将对应场景的异常抛出，交给上游自行处理。

在请求超时时，抛出ReplyTimeoutException，在其它异常场景会抛出spring的AmqpException，所以客户端在使用RPCRabbitTemplate的api发送消息时，应该注意这一点，如果需要处理异常可以自行catch对应的Exception。

类似于其它RPC机制，在一次请求过程中发生c3时，客户端无法明确知晓服务端是否执行了预期执行的逻辑或执行结果，相关需求一般有以下两种实现方案：

- 服务端接口设计为可重入的（或幂等的），客户端在发生异常后重复请求直至获取到明确结果
- 服务端提供额外的查询接口供客户端查询执行情况



#### Simple场景

##### Simple异常场景

Simple场景中，生产端与消费端完全解耦，需要分别考虑生产端消息发送异常与消费端消费异常。

生产端消息发送，认为将消息发送至broker，且路由至至少一个队列时，即认为消息发送成功。可能有以下异常场景：

a. 客户端发送消息到broker失败

b. 客户端发送消息到broker成功，但无服务端响应（消息未被路由到任何一个队列）

消费端消费，认为消费逻辑执行完成不抛出异常即认为消息消费成功。可能有以下异常场景：

c. 消费端消费逻辑异常

d. 消费端消费过程中连接断开

##### 如何处理Simple异常

使用RabbitMQ的消息确认机制来保证消息发送与消费的可靠性。

###### 生产端消息发送

生产端SimpleRabbitTemplate将开启强制发送，并设置return与confirm的回调：

```java
public SimpleRabbitTemplate(SimpleCachingConnectionFactory connectionFactory, SimpleProducerProperties simpleProperties) {
    super(connectionFactory);
    this.globalProperties = simpleProperties;
    // 消息return机制
    this.setMandatory(true); // 开启
    this.setReturnCallback(new SimpleReturnCallback()); // 设置returnCallback

    // 消息confirm机制
    this.setConfirmCallback(new SimpleConfirmCallback()); // 设置confirmCallback

}
```

目前，这里回调时的处理逻辑只是简单地进行日志打印。

发送消息api进行了一些重载，最终都会调用simpleSend方法，可以看到这个方法调用了executeSend方法，并对超时异常执行了处理：

```java
public void simpleSend(String exchange, String routingKey, String message, SimpleProducerSendConfig sendConfig)
    throws MessageReturnedException, PublisherConfirmFailException, SendTimeoutException {
	try {
        executeSend(exchange, routingKey, message, sendProperties);
    } catch (InterruptedException | ExecutionException e) {
        throw new RuntimeException("Exception occured when waiting ack.", e);
    } catch (TimeoutException e) {
        handleTimeout(exchange, routingKey, message, sendProperties);
    }
}
```

这里的超时异常将在executeSend方法中抛出，是因为executeSend方法中在发送消息时，通过设置correlationId，使用CorrelationData来接收confirm与return信息：

```java
private void executeSend(String exchange, String routingKey, String message, SimpleProducerProperties sendProperties) throws InterruptedException, ExecutionException, TimeoutException {
    final long sendTimeout = actualSendTimeoutValue(sendProperties);

    // 使用CorrelationData接收confirm与return信息
    CorrelationData cd = new CorrelationData();
    cd.setId(generateCorrelationId()); // 每次发送消息创建唯一的correlationId
    this.convertAndSend(exchange, routingKey, message, cd);

    Confirm confirm = cd.getFuture().get(sendTimeout, TimeUnit.MILLISECONDS);
    if (confirm.isAck()) {
        handleAckTrue(exchange, routingKey, message, sendProperties, cd);
    } else {
        handleAckFalse(confirm, exchange, routingKey, message, sendProperties);
    }
}
```

当在指定的时间内未能ack时，`cd.getFuture().get()`将会抛出超时异常，进入handleTimeout的超时处理逻辑。handleTimeout将根据配置的重试次数与间隔在当前线程进行重试。如未配置重试，或所有的重试均超时，则最终会抛出SendTimeoutException。

如果消息在指定时间内ack `true`，进入handleAckTrue方法，判断是否发生消息return，如果发生将抛出MessageReturnedException，如果未发生消息return则视为消息发送成功。

如果消息在指定时间内ack `false`，进入handleAckFalse方法，直接抛出PublisherConfirmFailException。

###### 消费端消费

定义SimpleConsumerContainerFactory用于消息消费，设置ack模式为AUTO，并关闭requeue：

```java
public SimpleConsumerContainerFactory(SimpleRabbitListenerContainerFactoryConfigurer configurer, ConnectionFactory connectionFactory) {
    super();
    configurer.configure(this, connectionFactory);
    this.setAcknowledgeMode(AcknowledgeMode.AUTO); // 根据执行过程是否抛出异常判断是否ack
    this.setDefaultRequeueRejected(false); // 开启消息重试时不开启requeue
}
```

定义相关死信队列与MessageRecoverer，这样，在消费时发生异常时将根据配置在消费端内进行重试，重试结束后如果仍然无法成功消费，消息将进入死信队列。



