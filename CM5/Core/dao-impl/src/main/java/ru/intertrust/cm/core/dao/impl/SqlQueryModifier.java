package ru.intertrust.cm.core.dao.impl;


import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.JdbcNamedParameter;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.*;
import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.IdsExcludedFilter;
import ru.intertrust.cm.core.business.api.dto.IdsIncludedFilter;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.DateTimeWithTimeZoneFieldConfig;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.config.ReferenceFieldConfig;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.exception.CollectionQueryException;
import ru.intertrust.cm.core.dao.impl.access.AccessControlUtility;

import java.util.*;

import static ru.intertrust.cm.core.dao.api.DomainObjectDao.*;
import static ru.intertrust.cm.core.dao.impl.DataStructureNamingHelper.getServiceColumnName;
import static ru.intertrust.cm.core.dao.impl.PostgreSqlQueryHelper.wrap;


/**
 * Модифицирует SQL запросы. Добавляет поле Тип Объекта идентификатора в SQL запрос получения данных для коллекции, добавляет ACL фильтр в
 * SQL получения данных данных для коллекции
 * @author atsvetkov
 */
public class SqlQueryModifier {

    private static final String USER_ID_PARAM = "USER_ID_PARAM";
    private static final String USER_ID_VALUE = ":user_id";

    /**
     * Добавляет сервисные поля (Тип Объекта идентификатора, идентификатор таймзоны и т.п.) в SQL запрос получения
     * данных для коллекции. Переданный SQL запрос должен быть запросом чтения (SELECT) либо объединением запросов
     * чтения (UNION). После ключевого слова FROM должно идти название таблицы для Доменного Объекта,
     * тип которго будет типом уникального идентификатора возвращаемых записей.
     * @param query первоначальный SQL запрос
     * @return запрос с добавленным полем Тип Объекта идентификатора
     */
    public String addServiceColumns(String query, final ConfigurationExplorer configurationExplorer) {
        return processQuery(query, new QueryProcessor() {
            @Override
            protected void processPlainSelect(PlainSelect plainSelect) {
                addServiceColumnsInPlainSelect(plainSelect, configurationExplorer);
            }
        });
    }

    public static String wrapAndLowerCaseNames(String query) {
        return processQuery(query, new QueryProcessor() {
            @Override
            protected void processPlainSelect(PlainSelect plainSelect) {
                wrapAndLowerCaseNamesInPlainSelect(plainSelect);
            }
        });
    }

    public String addIdBasedFilters(String query, final List<Filter> filterValues, final String idField) {
        return processQuery(query, new QueryProcessor() {
            @Override
            protected void processPlainSelect(PlainSelect plainSelect) {
                addIdBasedFiltersInPlainSelect(plainSelect, filterValues, idField);
            }
        });
    }

    public static Map<String, FieldConfig> buildColumnToConfigMap(String query,
                                                            ConfigurationExplorer configurationExplorer) {
        Map<String, FieldConfig> columnToTableMapping = new HashMap<>();

        SqlQueryParser sqlParser = new SqlQueryParser(query);
        SelectBody selectBody = sqlParser.getSelectBody();

        PlainSelect plainSelect = null;
        if (selectBody.getClass().equals(PlainSelect.class)) {
            plainSelect = (PlainSelect) selectBody;
        } else if (selectBody.getClass().equals(SetOperationList.class)) {
            SetOperationList union = (SetOperationList) selectBody;
            plainSelect = union.getPlainSelects().get(0);
        } else {
            throw new IllegalArgumentException("Unsupported type of select body: " + selectBody.getClass());
        }

        for (Object selectItem : plainSelect.getSelectItems()) {
            if (!(selectItem instanceof SelectExpressionItem)) {
                continue;
            }

            SelectExpressionItem selectExpressionItem = (SelectExpressionItem) selectItem;
            Column column = (Column) selectExpressionItem.getExpression();
            FieldConfig fieldConfig = configurationExplorer.getFieldConfig(getTableName(plainSelect, column),
                    column.getColumnName());

            if (selectExpressionItem.getAlias() != null) {
                columnToTableMapping.put(selectExpressionItem.getAlias(), fieldConfig);
            } else {
                columnToTableMapping.put(column.getColumnName(), fieldConfig);
            }
        }

        return columnToTableMapping;
    }

