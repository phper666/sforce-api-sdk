package com.phper666.sforce.api.sdk.model;

import lombok.Data;

/**
 * @author Yuzhao.LI
 * @email 562405704@qq.com
 * @date 2026-07-15
 */
@Data
public class ObjectFieldDescribe {
    private String name;
    private String type;
    private String label;
    private boolean updateable;
}
