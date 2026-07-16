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
@ObjectName(value = "SoqlTest__c")
public class SoqlTestEntity {
    @SerializedName(value = "Id")
    private Long id;
    @SerializedName(value = "Name")
    private String name;
    @SerializedName(value = "ConversationId__c")
    private String conversationId;
    @SerializedName(value = "EntityTime__c")
    private String entityTime;
    @SerializedName(value = "IsDeleted__c")
    private boolean deleted;
    @SerializedName(value = "Num__c")
    private Integer num;
    @SerializedName(value = "Picklists__c")
    private String picklists;

    public static final String FIELD_ID = "Id";
    public static final String FIELD_NAME = "Name";
    public static final String FIELD_CONVERSATION_ID = "ConversationId__c";
    public static final String FIELD_ENTITY_TIME = "EntityTime__c";
    public static final String FIELD_DELETED = "IsDeleted__c";
    public static final String FIELD_NUM = "Num__c";
    public static final String FIELD_PICKLISTS = "Picklists__c";
}
