package com.phper666.sforce.api.sdk.model;

/**
 * @author Yuzhao.LI
 * @email 562405704@qq.com
 * @date 2026-07-15
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
