package com.sunveee.framework.arranger.api.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskStepCreateVO {

    private String name;

    private Integer seqNo;

    private String busiData;

    private Long maxWaitMsec;
}
