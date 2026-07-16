package io.github.phper666.sforce.api.sdk.builder;

import io.github.phper666.sforce.api.sdk.builder.utils.SoqlUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;

import static io.github.phper666.sforce.api.sdk.builder.SoqlTestEntity.*;

@ExtendWith(MockitoExtension.class)
class SoqlBuilderTest {
    @Test
    public void testSoqlQueryBuilderCompare() {
        SoqlUtil.setGlobalCustomObjectNamespace("");
        String soql = new SoqlBuilder<>(SoqlTestEntity.class)
                .eq(SoqlTestEntity::isDeleted, false)
                .eq(SoqlTestEntity::getConversationId, "conversationId")
                .le(SoqlTestEntity::getNum, 10)
                .gt(SoqlTestEntity::getNum, 10)
                .lt(SoqlTestEntity::getEntityTime, "2025-10-19T15:06:28+08:00")
                .ge(SoqlTestEntity::getEntityTime, "2025-10-09T15:06:28+08:00")
                .neq(SoqlTestEntity::getName, "name")
                .eqMultiSelect(SoqlTestEntity::getPicklists, "AAA", "BBB")
                .neqMultiSelect(SoqlTestEntity::getPicklists, 111, 222)
                .excludes(SoqlTestEntity::getPicklists, "AAA", "BBB")
                .includes(SoqlTestEntity::getPicklists, Arrays.asList(new String[]{"AAA", "BBB"}))
                .build();

        String soql2 = new StringSoqlBuilder<>(SoqlTestEntity.class)
                .eq(FIELD_DELETED, false)
                .eq(FIELD_CONVERSATION_ID, "conversationId")
                .le(FIELD_NUM, 10)
                .gt(FIELD_NUM, 10)
                .lt(FIELD_ENTITY_TIME, "2025-10-19T15:06:28+08:00")
                .ge(FIELD_ENTITY_TIME, "2025-10-09T15:06:28+08:00")
                .neq(FIELD_NAME, "name")
                .eqMultiSelect(FIELD_PICKLISTS, "AAA", "BBB")
                .neqMultiSelect(FIELD_PICKLISTS, 111, 222)
                .excludes(FIELD_PICKLISTS, Arrays.asList(new String[]{"AAA", "BBB"}))
                .includes(FIELD_PICKLISTS, "AAA", "BBB")
                .build();
        String resultSoql = "SELECT Id,Name,ConversationId__c,EntityTime__c,IsDeleted__c,Num__c,Picklists__c FROM SoqlTest__c " +
                "WHERE IsDeleted__c = false AND ConversationId__c = 'conversationId' AND Num__c <= 10 AND Num__c > 10 " +
                "AND EntityTime__c < 2025-10-19T15%3A06%3A28%2B08%3A00 AND EntityTime__c >= 2025-10-09T15%3A06%3A28%2B08%3A00 " +
                "AND Name != 'name' AND Picklists__c = 'AAA;BBB' AND Picklists__c != '111;222' AND Picklists__c EXCLUDES ('AAA','BBB') AND Picklists__c INCLUDES ('AAA','BBB')";

        Assertions.assertEquals(resultSoql, soql);
        Assertions.assertEquals(soql2, soql);
    }

