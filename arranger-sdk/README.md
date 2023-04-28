# arranger-sdk

工作流引擎，以SDK方式提供基于消息驱动的工作流功能。



## Feature

- 提供工作流引擎基础特性，支持任务的**创建**、**启动**、**暂停**、**继续**、**中止**，及任务与步骤的查询
- 支持任务的**推进**（异常场景下的恢复）



## Restriction

- 仅支持springboot项目，验证过的springboot版本为`2.2.1.RELEASE`
- 大量使用了数据库事务进行并发控制，MySQL应该设置为支持事务的，且应设置为读已提交（rc）或更严格的事务隔离级别



## Usage

> 完整示例可参考arranger-sdk-example

### Maven依赖引入

> arranger基于RabbitMQ与MySQL提供服务，使用arranger的服务除引入SDK外还需要自行引入spring-boot-starter-data-jpa与rabbitmq-spring-boot-starter。

```xml
<dependency>
    <groupId>com.sunveee.framework</groupId>
    <artifactId>arranger-sdk</artifactId>
</dependency>
```

引入SDK的服务还需要引入以下包：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>com.sunveee.framework</groupId>
    <artifactId>rabbitmq-spring-boot-starter</artifactId>
</dependency>
```

### SQL执行（可选）

> 虽然SDK中使用了spring-data-jpa，能够自动创建数据库表，但尚无法保证字段顺序，可能会给运维带来不便。实际情况中可以手动执行以下SQL来创建数据库表。

```sql
CREATE TABLE `task` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `task_id` varchar(32) COLLATE utf8mb4_bin NOT NULL COMMENT '任务唯一标识',
  `type` varchar(16) COLLATE utf8mb4_bin NOT NULL COMMENT '任务类型',
  `exec_status` varchar(16) COLLATE utf8mb4_bin NOT NULL COMMENT '任务执行状态',
  `start_time` datetime DEFAULT NULL COMMENT '任务开始时间',
  `finish_time` datetime DEFAULT NULL COMMENT '任务完成时间',
  `busi_data` varchar(1024) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '业务数据',
  `create_datetime` datetime NOT NULL,
  `update_datetime` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_task_id` (`task_id`),
  KEY `idx_create_datetime` (`create_datetime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

CREATE TABLE `task_step` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `task_id` varchar(32) COLLATE utf8mb4_bin NOT NULL COMMENT '任务id',
  `name` varchar(32) COLLATE utf8mb4_bin NOT NULL COMMENT '步骤名称',
  `seq_no` int(11) NOT NULL COMMENT '步骤阶段序号',
  `exec_status` varchar(16) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '步骤执行状态',
  `start_time` datetime DEFAULT NULL COMMENT '执行开始时间',
  `finish_time` datetime DEFAULT NULL COMMENT '执行完成时间',
  `busi_data` varchar(1024) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '业务数据',
  `exec_message` varchar(1024) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '步骤执行信息',
  `max_wait_msec` bigint(20) DEFAULT NULL COMMENT '最大等待时长(毫秒)',
  `step_id` varchar(32) COLLATE utf8mb4_bin NOT NULL COMMENT '步骤唯一标识',
  `create_datetime` datetime NOT NULL,
  `update_datetime` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_step_id` (`step_id`),
  KEY `idx_task_seqno` (`task_id`,`seq_no`),
  KEY `idx_create_datetime` (`create_datetime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

```

### 声明包扫描路径

> arranger中通过注解声明相关bean提供功能，引入SDK后需要添加包扫描路径。

```java
@ComponentScan(basePackages = { "com.sunveee.framework.arranger" })
```

### 注入bean

```java
@Autowired
private ArrangerClient arrangerClient;
```

### 业务定义

> 根据具体的业务场景定义相关的任务类型与步骤，用于后续的任务创建与步骤执行。
>
> 这里以一键部署举例说明。

任务类型：`oneclick`

任务步骤exchange：`oneclick_task_step_exchange`

任务步骤：

```python
# 一键部署任务分为三个阶段，每个阶段执行内容如下：
## 阶段1: 执行步骤1、步骤2
## 阶段2: 执行步骤3
```

### 任务创建

```java
// 构造任务
TaskCreateVO task = new TaskCreateVO();
task.setTaskId("任务1"); // 任务id，每次创建任务需保证任务id不重复
task.setType("oneclick"); // 任务类型：一键部署
task.setBusiData(""); // 任务执行时需要的业务数据，自定义，长度不超过1024

// 构造步骤
TaskStepCreateVO step1 = new TaskStepCreateVO();
step1.setName("步骤1"); // 步骤名称，同一任务步骤名称不可重复
step1.setSeqNo(1); // 步骤执行阶段
step1.setBusiData(""); // 步骤1执行时需要的业务数据，自定义，长度不超过1024

TaskStepCreateVO step2 = new TaskStepCreateVO();
step2.setName("步骤2");
step2.setSeqNo(1);
step2.setBusiData("");

