package com.sunveee.framework.arranger.api.vo;

import java.time.LocalDateTime;

import com.sunveee.framework.arranger.common.enums.TaskExecStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskQueryVO {
    private long id;
    private String taskId;
    private String type;
    private TaskExecStatus execStatus;
    private LocalDateTime startTime;
    private LocalDateTime finishTime;
    private String busiData;
    private LocalDateTime createDatetime;
    private LocalDateTime updateDatetime;
}
