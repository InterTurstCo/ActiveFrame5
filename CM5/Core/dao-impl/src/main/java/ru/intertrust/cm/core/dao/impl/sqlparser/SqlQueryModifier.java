package ru.intertrust.cm.core.dao.impl.sqlparser;


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
import ru.intertrust.cm.core.dao.exception.CollectionQueryException;
import ru.intertrust.cm.core.dao.impl.access.AccessControlUtility;

import java.util.*;

import static ru.intertrust.cm.core.dao.api.DomainObjectDao.REFERENCE_TYPE_POSTFIX;
import static ru.intertrust.cm.core.dao.api.DomainObjectDao.TIME_ID_ZONE_POSTFIX;
import static ru.intertrust.cm.core.dao.impl.DataStructureNamingHelper.getReferenceTypeColumnName;
import static ru.intertrust.cm.core.dao.impl.DataStructureNamingHelper.getServiceColumnName;
import static ru.intertrust.cm.core.dao.impl.PostgreSqlQueryHelper.unwrap;


/**
 * Модифицирует SQL запросы. Добавляет поле Тип Объекта идентификатора в SQL запрос получения данных для коллекции, добавляет ACL фильтр в
 * SQL получения данных данных для коллекции
 * @author atsvetkov
 */
public class SqlQueryModifier {

    private static final String USER_ID_PARAM = "USER_ID_PARAM";
    private static final String USER_ID_VALUE = ":user_id";

    private ConfigurationExplorer configurationExplorer;

    public SqlQueryModifier(ConfigurationExplorer configurationExplorer) {
        this.configurationExplorer = configurationExplorer;
    }

    /**
     * Добавляет сервисные поля (Тип Объекта идентификатора, идентификатор таймзоны и т.п.) в SQL запрос получения
     * данных для коллекции. Переданный SQL запрос должен быть запросом чтения (SELECT) либо объединением запросов
     * чтения (UNION). После ключевого слова FROM должно идти название таблицы для Доменного Объекта,
     * тип которго будет типом уникального идентификатора возвращаемых записей.
     * @param query первоначальный SQL запрос
     * @return запрос с добавленным полем Тип Объекта идентификатора
     */
    public String addServiceColumns(String query) {
        return processQuery(query, new AddServiceColumnsQueryProcessor());
    }

    public static String wrapAndLowerCaseNames(String query) {
        SqlQueryParser sqlParser = new SqlQueryParser(query);
        SelectBody selectBody = sqlParser.getSelectBody();

        selectBody.accept(new WrapAndLowerCaseSelectVisitor());
        return selectBody.toString();
    }

    public String addIdBasedFilters(String query, final List<Filter> filterValues, final String idField) {
        return processQuery(query, new QueryProcessor() {
            @Override
            protected void processPlainSelect(PlainSelect plainSelect) {
                addIdBasedFiltersInPlainSelect(plainSelect, filterValues, idField);
            }
        });
    }

