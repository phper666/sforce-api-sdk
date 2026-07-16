package io.github.phper666.sforce.api.sdk.builder;

import io.github.phper666.sforce.api.sdk.builder.interfaces.*;
import io.github.phper666.sforce.api.sdk.builder.segments.*;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static io.github.phper666.sforce.api.sdk.builder.SoqlKeyword.*;
import static io.github.phper666.sforce.api.sdk.builder.StringPool.LEFT_BRACKET;
import static io.github.phper666.sforce.api.sdk.builder.StringPool.RIGHT_BRACKET;
import static io.github.phper666.sforce.api.sdk.builder.utils.SoqlUtil.*;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.StringUtils.SPACE;

/**
 * @author Yuzhao.Li
 * @email 562405704@qq.com
 * @date 2026-07-15
 */
@SuppressWarnings("unchecked")
public abstract class AbstractSoqlBuilder<T, R, Children extends AbstractSoqlBuilder<T, R, Children>>
        implements Compare<Children, R>, Nested<Children, Children>, Func<Children, R>, Query<Children, T, R> {
    protected final Children typedThis = (Children) this;
    protected final SelectSegmentList select = new SelectSegmentList();
    protected final NormalSegmentList condition = new NormalSegmentList();
    /**
     * not support field type:
     * multi-select picklist, rich text area, long text area, encrypted (if enabled), and data category group reference
     */
    protected final OrderBySegmentList orderBy = new OrderBySegmentList();
    protected final GroupBySegmentList group = new GroupBySegmentList();
    protected final PageSegmentList page = new PageSegmentList();

    @Override
    public Children eq(boolean condition, R column, Object value) {
        return addCondition(condition, column, SoqlKeyword.EQ, value);
    }

    @Override
    public Children neq(boolean condition, R column, Object value) {
        return addCondition(condition, column, SoqlKeyword.NEQ, value);
    }

    @Override
    public Children gt(boolean condition, R column, Object value) {
        return addCondition(condition, column, SoqlKeyword.GT, value);
    }

    @Override
    public Children ge(boolean condition, R column, Object value) {
        return addCondition(condition, column, SoqlKeyword.GE, value);
    }

    @Override
    public Children lt(boolean condition, R column, Object value) {
        return addCondition(condition, column, SoqlKeyword.LT, value);
    }

    @Override
    public Children le(boolean condition, R column, Object value) {
        return addCondition(condition, column, SoqlKeyword.LE, value);
    }

    @Override
    public Children eqMultiSelect(boolean condition, R column, Collection<?> coll) {
        return eq(condition, column, getMultiSelectValue(coll));
    }

    @Override
    public Children neqMultiSelect(boolean condition, R column, Collection<?> coll) {
        return neq(condition, column, getMultiSelectValue(coll));
    }

    @Override
    public Children includes(boolean condition, R column, Collection<?> coll) {
        return doIt(condition, () -> columnToString(column), SoqlKeyword.INCLUDES, () -> getValuesFormat(coll));
    }

    @Override
    public Children excludes(boolean condition, R column, Collection<?> coll) {
        return doIt(condition, () -> columnToString(column), SoqlKeyword.EXCLUDES, () -> getValuesFormat(coll));
    }

    @Override
    public Children in(boolean condition, R column, Collection<?> coll) {
        if (coll == null || coll.isEmpty()) {
            return typedThis;
        }
        Validate.isTrue(coll.size() <= 200, "in list is less than or equal to 200");
        return doIt(condition, () -> columnToString(column), SoqlKeyword.IN, () -> getValuesFormat(coll));
    }

    @Override
    public Children notIn(boolean condition, R column, Collection<?> coll) {
        if (coll == null || coll.isEmpty()) {
            return typedThis;
        }
        Validate.isTrue(coll.size() <= 200, "notIn list is less than or equal to 200");
        return doIt(condition, () -> columnToString(column), SoqlKeyword.NOT_IN, () -> getValuesFormat(coll));
    }

    @Override
    public Children inSoql(boolean condition, R column, String inValue) {
        return doIt(condition, () -> columnToString(column), SoqlKeyword.IN, () -> getInValueFormat(inValue));
    }

    @Override
    public Children notInSoql(boolean condition, R column, String inValue) {
        return doIt(condition, () -> columnToString(column), SoqlKeyword.NOT_IN, () -> getInValueFormat(inValue));
    }

    @Override
    public Children isNull(boolean condition, R column) {
        return eq(condition, column, null);
    }

    @Override
    public Children isNotNull(boolean condition, R column) {
        return neq(condition, column, null);
    }

    @Override
    public Children orderByAsc(R column) {
        return orderBy(true, true, false, column);
    }

    @Override
    @SafeVarargs
    public final Children orderByAsc(R... columns) {
        return orderByAsc(false, columns);
    }

    @Override
    public Children orderByDesc(R column) {
        return orderBy(true, false, false, column);
    }

    @Override
    @SafeVarargs
    public final Children orderByDesc(R... columns) {
        return orderByDesc(false, columns);
    }

    @Override
    @SafeVarargs
    public final Children orderBy(boolean isAsc, boolean isNullsFirst, R... columns) {
        return orderBy(true, isAsc, isNullsFirst, columns);
    }

    @Override
    @SafeVarargs
    public final Children orderBy(boolean condition, boolean isAsc, boolean isNullsFirst, R... columns) {
        if (ArrayUtils.isEmpty(columns)) {
            return typedThis;
        }
        if (condition) {
            orderBy.add(SoqlKeyword.ORDER_BY, () -> columns.length == 1 ? columnToString(columns[0]) : columnsToString(columns));
            SoqlKeyword order = isAsc ? SoqlKeyword.ASC : SoqlKeyword.DESC;
            orderBy.add(order);
            SoqlKeyword nullsFirst = isNullsFirst ? SoqlKeyword.NULLS_FIRST : SoqlKeyword.NULLS_LAST;
            orderBy.add(nullsFirst);
        }
        return typedThis;
    }

    @Override
    public Children groupBy(R column) {
        return groupBy(true, column);
    }

    @Override
    @SafeVarargs
    public final Children groupBy(R... columns) {
        return groupBy(true, columns);
    }

    @Override
    @SafeVarargs
    public final Children groupBy(boolean condition, R... columns) {
        if (ArrayUtils.isEmpty(columns)) {
            return typedThis;
        }
        if (condition) {
            group.add(GROUP_BY, () -> columns.length == 1 ? columnToString(columns[0]) : columnsToString(columns));
        }
        return typedThis;
    }

    public Children limit(Integer limit) {
        if (limit == null) {
            return typedThis;
        }
        page.add(LIMIT, limit::toString);
        return typedThis;
    }

    public Children offset(Integer offset) {
        if (offset == null) {
            return typedThis;
        }
        page.add(OFFSET, offset::toString);
        return typedThis;
    }

    @Override
    public Children and(boolean condition) {
        this.condition.add(LogicOperator.AND);
        return typedThis;
    }

    @Override
    public Children and(boolean condition, Consumer<Children> consumer) {
        return and().addNestedCondition(condition, consumer);
    }

    @Override
    public Children or(boolean condition) {
        this.condition.add(LogicOperator.OR);
        return typedThis;
    }

    @Override
    public Children or(boolean condition, Consumer<Children> consumer) {
        return or().addNestedCondition(condition, consumer);
    }

    @Override
    public Children not(boolean condition) {
        this.condition.add(LogicOperator.NOT);
        return typedThis;
    }

    @Override
    public Children not(boolean condition, Consumer<Children> consumer) {
        return not().addNestedCondition(condition, consumer);
    }

    /**
     * 普通查询条件
     *
     * @param condition   是否执行
     * @param column      属性
     * @param soqlKeyword SOQL 关键词
     * @param value       条件值
     */
    protected Children addCondition(boolean condition, R column, SoqlKeyword soqlKeyword, Object value) {
        return doIt(condition, () -> columnToString(column), soqlKeyword, () -> getValueFormat(value));
    }

    /**
     * @param condition 是否执行
     * @param segments  soql片段数组
     */
    protected Children doIt(boolean condition, SoqlSegment... segments) {
        if (ArrayUtils.isEmpty(segments)) {
            return typedThis;
        }
        if (condition) {
            if (!this.condition.isEmpty() && !(this.condition.getLastSegment() instanceof LogicOperator)) {
                this.condition.add(LogicOperator.AND);
            }
            this.condition.add(segments);
        }
        return typedThis;
    }

    /**
     * 多重嵌套查询条件
     *
     * @param condition 查询条件值
     */
    protected Children addNestedCondition(boolean condition, Consumer<Children> consumer) {
        if (condition) {
            final Children instance = instance();
            consumer.accept(instance);
            instance.condition.setNestedSegments(true);
            this.condition.add(instance::buildNested);
        }
        return typedThis;
    }

    /**
     * 获取 columnName
     */
    protected String columnToString(R column) {
        return appendNamespace((String) column);
    }

    /**
     * 多字段转换为逗号 "," 分割字符串
     *
     * @param columns 多字段
     */
    protected String columnsToString(R... columns) {
        return Arrays.stream(columns).map(this::columnToString).collect(joining(StringPool.COMMA));
    }

    /**
     * 子类返回一个自己的新对象
     */
    protected abstract Children instance();

    public String build() {
        return select.getSoqlSegment() +
                condition.getSoqlSegment() +
                group.getSoqlSegment() +
                orderBy.getSoqlSegment() +
                page.getSoqlSegment();
    }

    protected String buildNested() {
        select.setNestedSegments(true);
        return LEFT_BRACKET + build() + RIGHT_BRACKET;
    }

    @Override
    @SafeVarargs
    public final Children select(R... columns) {
        var selectedFields = Arrays.stream(columns)
                .map((Function<R, SoqlSegment>) column -> () -> columnToString(column))
                .toList();
        select.clear();
        select.addAll(selectedFields);
        return typedThis;
    }

    @Override
    @SafeVarargs
    public final Children unselect(R... columns) {
        if (ArrayUtils.isEmpty(columns)) {
            return typedThis;
        }
        Validate.notEmpty(select, "selected fields is empty");
        select.removeIf(soqlSegment -> Arrays.stream(columns).map(this::columnToString).anyMatch(obj -> obj.equals(soqlSegment.getSoqlSegment())));

        return typedThis;
    }

    @Override
    public Children select(R column, String alias, boolean isAppend) {
        if (StringUtils.isBlank(alias)) {
            return select(column, isAppend);
        }
        SoqlSegment soqlSegment = () -> columnToString(column) + SPACE + alias;
        if (isAppend) {
            select.add(soqlSegment);
        } else {
            select.clear();
            select.add(soqlSegment);
        }
        return typedThis;
    }

    @Override
    public Children select(R column, boolean isAppend) {
        SoqlSegment soqlSegment = () -> columnToString(column);
        if (isAppend) {
            select.add(soqlSegment);
        } else {
            select.clear();
            select.add(soqlSegment);
        }
        return typedThis;
    }

    @Override
    public Children select(Class<T> entityClass) {
        select.initiate(entityClass);
        return typedThis;
    }

    @Override
    public Children select(Class<T> entityClass, Predicate<String> predicate) {
        Validate.notNull(entityClass, "please set the entity class for query");
        if (select.isEmpty()) {
            select.initiate(entityClass);
        }
        select.removeIf(soqlSegment -> !predicate.test(soqlSegment.getSoqlSegment()));
        return typedThis;
    }

    @Override
    public Children unselect(boolean condition, R column) {
        if (condition) {
            Validate.notEmpty(select, "selected fields is empty");
            select.removeIf(soqlSegment -> columnToString(column).equals(soqlSegment.getSoqlSegment()));
        }
        return typedThis;
    }

    @Override
    public Children selectCount(R column, boolean isAppend) {
        var countStr = COUNT_FIELD.getSoqlSegment().formatted(columnToString(column));
        if (isAppend) {
            select.add(() -> countStr);
        } else {
            select.clear();
            select.add(() -> countStr);
        }
        return typedThis;
    }

    @Override
    public Children selectCount(R column, String alias, boolean isAppend) {
        if (StringUtils.isBlank(alias)) {
            return selectCount(column, isAppend);
        }
        var countStr = COUNT_FIELD.getSoqlSegment().formatted(columnToString(column)) + SPACE + alias;
        if (isAppend) {
            select.add(() -> countStr);
        } else {
            select.clear();
            select.add(() -> countStr);
        }
        return typedThis;
    }

    @Override
    public Children selectCount(boolean isAppend) {
        if (isAppend) {
            select.add(COUNT);
        } else {
            select.clear();
            select.add(COUNT);
        }
        return typedThis;
    }

    @Override
    public Children selectFieldAll() {
        select.clear();
        select.add(FIELDS_ALL);
        return typedThis;
    }
}
