package io.github.phper666.sforce.api.sdk.internal;

import com.google.gson.annotations.SerializedName;

/**
 * Parsing helper for the record list Salesforce returns on HTTP 300.
 * <p>
 * SF uses a capitalised {@code Id} key here, unlike the lowercase {@code id}
 * of batch responses, so the mapping is explicit.
 *
 * @param id matched record id
 */
record AmbiguousRecordRef(@SerializedName("Id") String id) {
}
