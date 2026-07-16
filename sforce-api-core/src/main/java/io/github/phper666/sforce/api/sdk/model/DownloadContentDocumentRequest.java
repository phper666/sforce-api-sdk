package io.github.phper666.sforce.api.sdk.model;

/**
 * Request to download a Salesforce ContentDocument.
 *
 * @author Yuzhao.Li
 */
public record DownloadContentDocumentRequest(String fileId, String directory, String prefix, String suffix) {}