    public Map<String, FieldConfig> buildColumnToConfigMap(String query) {
        final Map<String, FieldConfig> columnToTableMapping = new HashMap<>();

        processQuery(query, new QueryProcessor() {
            @Override
            protected void processPlainSelect(PlainSelect plainSelect) {
                buildColumnToConfigMapInPlainSelect(plainSelect, columnToTableMapping);
            }
        });

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
                    (selectExpressionItem.getExpression() instanceof Column ?
                        ((Column) selectExpressionItem.getExpression()).getColumnName() : "");
            column = column.toLowerCase();
            if (!columns.add(column)) {
                throw new CollectionQueryException("Collection query contains duplicated columns: " +
                        plainSelect.toString());
            }
        }
    }

    private void addServiceColumnsInPlainSelect(PlainSelect plainSelect) {

        if (plainSelect.getFromItem() instanceof SubSelect) {
            SubSelect subSelect = (SubSelect) plainSelect.getFromItem();
            processSelectBody(subSelect.getSelectBody(), new AddServiceColumnsQueryProcessor());
        }

        List selectItems = new ArrayList(plainSelect.getSelectItems().size());

        for (Object selectItem : plainSelect.getSelectItems()) {
            selectItems.add(selectItem);

            if (!(selectItem instanceof SelectExpressionItem)) {
                continue;
            }

            SelectExpressionItem selectExpressionItem = (SelectExpressionItem) selectItem;

            if (!(selectExpressionItem.getExpression() instanceof Column)) {
                continue;
            }

            Column column = (Column) selectExpressionItem.getExpression();
            FieldConfig fieldConfig = configurationExplorer.getFieldConfig(getDOTypeName(plainSelect, column, false),
                    unwrap(column.getColumnName()));

            if (fieldConfig instanceof ReferenceFieldConfig) {
                selectItems.add(createReferenceFieldTypeSelectItem(selectExpressionItem));
            } else if (fieldConfig instanceof DateTimeWithTimeZoneFieldConfig) {
                selectItems.add(createTimeZoneIdSelectItem(selectExpressionItem));
            }
        }

        plainSelect.setSelectItems(selectItems);
    }

    private void buildColumnToConfigMapInPlainSelect(PlainSelect plainSelect,
                                                            Map<String, FieldConfig> columnToConfigMap) {
        for (Object selectItem : plainSelect.getSelectItems()) {
            if (!(selectItem instanceof SelectExpressionItem)) {
                continue;
            }

            SelectExpressionItem selectExpressionItem = (SelectExpressionItem) selectItem;

            if (!(selectExpressionItem.getExpression() instanceof Column)) {
                continue;
            }

            Column column = (Column) selectExpressionItem.getExpression();

            String fieldName = unwrap(column.getColumnName().toLowerCase());
            String columnName = selectExpressionItem.getAlias() != null ?
                    unwrap(selectExpressionItem.getAlias().toLowerCase()) : fieldName;

            if (columnToConfigMap.get(columnName) == null) {
                FieldConfig fieldConfig =
                        configurationExplorer.getFieldConfig(getDOTypeName(plainSelect, column, false), fieldName);
                columnToConfigMap.put(columnName, fieldConfig);
            }
        }
    }

    private void addIdBasedFiltersInPlainSelect(PlainSelect plainSelect, List<Filter> filterValues, String idField) {
        if (filterValues == null) {
            return;
        }

        Table whereTable = new Table();
        whereTable.setName(getTableAlias(plainSelect));

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
                Parenthesis parenthesis;
                if (filter instanceof IdsIncludedFilter) {
                    IdsIncludedFilter idsIncludedFilter = (IdsIncludedFilter) filter;

                    Expression idExpression = getIdEqualsExpression(idsIncludedFilter, key, whereTable, idField, false);
                    Expression typeExpression = getIdEqualsExpression(idsIncludedFilter, key, whereTable,
                            getReferenceTypeColumnName(idField), true);

                    parenthesis = new Parenthesis(new AndExpression(idExpression, typeExpression));
                    if (expression != null) {
                        expression = new OrExpression(expression, parenthesis);
                    }
                } else {
                    IdsExcludedFilter idsExcludedFilter = (IdsExcludedFilter) filter;

                    Expression idExpression = getIdNotEqualsExpression(idsExcludedFilter, key, whereTable, idField, false);
                    Expression typeExpression = getIdNotEqualsExpression(idsExcludedFilter, key, whereTable,
                            getReferenceTypeColumnName(idField), true);

                    parenthesis = new Parenthesis(new OrExpression(idExpression, typeExpression));
                    if (expression != null) {
                        expression = new AndExpression(expression, parenthesis);
                    }
                }

                if (expression == null) {
                    expression = parenthesis;
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

    private Expression getIdEqualsExpression(IdsIncludedFilter filter, Integer key, Table table, String columnName, boolean isType) {
        JdbcNamedParameter jdbcNamedParameter = new JdbcNamedParameter();
        jdbcNamedParameter.setName(filter.getFilter() + key + (isType ? REFERENCE_TYPE_POSTFIX : ""));

        EqualsTo idEqualsTo = new EqualsTo();
        idEqualsTo.setLeftExpression(new Column(table, columnName));
        idEqualsTo.setRightExpression(jdbcNamedParameter);
        return idEqualsTo;
    }

    private Expression getIdNotEqualsExpression(Filter filter, Integer key, Table table, String columnName, boolean isType) {
        JdbcNamedParameter jdbcNamedParameter = new JdbcNamedParameter();
        jdbcNamedParameter.setName(filter.getFilter() + key + (isType ? REFERENCE_TYPE_POSTFIX : ""));

        NotEqualsTo idNotEqualsTo = new NotEqualsTo();
        idNotEqualsTo.setLeftExpression(new Column(table, columnName));
        idNotEqualsTo.setRightExpression(jdbcNamedParameter);
        return idNotEqualsTo;
    }

    /**
     * Возвращает имя таблицы, в которой находится данная колонка. Елси алиас для таблицы не был использован в SQL
     * запросе, то берется название первой таблицы в FROM выражении.
     * @param plainSelect SQL запрос
     * @param column колока (поле) в запросе.
     * @return
     */
    private static String getDOTypeName(PlainSelect plainSelect, Column column, boolean forSubSelect) {

        if (plainSelect.getFromItem() instanceof SubSelect) {
            SubSelect subSelect = (SubSelect) plainSelect.getFromItem();
            PlainSelect plainSubSelect = getPlainSelect(subSelect.getSelectBody());
            return getDOTypeName(plainSubSelect, column, true);
        } else if (plainSelect.getFromItem() instanceof Table) {
            Table fromItem = (Table) plainSelect.getFromItem();

            if (forSubSelect || column.getTable() == null || column.getTable().getName() == null) {
                return unwrap(fromItem.getName());
            }

            if (column.getTable().getName().equals(fromItem.getAlias()) ||
                    column.getTable().getName().equals(fromItem.getName()) ) {
                return unwrap(fromItem.getName());
            }

            List joinList = plainSelect.getJoins();
            if (joinList == null || joinList.isEmpty()) {
                throw new CollectionQueryException("Failed to evaluate table name for column '" +
                        column.getColumnName() + "'");
            }

            for (Object joinObject : joinList) {
                Join join = (Join) joinObject;

                if (!(join.getRightItem() instanceof Table)) {
                    continue;
                }

                Table joinTable = (Table) join.getRightItem();

                if (column.getTable().getName().equals(joinTable.getAlias()) ||
                        column.getTable().getName().equals(joinTable.getName())) {
                    return unwrap(joinTable.getName());
                }
            }
        }
        
        throw new CollectionQueryException("Failed to evaluate table name for column '" +
                column.getColumnName() + "'");
    }

    private static String getTableAlias(PlainSelect plainSelect) {
        if (plainSelect.getFromItem() instanceof SubSelect) {
            SubSelect subSelect = (SubSelect) plainSelect.getFromItem();
            return subSelect.getAlias() != null ? subSelect.getAlias() : null;
        } else if (plainSelect.getFromItem() instanceof Table) {
            Table table = (Table) plainSelect.getFromItem();
            return table.getAlias() != null ? table.getAlias() : table.getName();
        }

        throw new CollectionQueryException("Unsupported FromItem type.");
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

    private SelectExpressionItem createReferenceFieldTypeSelectItem(SelectExpressionItem selectExpressionItem) {
        return generateServiceColumnExpression(selectExpressionItem, REFERENCE_TYPE_POSTFIX);
    }

    private SelectExpressionItem createTimeZoneIdSelectItem(SelectExpressionItem selectExpressionItem) {
        return generateServiceColumnExpression(selectExpressionItem, TIME_ID_ZONE_POSTFIX);
    }

    private SelectExpressionItem generateServiceColumnExpression(SelectExpressionItem selectExpressionItem, String postfix) {
        Column column = (Column) selectExpressionItem.getExpression();

        StringBuilder referenceColumnExpression = new StringBuilder();
        if (column.getTable() != null && column.getTable().getName() != null) {
            referenceColumnExpression.append(column.getTable().getName()).append(".");
        }
        referenceColumnExpression.append(getServiceColumnName(unwrap(column.getColumnName()), postfix));

        if (selectExpressionItem.getAlias() != null) {
            referenceColumnExpression.append(" as ")
                    .append(getServiceColumnName(selectExpressionItem.getAlias(), postfix));
        }

        SelectExpressionItem referenceFieldTypeItem = new SelectExpressionItem();
        referenceFieldTypeItem.setExpression(new Column(new Table(), referenceColumnExpression.toString()));
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

    private static PlainSelect getPlainSelect(SelectBody selectBody) {
        if (selectBody instanceof PlainSelect) {
            return (PlainSelect) selectBody;
        } else if (selectBody instanceof SetOperationList) {
            SetOperationList union = (SetOperationList) selectBody;
            return union.getPlainSelects().get(0);
        } else {
            throw new IllegalArgumentException("Unsupported type of select body: " + selectBody.getClass());
        }
    }

    private String processQuery(String query, QueryProcessor processor) {
        SqlQueryParser sqlParser = new SqlQueryParser(query);
        SelectBody selectBody = sqlParser.getSelectBody();

        selectBody = processor.process(selectBody);
        return selectBody.toString();
    }

    private SelectBody processSelectBody(SelectBody selectBody, QueryProcessor processor) {
        return processor.process(selectBody);
    }

    private abstract class QueryProcessor {

        public SelectBody process(SelectBody selectBody) {
            if (selectBody.getClass().equals(PlainSelect.class)) {
                PlainSelect plainSelect = (PlainSelect) selectBody;
                processPlainSelect(plainSelect);
                return plainSelect;
            } else if (selectBody.getClass().equals(SetOperationList.class)) {
                SetOperationList union = (SetOperationList) selectBody;
                List plainSelects = union.getPlainSelects();
                for (Object plainSelect : plainSelects) {
                    processPlainSelect((PlainSelect) plainSelect);
                }
                return union;
            } else {
                throw new IllegalArgumentException("Unsupported type of select body: " + selectBody.getClass());
            }
        }

        protected abstract void processPlainSelect(PlainSelect plainSelect);

    }

    private class AddServiceColumnsQueryProcessor extends QueryProcessor {
        @Override
        protected void processPlainSelect(PlainSelect plainSelect) {
            addServiceColumnsInPlainSelect(plainSelect);
        }
    }

}