TaskStepCreateVO step3 = new TaskStepCreateVO();
step3.setName("步骤3");
step3.setSeqNo(2);
step3.setBusiData("");

// 创建并启动任务
CreateTaskInput input = new CreateTaskInput();
input.setTask(task);
input.setTaskSteps(Arrays.asList(step1, step2, step3));
arrangerClient.createAndLaunchTask(createTaskInput);
```

### 实现步骤执行逻辑

> 步骤执行由步骤prepared事件消息触发，需要实现该消息的消费逻辑，在消费逻辑中执行步骤。

```java
@SimpleConsumerMethod
@RabbitListener(bindings = @QueueBinding(value = @Queue(value = "oneclick_task_step_prepared_queue", durable = "true"), exchange = @Exchange(value = OneclickTaskConstants.ONECLICK_TASK_STEP_EXCHANGE, type = ExchangeTypes.TOPIC), key = TaskConstants.TASK_STEP_ROUTINGKEY_PREPARED), containerFactory = "simpleConsumerContainerFactory")
public void oneclickTaskStepPrepared(@Payload String content, Channel channel, Message message) {
    final String stepId = content;

    // 开始执行（获取锁）
    arrangerClient.startStepExecution(stepId);

    // 执行某一步骤
    boolean result = false;
    try {
        TaskStepQueryVO step = arrangerClient.queryStep(stepId);
        switch(step.getName()) {
            case "步骤1":
                result = executeStep1();
                break;
            case "步骤2":
                result = executeStep2();
                break;
            case "步骤3":
                result = executeStep3();
                break;
        }
    } catch (InterruptedException e) {
        log.error("", e);
    } finally {
        // 执行完成（释放锁）
        arrangerClient.endStepExecution(stepId, result, result ? "成功" : "失败");
    }

}
```

### 任务管理

```java
// 暂停任务
arrangerClient.pauseTask("任务1");
// 继续任务
arrangerClient.proceedTask("任务1");
// 中止任务
arrangerClient.stopTask("任务1");
// 推进任务
arrangerClient.promoteSteps("任务1");
```

### 查询

```java
// 查询任务与步骤
arrangerClient.queryTaskAndSteps("任务1");
// 查询任务
arrangerClient.queryTask("任务1");
// 查询指定任务的步骤列表
arrangerClient.querySteps("任务1");
// 查询指定步骤
arrangerClient.queryStep("步骤1");
```



## Reference

### 任务创建

任务创建会在一个事务中插入一条任务记录和多条步骤记录，初始插入的任务和记录均为INIT。

任务状态机如下：

![任务状态机](/arranger-sdk/doc-img/%E4%BB%BB%E5%8A%A1%E7%8A%B6%E6%80%81%E6%9C%BA.png)

步骤状态机如下：

![步骤状态机](/arranger-sdk/doc-img/%E6%AD%A5%E9%AA%A4%E7%8A%B6%E6%80%81%E6%9C%BA.png)

创建任务过程中，除数据库表中对数据完整性要求外，还会进行以下校验：

- 步骤序号必须从1开始且连续（可重复）
- 同一任务的步骤名称不允许重复

另外，创建任务时除了自动设置状态外，还会为每个步骤生成唯一的步骤id，该步骤id将用于唯一标识一个步骤。

### 任务启动

任务启动会在一个事务内：

- 更新指定任务状态：INIT >> PROCESS
- 更新指定任务开始时间为当前时间
- 更新指定任务的阶段`1`的步骤状态：INIT >> PREPARED
- 发送上述所更新步骤的prepared消息（routingKey='prepared'），该消息将触发单个步骤的执行

![创建并启动任务](/arranger-sdk/doc-img/%E5%88%9B%E5%BB%BA%E5%B9%B6%E5%90%AF%E5%8A%A8%E4%BB%BB%E5%8A%A1.png)

### 步骤执行

编制服务消费步骤prepared消息，消费逻辑按以下过程执行：

![执行步骤](/arranger-sdk/doc-img/%E6%89%A7%E8%A1%8C%E6%AD%A5%E9%AA%A4.png)

在事务①中，通过带状态更新来加锁，确保一个任务步骤不会因为重复消费的消息而同时被多个服务执行。

具体的执行过程可以是同步的，也可能是异步的。同步执行时，应该合并事务①与事务②为一个事务，并将执行步骤封装在事务内。异步执行时，任务步骤执行服务应该保证不会因为外部原因导致无感知的执行中断。同时，也应该通过某些定时任务来监控挂起时间过长的任务步骤。

步骤执行成功时，将会按照以下流程触发下一阶段步骤的执行：

![步骤执行成功](/arranger-sdk/doc-img/%E6%AD%A5%E9%AA%A4%E6%89%A7%E8%A1%8C%E6%88%90%E5%8A%9F.png)

### 任务推进

检索任务当前执行到的阶段，重新唤起当前阶段的所有未完成且未在执行中的步骤。