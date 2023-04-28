package com.sunveee.framework.rpc.common.model.page;

import java.io.Serializable;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponseInfo implements Serializable {
    /**
     * 每页记录数
     */
    @Builder.Default
    private int pageSize = 10;

    /**
     * 页码，从0开始
     */
    private int pageIndex;

    /**
     * 总页数
     */
    private int totalPageCount;

    /**
     * 总记录数
     */
    private long totalRecordCount;
}
