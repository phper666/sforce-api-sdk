package io.github.phper666.sforce.api.sdk.model;

import io.github.phper666.sforce.api.sdk.serialize.GsonJsonSerializer;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Base response body for Salesforce API responses.
 *
 * @author Yuzhao.Li
 */
@Getter
@Setter
public class BaseResponseBody {
    private Object errors;

    /**
     * Typed view of {@link #errors}.
     * <p>
     * Salesforce returns errors as an array of {@code {statusCode, message, fields}}
     * entries. {@link #errors} is kept as {@code Object} for backward compatibility;
     * this accessor parses the same data.
     *
     * @return error details, empty when the response carries no errors
     */
    @SuppressWarnings("unchecked")
    public List<ResponseErrorDto> errorList() {
        if (errors == null) {
            return List.of();
        }
        var serializer = GsonJsonSerializer.INSTANCE();
        return (List<ResponseErrorDto>) serializer.fromJsonList(serializer.toJson(errors), ResponseErrorDto.class);
    }
}
