package io.github.phper666.sforce.api.sdk.builder;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import io.github.phper666.sforce.api.sdk.internal.AppendCustomNamespace;
import io.github.phper666.sforce.api.sdk.internal.CustomObjectTypeFactory;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Setter
@ToString
@Accessors(chain = true)
@AppendCustomNamespace
@JsonAdapter(CustomObjectTypeFactory.class)
@ObjectName(value = "SubSoqlTestEntity__c")
public class SubSoqlTestEntity {
    @SerializedName(value = "Id")
    private Long id;
    @SerializedName(value = "ConversationId__c")
    private String conversationId;
    @SerializedName(value = "Name")
    private String name;
}
