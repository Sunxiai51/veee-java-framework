package com.sunveee.framework.rpc.common.model.page;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageRequestInfo implements Serializable {

    /**
     * 每页记录数
     */
    @Builder.Default
    private int pageSize = 10;

    /**
     * 页码，从0开始
     */
    @Builder.Default
    private int pageIndex = 0;

}
