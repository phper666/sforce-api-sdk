package com.phper666.sforce.api.sdk.model;

import lombok.Data;

import java.util.List;

/**
 * @author Yuzhao.LI
 * @email 562405704@qq.com
 * @date 2026-07-15
 */
@Data
public class PageQueryResponse<T> {
    private Integer totalSize;
    private Boolean done;
    private List<T> records;
    private String nextRecordsUrl;
}
