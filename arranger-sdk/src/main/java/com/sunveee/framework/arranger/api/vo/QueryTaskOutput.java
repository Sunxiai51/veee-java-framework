package com.sunveee.framework.arranger.api.vo;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QueryTaskOutput {
    private TaskQueryVO task;
    private List<TaskStepQueryVO> taskSteps;
}