    @Test
    public void testSoqlQueryBuilderFunc() {
        SoqlUtil.setGlobalCustomObjectNamespace("");
        String soql = new SoqlBuilder<>(SoqlTestEntity.class)
                .in(SoqlTestEntity::getId, 11L, 22L)
                .notIn(SoqlTestEntity::getConversationId, "conversationId1", "conversationId2")
                .inSoql(SoqlTestEntity::getName, new SoqlBuilder<>(SubSoqlTestEntity.class).select(SubSoqlTestEntity::getName).build())
                .notInSoql(SoqlTestEntity::getNum, "1,2,3,4,5")
                .isNull(SoqlTestEntity::getEntityTime)
                .isNotNull(SoqlTestEntity::isDeleted)
                .orderByDesc(SoqlTestEntity::getEntityTime)
                .build();

        String soql2 = new StringSoqlBuilder<>(SoqlTestEntity.class)
                .in(FIELD_ID, 11L, 22L)
                .notIn(FIELD_CONVERSATION_ID, "conversationId1", "conversationId2")
                .inSoql(FIELD_NAME, new SoqlBuilder<>(SubSoqlTestEntity.class).select(SubSoqlTestEntity::getName).build())
                .notInSoql(FIELD_NUM, "1,2,3,4,5")
                .isNull(FIELD_ENTITY_TIME)
                .isNotNull(FIELD_DELETED)
                .orderByDesc(FIELD_ENTITY_TIME)
                .build();

        String resultSoql = "SELECT Id,Name,ConversationId__c,EntityTime__c,IsDeleted__c,Num__c,Picklists__c FROM SoqlTest__c " +
                "WHERE Id IN (11,22) AND ConversationId__c NOT IN ('conversationId1','conversationId2') " +
                "AND Name IN (SELECT Name FROM SubSoqlTestEntity__c) " +
                "AND Num__c NOT IN (1,2,3,4,5) AND EntityTime__c = null " +
                "AND IsDeleted__c != null ORDER BY EntityTime__c DESC NULLS LAST";

        Assertions.assertEquals(resultSoql, soql);
        Assertions.assertEquals(soql2, soql);
    }

    @Test
    public void testSoqlQueryBuilderNested() {
        SoqlUtil.setGlobalCustomObjectNamespace("");
        String soql = new SoqlBuilder<>(SoqlTestEntity.class)
                .select(SoqlTestEntity::getId, SoqlTestEntity::getName)
                .eq(true, SoqlTestEntity::getId, 1L)
                .eq(false, SoqlTestEntity::getName, "name")
                .and(soqlBuilder -> soqlBuilder.eq(SoqlTestEntity::getNum, 1L).or().eq(SoqlTestEntity::getNum, 2L))
                .not(soqlBuilder -> soqlBuilder.eq(SoqlTestEntity::getConversationId, "conversationId").eq(SoqlTestEntity::getEntityTime, "2025-10-19T15:06:28+08:00"))
                .or(soqlBuilder -> soqlBuilder.eqMultiSelect(SoqlTestEntity::getPicklists, "AAA", "BBB").not().eq(SoqlTestEntity::getName, "name"))
                .build();

        String soql2 = new StringSoqlBuilder<>(SoqlTestEntity.class)
                .select(FIELD_ID, FIELD_NAME)
                .eq(true, FIELD_ID, 1L)
                .eq(false, FIELD_NAME, "name")
                .and(soqlQueryBuilder -> soqlQueryBuilder.eq(FIELD_NUM, 1L).or().eq(FIELD_NUM, 2L))
                .not(soqlQueryBuilder -> soqlQueryBuilder.eq(FIELD_CONVERSATION_ID, "conversationId").eq(FIELD_ENTITY_TIME, "2025-10-19T15:06:28+08:00"))
                .or(soqlQueryBuilder -> soqlQueryBuilder.eqMultiSelect(FIELD_PICKLISTS, "AAA", "BBB").not().eq(FIELD_NAME, "name"))
                .build();

        String resultSoql = "SELECT Id,Name FROM SoqlTest__c WHERE Id = 1 " +
                "AND (Num__c = 1 OR Num__c = 2) " +
                "NOT (ConversationId__c = 'conversationId' AND EntityTime__c = 2025-10-19T15%3A06%3A28%2B08%3A00) " +
                "OR (Picklists__c = 'AAA;BBB' NOT Name = 'name')";

        Assertions.assertEquals(resultSoql, soql);
        Assertions.assertEquals(soql2, soql);
    }

