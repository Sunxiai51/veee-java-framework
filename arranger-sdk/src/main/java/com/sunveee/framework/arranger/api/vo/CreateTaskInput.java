package com.sunveee.framework.arranger.api.vo;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTaskInput {
    private TaskCreateVO task;
    private List<TaskStepCreateVO> taskSteps;
}
