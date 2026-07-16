package io.github.phper666.sforce.api.sdk.model;

/**
 * Request body for a Salesforce parameterized SOSL search.
 *
 * @author Yuzhao.Li
 */
public record ParameterizedSearchRequestBody(
    String q,
    String defaultLimit,
    String division,
    String[] fields,
    DataCategoriesFilter[] dataCategories,
    String in,
    String metadata,
    String[] netWorkIds,
    String offset,
    String overallLimit,
    String pricebookId,
    String snippet,
    SobjectsFilter[] sobjects,
    boolean spellCorrection,
    String updateTracking,
    String updateViewStat
) {
    public record DataCategoriesFilter(String groupName, String operator, String[] categories) {}

    public record SobjectsFilter(String[] fields, String limit, String name, String orderBy, String where) {}
}
