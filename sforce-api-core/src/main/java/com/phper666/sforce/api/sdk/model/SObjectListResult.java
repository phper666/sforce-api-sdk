package com.phper666.sforce.api.sdk.model;

import lombok.Data;

import java.util.List;

/**
 * Wrapper for {@code GET /services/data/vXX.X/sobjects/} response.
 */
@Data
public class SObjectListResult {
    private List<SObjectMetadata> sobjects;
}