    /**
     * Добавляет ACL фильтр в SQL получения данных для коллекции.
     * @param query первоначальный запрос
     * @param idField название поля, использующегося в качестве ключевого
     * @return запрос с добавленным ACL фильтром
     */
    public String addAclQuery(String query, final String idField) {
        query = processQuery(query, new QueryProcessor() {
            @Override
            protected void processPlainSelect(PlainSelect plainSelect) {
                applyAclFilterExpression(idField, plainSelect);
            }
        });

        SqlQueryParser sqlParser = new SqlQueryParser(query);
        SelectBody selectBody = sqlParser.getSelectBody();
        String modifiedQuery = selectBody.toString();
        modifiedQuery = modifiedQuery.replaceAll(USER_ID_PARAM, USER_ID_VALUE);
        return modifiedQuery;

    }

    public void checkDuplicatedColumns(String query) {
        processQuery(query, new QueryProcessor() {
            @Override
            protected void processPlainSelect(PlainSelect plainSelect) {
                checkDuplicatedColumnsInPlainSelect(plainSelect);
            }
        });
    }

    private void checkDuplicatedColumnsInPlainSelect(PlainSelect plainSelect) {
        Set<String> columns = new HashSet<>();
        for (Object selectItem : plainSelect.getSelectItems()) {
            if (!(selectItem instanceof SelectExpressionItem)) {
                continue;
            }
            SelectExpressionItem selectExpressionItem = (SelectExpressionItem) selectItem;
            String column = selectExpressionItem.getAlias() + ":" +
                    ((Column) selectExpressionItem.getExpression()).getColumnName();
            column = column.toLowerCase();
            if (!columns.add(column)) {
                throw new CollectionQueryException("Collection query contains duplicated columns: " +
                        plainSelect.toString());
            }
        }
    }

    private void addServiceColumnsInPlainSelect(PlainSelect plainSelect, ConfigurationExplorer configurationExplorer) {
        List<SelectExpressionItem> selectExpressionItemsToAdd = new ArrayList<>();

        for (Object selectItem : plainSelect.getSelectItems()) {
            if (!(selectItem instanceof SelectExpressionItem)) {
                continue;
            }

            SelectExpressionItem selectExpressionItem = (SelectExpressionItem) selectItem;
            Column column = (Column) selectExpressionItem.getExpression();
            FieldConfig fieldConfig = configurationExplorer.getFieldConfig(getTableName(plainSelect, column),
                    column.getColumnName());

            if (fieldConfig instanceof ReferenceFieldConfig) {
                if (DomainObjectDao.ID_COLUMN.equalsIgnoreCase(column.getColumnName())) {
                    selectExpressionItemsToAdd.add(createObjectTypeSelectItem(selectExpressionItem));
                } else {
                    selectExpressionItemsToAdd.add(createReferenceFieldTypeSelectItem(selectExpressionItem));
                }
            } else if (fieldConfig instanceof DateTimeWithTimeZoneFieldConfig) {
                selectExpressionItemsToAdd.add(createTimeZoneIdSelectItem(selectExpressionItem));
            }
        }

        plainSelect.getSelectItems().addAll(selectExpressionItemsToAdd);
    }

