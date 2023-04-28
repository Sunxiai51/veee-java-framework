package com.sunveee.framework.arranger.api.vo;

import java.time.LocalDateTime;

import com.sunveee.framework.arranger.common.enums.TaskStepExecStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskStepQueryVO {
    private long id;
    private String stepId;
    private String name;
    private Integer seqNo;
    private String taskId;
    private TaskStepExecStatus execStatus;
    private LocalDateTime startTime;
    private LocalDateTime finishTime;
    private String busiData;
    private String execMessage;
    private Long maxWaitMsec;
    private LocalDateTime createDatetime;
    private LocalDateTime updateDatetime;
}
