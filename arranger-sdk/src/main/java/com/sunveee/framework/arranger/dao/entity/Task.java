package com.sunveee.framework.arranger.dao.entity;

import java.time.LocalDateTime;

import javax.persistence.*;

import com.sunveee.framework.arranger.common.enums.TaskExecStatus;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "task", uniqueConstraints = {
        @UniqueConstraint(columnNames = "task_id")
}, indexes = {
        @Index(columnList = "create_datetime")
})
public class Task extends BaseJpaEntity {

    @Id
    @Column(name = "task_id", columnDefinition = "varchar(32) COMMENT '任务唯一标识'", nullable = false)
    private String taskId;

    @Column(columnDefinition = "varchar(16) COMMENT '任务类型'", nullable = false)
    private String type;

    @Enumerated(EnumType.STRING)
    @Column(name = "exec_status", columnDefinition = "varchar(16) COMMENT '任务执行状态'", nullable = false)
    private TaskExecStatus execStatus;

    @Column(name = "start_time", columnDefinition = "datetime COMMENT '任务开始时间'")
    private LocalDateTime startTime;

    @Column(name = "finish_time", columnDefinition = "datetime COMMENT '任务完成时间'")
    private LocalDateTime finishTime;

    @Column(name = "busi_data", columnDefinition = "varchar(1024) COMMENT '业务数据'")
    private String busiData;
}