    private void addIdBasedFiltersInPlainSelect(PlainSelect plainSelect, List<Filter> filterValues, String idField) {
        if (filterValues == null) {
            return;
        }

        Table fromItem = (Table) plainSelect.getFromItem();

        Expression where = plainSelect.getWhere();
        if (where == null) {
            EqualsTo equalsTo = new EqualsTo();
            equalsTo.setLeftExpression(new LongValue("1"));
            equalsTo.setRightExpression(new LongValue("1"));
            plainSelect.setWhere(equalsTo);
            where = plainSelect.getWhere();
        }

        for (Filter filter : filterValues) {
            if (!(filter instanceof IdsIncludedFilter || filter instanceof IdsExcludedFilter)) {
                continue;
            }

            Expression expression = null;
            for (Integer key : filter.getCriterionKeys()){
                Expression idExpression = getIdEqualsExpression(filter, key, fromItem, idField, false);
                Expression typeExpression = ID_COLUMN.equalsIgnoreCase(idField) ?
                        getIdEqualsExpression(filter, key, fromItem, TYPE_COLUMN, true) :
                        getIdEqualsExpression(filter, key, fromItem, idField + REFERENCE_TYPE_POSTFIX, true);

                AndExpression andExpression = new AndExpression(idExpression, typeExpression);
                Parenthesis parenthesis = new Parenthesis(andExpression);

                if (expression == null) {
                    expression = parenthesis;
                } else {
                    if (filter instanceof IdsIncludedFilter) {
                        expression = new OrExpression(expression, parenthesis);
                    } else if (filter instanceof IdsExcludedFilter) {
                        expression = new AndExpression(expression, parenthesis);
                    }
                }
            }

            if (expression != null) {
                if (!(expression instanceof Parenthesis)) {
                    expression = new Parenthesis(expression);
                }
                where = new AndExpression(where, expression);
            }
        }

        plainSelect.setWhere(where);
    }

    private Expression getIdEqualsExpression(Filter filter, Integer key, Table table, String columnName, boolean isType) {
        JdbcNamedParameter jdbcNamedParameter = new JdbcNamedParameter();
        jdbcNamedParameter.setName(filter.getFilter() + key + (isType ? REFERENCE_TYPE_POSTFIX : ""));

        if (filter instanceof IdsIncludedFilter) {
            EqualsTo idEqualsTo = new EqualsTo();
            idEqualsTo.setLeftExpression(new Column(table, columnName));
            idEqualsTo.setRightExpression(jdbcNamedParameter);
            return idEqualsTo;
        } else if (filter instanceof IdsExcludedFilter) {
            NotEqualsTo idNotEqualsTo = new NotEqualsTo();
            idNotEqualsTo.setLeftExpression(new Column(table, columnName));
            idNotEqualsTo.setRightExpression(jdbcNamedParameter);
            return idNotEqualsTo;
        } else {
            throw new IllegalArgumentException("IdsIncluded and IdsExcluded filters supported only");
        }
    }

    private static void wrapAndLowerCaseNamesInPlainSelect(PlainSelect plainSelect) {
        Table fromItem = (Table) plainSelect.getFromItem();
        fromItem.setName(wrap(fromItem.getName().toLowerCase()));


        for (Object selectItem : plainSelect.getSelectItems()) {
            if (!(selectItem instanceof SelectExpressionItem)) {
                continue;
            }

            SelectExpressionItem selectExpressionItem = (SelectExpressionItem) selectItem;
            Column column = (Column) selectExpressionItem.getExpression();
            column.setColumnName(wrap(column.getColumnName().toLowerCase()));
        }
    }

    private static String getTableName(PlainSelect plainSelect, Column column) {
        Table fromItem = (Table) plainSelect.getFromItem();
        if (column.getTable().getName().equals(fromItem.getAlias())) {
            return fromItem.getName();
        }

        List joinList = plainSelect.getJoins();
        if (joinList == null || joinList.isEmpty()) {
            throw new CollectionQueryException("Failed to evaluate table name for column '" +
                    column.getColumnName() + "'");
        }

        for (Object joinObject : joinList) {
            Join join = (Join) joinObject;
            if (column.getTable().getName().equals(join.getRightItem().getAlias())) {
                return ((Table) join.getRightItem()).getName();
            }
        }

        throw new CollectionQueryException("Failed to evaluate table name for column '" +
                column.getColumnName() + "'");
    }

    private String getDomainObjectTypeFromSelect(PlainSelect plainSelect) {
        FromItem fromItem = plainSelect.getFromItem();
        validateFromItem(fromItem);
        return ((Table) fromItem).getName();
    }

    private void validateFromItem(FromItem fromItem) {
        if (!fromItem.getClass().equals(Table.class)) {
            throw new CollectionQueryException(
                    "Domain object type should follow immediatly after FROM clause in collection query");
        }
    }