    @Test
    public void testSoqlQueryBuilder_select1() {
        SoqlUtil.setGlobalCustomObjectNamespace("");
        String soql = new SoqlBuilder<>(SoqlTestEntity.class)
                .select("MIN(Num__c) min", "MAX(Num__c) max")
                .select(SoqlTestEntity::getPicklists, "pl", true)
                //设置不查询的字段
                .select(SoqlTestEntity.class, s -> !s.equals(FIELD_PICKLISTS))
                .groupBy(SoqlTestEntity::getNum)
                .build();

        String soql2 = new StringSoqlBuilder<>(SoqlTestEntity.class)
                .select(FIELD_ID, FIELD_NAME, FIELD_CONVERSATION_ID, FIELD_ENTITY_TIME, FIELD_DELETED, FIELD_NUM, "MIN(Num__c) min", "MAX(Num__c) max")
                .select(FIELD_PICKLISTS, "pl", true)
                //设置不查询的字段
                .select(SoqlTestEntity.class, s -> !s.equals(FIELD_PICKLISTS))
                .groupBy(FIELD_NUM)
                .build();

        String resultSoql = "SELECT Id,Name,ConversationId__c,EntityTime__c,IsDeleted__c,Num__c,MIN(Num__c) min,MAX(Num__c) max,Picklists__c pl FROM SoqlTest__c GROUP BY Num__c";
        Assertions.assertEquals(resultSoql, soql);
        Assertions.assertEquals(soql2, soql);
    }

    @Test
    public void testSoqlQueryBuilder_select2() {
        SoqlUtil.setGlobalCustomObjectNamespace("");
        String soql = new SoqlBuilder<SoqlTestEntity>()
                .select(SoqlTestEntity.class)
                .select(SoqlTestEntity::getId, SoqlTestEntity::getName)
                .select(SoqlTestEntity::getPicklists, "pl", true)
                .orderByDesc(SoqlTestEntity::getEntityTime)
                .limit(10)
                .offset(100)
                .build();

        String soql2 = new StringSoqlBuilder<SoqlTestEntity>()
                .select(SoqlTestEntity.class)
                .select(FIELD_ID, FIELD_NAME)
                .select(FIELD_PICKLISTS, "pl", true)
                .orderByDesc(FIELD_ENTITY_TIME)
                .limit(10)
                .offset(100)
                .build();

        String resultSoql = "SELECT Id,Name,Picklists__c pl FROM SoqlTest__c ORDER BY EntityTime__c DESC NULLS LAST LIMIT 10 OFFSET 100";
        Assertions.assertEquals(resultSoql, soql);
        Assertions.assertEquals(soql2, soql);
    }

    @Test
    public void testSoqlQueryBuilder_selectFieldAll() {
        SoqlUtil.setGlobalCustomObjectNamespace("");
        String soql = new SoqlBuilder<SoqlTestEntity>()
                .select(SoqlTestEntity.class)
                .selectFieldAll()
                .orderByDesc(SoqlTestEntity::getEntityTime, SoqlTestEntity::getNum)
                .limit(10)
                .offset(100)
                .build();

        String soql2 = new StringSoqlBuilder<SoqlTestEntity>()
                .select(SoqlTestEntity.class)
                .selectFieldAll()
                .orderByDesc(FIELD_ENTITY_TIME, FIELD_NUM)
                .limit(10)
                .offset(100)
                .build();

        String resultSoql = "SELECT Fields(all) FROM SoqlTest__c ORDER BY EntityTime__c,Num__c DESC NULLS LAST LIMIT 10 OFFSET 100";
        Assertions.assertEquals(resultSoql, soql);
        Assertions.assertEquals(soql2, soql);
    }

    @Test
    public void testSoqlQueryBuilder_unselect() {
        SoqlUtil.setGlobalCustomObjectNamespace("");
        String soql = new SoqlBuilder<SoqlTestEntity>()
                .select(SoqlTestEntity.class)
                .unselect(SoqlTestEntity::getId, SoqlTestEntity::getName)
                .unselect(true, SoqlTestEntity::getPicklists)
                .orderByAsc(SoqlTestEntity::getEntityTime, SoqlTestEntity::getNum)
                .limit(10)
                .offset(100)
                .build();

        String soql2 = new StringSoqlBuilder<SoqlTestEntity>()
                .select(SoqlTestEntity.class)
                .unselect(FIELD_ID, FIELD_NAME)
                .unselect(true, FIELD_PICKLISTS)
                .orderByAsc(FIELD_ENTITY_TIME, FIELD_NUM)
                .limit(10)
                .offset(100)
                .build();

        String resultSoql = "SELECT ConversationId__c,EntityTime__c,IsDeleted__c,Num__c FROM SoqlTest__c ORDER BY EntityTime__c,Num__c ASC NULLS LAST LIMIT 10 OFFSET 100";
        Assertions.assertEquals(resultSoql, soql);
        Assertions.assertEquals(soql2, soql);
    }

