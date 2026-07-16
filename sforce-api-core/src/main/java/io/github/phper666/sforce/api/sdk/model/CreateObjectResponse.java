package io.github.phper666.sforce.api.sdk.model;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Yuzhao.Li
 * @email 562405704@qq.com
 * @date 2026-07-15
 */
@Getter
@Setter
public class CreateObjectResponse extends BaseResponseBody {
    private String id;
    private boolean success;
}
