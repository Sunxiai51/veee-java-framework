package com.sunveee.framework.arranger.dao.entity;

import java.time.LocalDateTime;

import javax.persistence.*;

import com.sunveee.framework.arranger.common.enums.TaskStepExecStatus;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "task_step", uniqueConstraints = {
        @UniqueConstraint(columnNames = "step_id")
}, indexes = {
        @Index(columnList = "task_id, seq_no"),
        @Index(columnList = "create_datetime")
})
public class TaskStep extends BaseJpaEntity {

    @Id
    @Column(name = "step_id", columnDefinition = "varchar(32) COMMENT '步骤唯一标识'", nullable = false)
    private String stepId;

    @Column(name = "name", columnDefinition = "varchar(32) COMMENT '步骤名称'", nullable = false)
    private String name;

    @Column(name = "seq_no", columnDefinition = "int COMMENT '步骤阶段序号'", nullable = false)
    private Integer seqNo;

    @Column(name = "task_id", columnDefinition = "varchar(32) COMMENT '任务id'", nullable = false)
    private String taskId;

    @Enumerated(EnumType.STRING)
    @Column(name = "exec_status", columnDefinition = "varchar(16) COMMENT '步骤执行状态'")
    private TaskStepExecStatus execStatus;

    @Column(name = "start_time", columnDefinition = "datetime COMMENT '执行开始时间'")
    private LocalDateTime startTime;

    @Column(name = "finish_time", columnDefinition = "datetime COMMENT '执行完成时间'")
    private LocalDateTime finishTime;

    @Column(name = "busi_data", columnDefinition = "varchar(1024) COMMENT '业务数据'")
    private String busiData;

    @Column(name = "exec_message", columnDefinition = "varchar(1024) COMMENT '步骤执行信息'")
    private String execMessage;

    @Column(name = "max_wait_msec", columnDefinition = "bigint COMMENT '最大等待时长(毫秒)'")
    private Long maxWaitMsec;
}
