package com.sunveee.framework.arranger.api.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskCreateVO {

    private String taskId;

    private String type;

    private String busiData;
}
