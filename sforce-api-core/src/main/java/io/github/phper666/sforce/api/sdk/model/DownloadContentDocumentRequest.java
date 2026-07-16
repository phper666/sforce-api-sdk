package io.github.phper666.sforce.api.sdk.model;

/**
 * @author Yuzhao.Li
 * @email 562405704@qq.com
 * @date 2026-07-15
 */
public record DownloadContentDocumentRequest(String fileId, String directory, String prefix, String suffix) {}
