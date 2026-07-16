package io.github.phper666.sforce.api.sdk.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Result wrapper for a list of invocable Salesforce actions.
 *
 * @author Yuzhao.Li
 */
@Data
@Accessors(chain=true)
public class ListInvocableActionResult {
    private List<InvocableActionItem> actions;
}