    private SelectExpressionItem createObjectTypeSelectItem(SelectExpressionItem selectExpressionItem) {
        Column column = (Column) selectExpressionItem.getExpression();
        StringBuilder expression = new StringBuilder(column.getTable().getName()).append(".").append(TYPE_COLUMN);
        if (selectExpressionItem.getAlias() != null) {
            expression.append(" as ").append(selectExpressionItem.getAlias()).append(REFERENCE_TYPE_POSTFIX);
        }

        SelectExpressionItem objectTypeItem = new SelectExpressionItem();
        objectTypeItem.setExpression(new Column(new Table(), expression.toString()));
        return objectTypeItem;
    }

    private SelectExpressionItem createReferenceFieldTypeSelectItem(SelectExpressionItem selectExpressionItem) {
        return generateServiceColumnExpression(selectExpressionItem, REFERENCE_TYPE_POSTFIX);
    }

    private SelectExpressionItem createTimeZoneIdSelectItem(SelectExpressionItem selectExpressionItem) {
        return generateServiceColumnExpression(selectExpressionItem, TIME_ID_ZONE_POSTFIX);
    }

    private SelectExpressionItem generateServiceColumnExpression(SelectExpressionItem selectExpressionItem, String postfix) {
        Column column = (Column) selectExpressionItem.getExpression();
        StringBuilder expression = new StringBuilder(column.getTable().getName()).append(".").
                append(getServiceColumnName(column.getColumnName(), postfix));

        if (selectExpressionItem.getAlias() != null) {
            expression.append(" as ").append(getServiceColumnName(selectExpressionItem.getAlias(), postfix));
        }

        SelectExpressionItem referenceFieldTypeItem = new SelectExpressionItem();
        referenceFieldTypeItem.setExpression(new Column(new Table(), expression.toString()));
        return referenceFieldTypeItem;
    }


    private void applyAclFilterExpression(String idField, PlainSelect plainSelect) {
        String domainObjectType = getDomainObjectTypeFromSelect(plainSelect);

        Expression aclExpression = createAclExpression(domainObjectType, idField);
        Expression oldWhereExpression = plainSelect.getWhere();

        if (oldWhereExpression == null) {
            plainSelect.setWhere(aclExpression);
        } else {
            AndExpression newWhereExpression = new AndExpression(aclExpression, oldWhereExpression);
            plainSelect.setWhere(newWhereExpression);
        }
    }

    private Expression createAclExpression(String domainObjectType, String idField) {
        StringBuilder aclQuery = new StringBuilder();

        String aclReadTable = AccessControlUtility.getAclReadTableNameFor(domainObjectType);
        aclQuery.append("Select * from " + domainObjectType + " where exists (select r.object_id from ")
                .append(aclReadTable).append(" r ");
        aclQuery.append("inner join group_member gm on r.group_id = gm.usergroup where gm.person_id = " + USER_ID_PARAM
                + " and r.object_id = ");
        aclQuery.append(idField).append(")");

        SqlQueryParser aclSqlParser = new SqlQueryParser(aclQuery.toString());
        Expression aclExpression = ((PlainSelect) aclSqlParser.getSelectBody()).getWhere();
        return aclExpression;
    }

    private static String processQuery(String query, QueryProcessor processor) {
        return processor.process(query);
    }

    private static abstract class QueryProcessor {

        public String process(String query) {
            String modifiedQuery = null;
            SqlQueryParser sqlParser = new SqlQueryParser(query);

            SelectBody selectBody = sqlParser.getSelectBody();
            if (selectBody.getClass().equals(PlainSelect.class)) {
                PlainSelect plainSelect = (PlainSelect) selectBody;
                processPlainSelect(plainSelect);
                modifiedQuery = plainSelect.toString();
            } else if (selectBody.getClass().equals(SetOperationList.class)) {
                SetOperationList union = (SetOperationList) selectBody;
                List plainSelects = union.getPlainSelects();
                for (Object plainSelect : plainSelects) {
                    processPlainSelect((PlainSelect) plainSelect);
                }
                modifiedQuery = union.toString();
            } else {
                throw new IllegalArgumentException("Unsupported type of select body: " + selectBody.getClass());
            }

            return modifiedQuery;
        }

        protected abstract void processPlainSelect(PlainSelect plainSelect);

    }

}
