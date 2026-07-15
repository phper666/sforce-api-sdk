package com.phper666.sforce.api.sdk.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author Yuzhao.LI
 * @email 562405704@qq.com
 * @date 2026-07-15
 */
@Data
@Accessors(chain=true)
public class ListInvocableActionResult {
    private List<InvocableActionItem> actions;
}