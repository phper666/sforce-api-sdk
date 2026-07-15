package com.phper666.sforce.api.sdk.model;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Yuzhao.LI
 * @email 562405704@qq.com
 * @date 2026-07-15
 */
@Getter
@Setter
public class CreateOrUpdateObjectResponse extends CreateObjectResponse {
    private boolean created;
}