    @Test
    public void testSoqlQueryBuilder_selectCount1() {
        SoqlUtil.setGlobalCustomObjectNamespace("");
        String soql = new SoqlBuilder<>(SoqlTestEntity.class)
                .selectCount()
                .build();

        String soql2 = new StringSoqlBuilder<>(SoqlTestEntity.class)
                .selectCount()
                .build();

        String resultSoql = "SELECT COUNT() FROM SoqlTest__c";
        Assertions.assertEquals(resultSoql, soql);
        Assertions.assertEquals(soql2, soql);
    }

    @Test
    public void testSoqlQueryBuilder_selectCount2() {
        SoqlUtil.setGlobalCustomObjectNamespace("");
        String soql = new SoqlBuilder<>(SoqlTestEntity.class)
                .select(SoqlTestEntity::getName)
                .selectCount(SoqlTestEntity::getId, true)
                .selectCount(SoqlTestEntity::getNum, "num", true)
                .build();

        String soql2 = new StringSoqlBuilder<>(SoqlTestEntity.class)
                .select(FIELD_NAME)
                .selectCount(FIELD_ID, true)
                .selectCount(FIELD_NUM, "num", true)
                .build();

        String resultSoql = "SELECT Name,COUNT(Id),COUNT(Num__c) num FROM SoqlTest__c";
        Assertions.assertEquals(resultSoql, soql);
        Assertions.assertEquals(soql2, soql);
    }

    @Test
    public void testSoqlQueryBuilder_selectCount3() {
        SoqlUtil.setGlobalCustomObjectNamespace("");
        String soql = new SoqlBuilder<>(SoqlTestEntity.class)
                .select(SoqlTestEntity::getName)
                .selectCount(SoqlTestEntity::getId)
                .build();

        String soql2 = new StringSoqlBuilder<>(SoqlTestEntity.class)
                .select(FIELD_NAME)
                .selectCount(FIELD_ID)
                .build();

        String resultSoql = "SELECT COUNT(Id) FROM SoqlTest__c";
        Assertions.assertEquals(resultSoql, soql);
        Assertions.assertEquals(soql2, soql);
    }

    @Test
    public void testSoqlQueryBuilder_namespace() {
        SoqlUtil.setGlobalCustomObjectNamespace("CXG");
        String soql = new SoqlBuilder<>(SoqlTestEntity.class)
                .select(SoqlTestEntity::getName)
                .selectCount(SoqlTestEntity::getConversationId, true)
                .eq(SoqlTestEntity::getNum, 10)
                .inSoql(SoqlTestEntity::getId, new SoqlBuilder<>(SubSoqlTestEntity.class).select(SubSoqlTestEntity::getId).eq(SubSoqlTestEntity::getName, "name").build())
                .orderByAsc(SoqlTestEntity::getEntityTime)
                .build();

        String soql2 = new StringSoqlBuilder<>(SoqlTestEntity.class)
                .select(FIELD_NAME)
                .selectCount(FIELD_CONVERSATION_ID, true)
                .eq(FIELD_NUM, 10)
                .inSoql(FIELD_ID, new SoqlBuilder<>(SubSoqlTestEntity.class).select(SubSoqlTestEntity::getId).eq(SubSoqlTestEntity::getName, "name").build())
                .orderByAsc(FIELD_ENTITY_TIME)
                .build();

        String resultSoql = "SELECT Name,COUNT(CXG__ConversationId__c) FROM CXG__SoqlTest__c " +
                "WHERE CXG__Num__c = 10 " +
                "AND Id IN (SELECT Id FROM CXG__SubSoqlTestEntity__c WHERE Name = 'name') " +
                "ORDER BY CXG__EntityTime__c ASC NULLS LAST";
        Assertions.assertEquals(resultSoql, soql);
        Assertions.assertEquals(soql2, soql);
    }
}
