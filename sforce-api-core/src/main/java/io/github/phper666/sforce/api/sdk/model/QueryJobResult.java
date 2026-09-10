package io.github.phper666.sforce.api.sdk.model;

import java.io.File;

/**
 * Outcome of a single bulk query job run by {@code BulkApi.runQueryJobs}:
 * the query, its job id and the downloaded CSV result file.
 *
 * @param query      the SOQL query that was run
 * @param jobId      the bulk query job id (for debugging or explicit cleanup)
 * @param resultFile the downloaded CSV result file
 * @author Yuzhao.Li
 */
public record QueryJobResult(String query, String jobId, File resultFile) {}