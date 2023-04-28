package com.sunveee.framework.rpc.common.utils.page;

import com.sunveee.framework.rpc.common.model.page.PageRequestInfo;
import com.sunveee.framework.rpc.common.model.page.PageResponseInfo;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * MemoryPagingUtils
 *
 * @author SunVeee
 * @date 2022/4/14 10:44
 */
public class MemoryPagingUtils {

    public static <T> AbstractMap.SimpleImmutableEntry<PageResponseInfo, List<T>> paging(PageRequestInfo pageRequestInfo, List<T> allRecords, Comparator<? super T> comparator) {
        PageResponseInfo pageResponseInfo = PageResponseInfo.builder().build();
        if (null == allRecords || allRecords.isEmpty()) {
            pageResponseInfo.setPageSize(pageRequestInfo.getPageSize());
            pageResponseInfo.setPageIndex(0);
            pageResponseInfo.setTotalPageCount(0);
            pageResponseInfo.setTotalRecordCount(0);
            return new AbstractMap.SimpleImmutableEntry(pageResponseInfo, new ArrayList<>());
        }

        final int pageSize = Math.max(1, pageRequestInfo.getPageSize());
        final int totalRecordCount = allRecords.size();
        final int totalPageCount = (totalRecordCount - 1) / pageSize + 1;
        final int pageIndex = Math.min(pageRequestInfo.getPageIndex(), totalPageCount - 1);

        int from = pageIndex * pageSize;
        int to = Math.min(from + pageSize, totalRecordCount);

        // 排序
        List<T> sortedList = allRecords.stream().sorted(comparator).collect(Collectors.toList());

        return new AbstractMap.SimpleImmutableEntry(PageResponseInfo.builder()
                .pageIndex(pageIndex)
                .pageSize(pageSize)
                .totalPageCount(totalPageCount)
                .totalRecordCount(totalRecordCount)
                .build(),
                sortedList.subList(from, to));
    }
}
