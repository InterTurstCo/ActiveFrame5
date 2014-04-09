package ru.intertrust.cm.core.dao.impl.sqlparser;


import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.*;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.DateTimeWithTimeZoneFieldConfig;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.config.ReferenceFieldConfig;
import ru.intertrust.cm.core.dao.exception.CollectionQueryException;
import ru.intertrust.cm.core.dao.exception.DaoException;
import ru.intertrust.cm.core.dao.impl.utils.DaoUtils;
import ru.intertrust.cm.core.util.KryoCloner;

import java.util.*;

import static ru.intertrust.cm.core.dao.api.DomainObjectDao.*;
import static ru.intertrust.cm.core.dao.impl.DataStructureNamingHelper.getReferenceTypeColumnName;
import static ru.intertrust.cm.core.dao.impl.DataStructureNamingHelper.getServiceColumnName;


/**
 * Модифицирует SQL запросы. Добавляет поле Тип Объекта идентификатора в SQL запрос получения данных для коллекции, добавляет ACL фильтр в
 * SQL получения данных данных для коллекции
 * @author atsvetkov
 */
public class SqlQueryModifier {

    public static final String USER_ID_PARAM = "USER_ID_PARAM";
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

    public String addIdBasedFilters(String query, final List<? extends Filter> filterValues, final String idField) {
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

        processQuery(query, new QueryProcessor() {
            @Override
            protected void processPlainSelect(PlainSelect plainSelect) {
                buildColumnToConfigMapInWhereClause(plainSelect, columnToTableMapping);
            }
        });
        
        return columnToTableMapping;
    }

    private void buildColumnToConfigMapInWhereClause(PlainSelect plainSelect, final Map<String, FieldConfig> columnToTableMapping) {
        CollectingWhereColumnConfigVisitor collectWhereColumnConfigVisitor = new CollectingWhereColumnConfigVisitor(configurationExplorer, plainSelect);
        if (plainSelect.getWhere() != null) {
            plainSelect.getWhere().accept(collectWhereColumnConfigVisitor);
        }
        columnToTableMapping.putAll(collectWhereColumnConfigVisitor.getWhereColumnToConfigMapping());
    }

    /**
     * Заменяет параметризованный фильтр по Reference полю (например, t.id = {0}) на рабочий вариант этого фильтра
     * {например, t.id = 1 and t.id_type = 2 }
     * @param query SQL запрос
     * @param params список переданных параметров
     * @return
     */
    public String modifyQueryWithParameters(String query, final List<? extends Value> params, final Map<String, FieldConfig> columnToConfigMap) {

        final Map<String, String> replaceExpressions = new HashMap<>();

        String modifiedQuery = processQuery(query, new QueryProcessor() {
            @Override
            protected void processPlainSelect(PlainSelect plainSelect) {
                ReferenceParamsProcessingVisitor modifyReferenceFieldParameter =
                        new ReferenceParamsProcessingVisitor(params, columnToConfigMap);
                if (plainSelect.getWhere() != null) {
                    plainSelect.getWhere().accept(modifyReferenceFieldParameter);
                    replaceExpressions.putAll(modifyReferenceFieldParameter.getReplaceExpressions());
                }
            }
        });

        for (Map.Entry<String, String> entry : replaceExpressions.entrySet()) {
            modifiedQuery = modifiedQuery.replaceAll(entry.getKey(), entry.getValue());
        }

        return modifiedQuery;
    }
    
    /**
     * Добавляет ACL фильтр в SQL получения данных для коллекции.
     * @param query первоначальный запрос
     * @return запрос с добавленным ACL фильтром
     */
    public String addAclQuery(String query) {
        SqlQueryParser sqlParser = new SqlQueryParser(query);
        SelectBody selectBody = sqlParser.getSelectBody();
        AddAclVisitor aclVistor = new AddAclVisitor(configurationExplorer);
        selectBody.accept(aclVistor);
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
            if (selectExpressionItem.getAlias() == null) {
                continue;
            }

            String column = selectExpressionItem.getAlias().getName() + ":" +
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

            if (selectExpressionItem.getExpression() instanceof Column) {
                Column column = (Column) selectExpressionItem.getExpression();
                FieldConfig fieldConfig = getFieldConfig(plainSelect, selectExpressionItem);

                if (fieldConfig instanceof ReferenceFieldConfig) {
                    selectItems.add(createReferenceFieldTypeSelectItem(selectExpressionItem));
                } else if (fieldConfig instanceof DateTimeWithTimeZoneFieldConfig) {
                    selectItems.add(createTimeZoneIdSelectItem(selectExpressionItem));
                }
            } else if (selectExpressionItem.getExpression() instanceof CaseExpression) {
                CaseExpression caseExpression = (CaseExpression) selectExpressionItem.getExpression();
                boolean returnsId = caseExpressionReturnsId(caseExpression, plainSelect);

                if (returnsId) {
                    //TODO клон CaseExpression не работает
                    KryoCloner kryoCloner = new KryoCloner();
                    CaseExpression idTypeExpression = kryoCloner.cloneObject(caseExpression, caseExpression.getClass());

                    for (Expression whenExpression : idTypeExpression.getWhenClauses()) {
                        WhenClause whenClause = (WhenClause) whenExpression;
                        if (whenClause.getThenExpression() instanceof Column) {
                            Column column = (Column) whenClause.getThenExpression();
                            FieldConfig fieldConfig = configurationExplorer.getFieldConfig(
                                    getDOTypeName(plainSelect, column, false), DaoUtils.unwrap(column.getColumnName()));

                            if (fieldConfig instanceof ReferenceFieldConfig) {
                                column.setColumnName(getReferenceTypeColumnName(column.getColumnName()));
                            }
                        }
                    }

                    if (idTypeExpression.getElseExpression() instanceof Column) {
                        Column column = (Column) idTypeExpression.getElseExpression();
                        FieldConfig fieldConfig = configurationExplorer.getFieldConfig(
                                getDOTypeName(plainSelect, column, false), DaoUtils.unwrap(column.getColumnName()));

                        if (fieldConfig instanceof ReferenceFieldConfig) {
                            column.setColumnName(getReferenceTypeColumnName(column.getColumnName()));
                        }
                    }

                    SelectExpressionItem idTypeSelectExpressionItem = new SelectExpressionItem();
                    idTypeSelectExpressionItem.setExpression(idTypeExpression);
                    if (selectExpressionItem.getAlias() != null) {
                        idTypeSelectExpressionItem.setAlias(
                                new Alias(getReferenceTypeColumnName(DaoUtils.unwrap(selectExpressionItem.getAlias().getName()))));
                    }
                     
                    if (!containsExpressionInPlainselect(plainSelect, idTypeSelectExpressionItem)) {
                        selectItems.add(idTypeSelectExpressionItem);
                    }
                }
            } else if (selectExpressionItem.getAlias() != null && selectExpressionItem.getAlias().getName().endsWith(REFERENCE_POSTFIX)) {
                Alias alias = selectExpressionItem.getAlias();
                if (selectExpressionItem.getExpression() instanceof NullValue) {
                    SelectExpressionItem referenceFieldTypeItem = new SelectExpressionItem();
                    referenceFieldTypeItem.setAlias(new Alias(getServiceColumnName(DaoUtils.unwrap(alias.getName()), REFERENCE_TYPE_POSTFIX)));
                    referenceFieldTypeItem.setExpression(new NullValue());
                    selectItems.add(referenceFieldTypeItem);
                } else if (selectExpressionItem.getExpression() instanceof StringValue) {
                    StringValue stringValue = (StringValue) selectExpressionItem.getExpression();
                    RdbmsId id = new RdbmsId(stringValue.getValue());

                    SelectExpressionItem referenceFieldIdItem = new SelectExpressionItem();
                    referenceFieldIdItem.setAlias(alias);
                    referenceFieldIdItem.setExpression(new LongValue(String.valueOf(id.getId())));
                    selectItems.set(selectItems.size() - 1, referenceFieldIdItem);

                    SelectExpressionItem referenceFieldTypeItem = new SelectExpressionItem();
                    referenceFieldTypeItem.setAlias(new Alias(getServiceColumnName(DaoUtils.unwrap(alias.getName()), REFERENCE_TYPE_POSTFIX)));
                    referenceFieldTypeItem.setExpression(new LongValue(String.valueOf(id.getTypeId())));
                    selectItems.add(referenceFieldTypeItem);
                } else {
                    throw new DaoException("Unsupported Id constant type " +
                            selectExpressionItem.getExpression().getClass().getName() +
                            ". Only null and string constants can represent Id");
                }
            }
        }

        plainSelect.setSelectItems(selectItems);
    }

    private boolean containsExpressionInPlainselect(PlainSelect plainSelect, SelectExpressionItem selectExpressionItem) {
        String plainSelectQuery = plainSelect.toString().replaceAll("\\s+", " ").trim();
        String selectExpressionItemQuery = selectExpressionItem.toString().replaceAll("\\s+", " ").trim();
        return plainSelectQuery.indexOf(selectExpressionItemQuery) > 0;        
    }

    private boolean caseExpressionReturnsId(CaseExpression caseExpression, PlainSelect plainSelect) {
        boolean returnsId = false;
        for (Expression whenExpression : caseExpression.getWhenClauses()) {
            WhenClause whenClause = (WhenClause) whenExpression;
            if (whenClause.getThenExpression() instanceof Column) {
                Column column = (Column) whenClause.getThenExpression();
                FieldConfig fieldConfig = configurationExplorer.getFieldConfig(
                        getDOTypeName(plainSelect, column, false), DaoUtils.unwrap(column.getColumnName()));

                if (fieldConfig instanceof ReferenceFieldConfig) {
                    return true;
                }
            }
        }

        if (!returnsId && caseExpression.getElseExpression() instanceof Column) {
            Column column = (Column) caseExpression.getElseExpression();
            FieldConfig fieldConfig = configurationExplorer.getFieldConfig(
                    getDOTypeName(plainSelect, column, false), DaoUtils.unwrap(column.getColumnName()));

            if (fieldConfig instanceof ReferenceFieldConfig) {
                return true;
            }
        }

        return false;
    }

    private void buildColumnToConfigMapInPlainSelect(PlainSelect plainSelect,
                                                            Map<String, FieldConfig> columnToConfigMap) {
        for (Object selectItem : plainSelect.getSelectItems()) {
            if (!(selectItem instanceof SelectExpressionItem)) {
                continue;
            }

            SelectExpressionItem selectExpressionItem = (SelectExpressionItem) selectItem;

            if (selectExpressionItem.getExpression() instanceof Column) {
                Column column = (Column) selectExpressionItem.getExpression();

                String fieldName = DaoUtils.unwrap(column.getColumnName().toLowerCase());
                String columnName = selectExpressionItem.getAlias() != null ?
                        DaoUtils.unwrap(selectExpressionItem.getAlias().getName().toLowerCase()) : fieldName;

                if (columnToConfigMap.get(columnName) == null) {
                    FieldConfig fieldConfig = getFieldConfig(plainSelect, selectExpressionItem);
                    columnToConfigMap.put(columnName, fieldConfig);
                }
            } else if (selectExpressionItem.getExpression() instanceof CaseExpression) {
                CaseExpression caseExpression = (CaseExpression) selectExpressionItem.getExpression();
                boolean returnsId = caseExpressionReturnsId(caseExpression, plainSelect);
                if (returnsId) {
                    addReferenceFieldConfig(selectExpressionItem, columnToConfigMap);
                }
            } else if (selectExpressionItem.getAlias() != null &&
                    selectExpressionItem.getAlias().getName().endsWith(REFERENCE_POSTFIX) &&
                    (selectExpressionItem.getExpression() instanceof NullValue ||
                            selectExpressionItem.getExpression() instanceof LongValue)) {
                addReferenceFieldConfig(selectExpressionItem, columnToConfigMap);
            }
        }
    }

    private void addReferenceFieldConfig(SelectExpressionItem selectExpressionItem, Map<String, FieldConfig> columnToConfigMap) {
        if (selectExpressionItem.getAlias() == null) {
            return;
        }

        String name = DaoUtils.unwrap(selectExpressionItem.getAlias().getName().toLowerCase());
        if (columnToConfigMap.get(name) == null) {
            FieldConfig fieldConfig = new ReferenceFieldConfig();
            fieldConfig.setName(name);
            columnToConfigMap.put(name, fieldConfig);
        }
    }

    private void addIdBasedFiltersInPlainSelect(PlainSelect plainSelect, List<? extends Filter> filterValues,
            String idField) {
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

            // Пустой фильтр разрешенных идентификаторов, поэтому используем условие, гарантирующее пустой результат
            if (expression == null && filter instanceof IdsIncludedFilter) {
                EqualsTo emptyResultExpression = new EqualsTo();
                emptyResultExpression.setLeftExpression(new LongValue("0"));
                emptyResultExpression.setRightExpression(new LongValue("1"));

                expression = emptyResultExpression;
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
    public static String getDOTypeName(PlainSelect plainSelect, Column column, boolean forSubSelect) {

        if (plainSelect.getFromItem() instanceof SubSelect) {
            SubSelect subSelect = (SubSelect) plainSelect.getFromItem();
            PlainSelect plainSubSelect = getPlainSelect(subSelect.getSelectBody());
            return getDOTypeName(plainSubSelect, column, true);
        } else if (plainSelect.getFromItem() instanceof Table) {
            Table fromItem = (Table) plainSelect.getFromItem();

            if (forSubSelect || column.getTable() == null || column.getTable().getName() == null) {
                return DaoUtils.unwrap(fromItem.getName());
            }

            if ((fromItem.getAlias() != null && column.getTable().getName().equals(fromItem.getAlias().getName())) ||
                    column.getTable().getName().equals(fromItem.getName())) {
                return DaoUtils.unwrap(fromItem.getName());
            }

            List joinList = plainSelect.getJoins();
            if (joinList == null || joinList.isEmpty()) {
                throw new CollectionQueryException("Failed to evaluate table name for column '" +
                        column.getColumnName() + "'");
            }

            for (Object joinObject : joinList) {
                Join join = (Join) joinObject;
                
                if (join.getRightItem() instanceof SubSelect) {
                    SubSelect subSelect = (SubSelect) join.getRightItem();
                    PlainSelect plainSubSelect = getPlainSelect(subSelect.getSelectBody());
                    return getDOTypeName(plainSubSelect, column, true);
                } else if(join.getRightItem() instanceof Table){
                    Table joinTable = (Table) join.getRightItem();
                    if (joinTable.getAlias() != null && column.getTable().getName().equals(joinTable.getAlias().getName()) ||
                            column.getTable().getName().equals(joinTable.getName())) {
                        return DaoUtils.unwrap(joinTable.getName());
                    }
                    
                }

            }
        }

        throw new CollectionQueryException("Failed to evaluate table name for column '" +
                column.getColumnName() + "'");
    }

    private FieldConfig getFieldConfig(PlainSelect plainSelect, SelectExpressionItem selectExpressionItem) {
        if (!(selectExpressionItem.getExpression() instanceof Column)) {
            return null;
        }

        Column column = (Column) selectExpressionItem.getExpression();

        if (plainSelect.getFromItem() instanceof SubSelect) {
            SubSelect subSelect = (SubSelect) plainSelect.getFromItem();
            PlainSelect plainSubSelect = getPlainSelect(subSelect.getSelectBody());
            return getFieldConfigFromSubSelect(plainSubSelect, column);
        } else if (plainSelect.getFromItem() instanceof Table) {
            String fieldName = DaoUtils.unwrap(column.getColumnName().toLowerCase());
            return configurationExplorer.getFieldConfig(getDOTypeName(plainSelect, column, false), fieldName);
        }

        return null;
    }

    private FieldConfig getFieldConfigFromSubSelect(PlainSelect plainSelect, Column upperLevelColumn) {
        for (Object selectItem : plainSelect.getSelectItems()) {
            if (selectItem instanceof AllColumns) {
                String fieldName = DaoUtils.unwrap(upperLevelColumn.getColumnName().toLowerCase());
                return configurationExplorer.getFieldConfig(getDOTypeName(plainSelect, upperLevelColumn, true), fieldName);
            } else if (!(selectItem instanceof SelectExpressionItem)) {
                continue;
            }

            SelectExpressionItem selectExpressionItem = (SelectExpressionItem) selectItem;

            if (selectExpressionItem.getAlias() != null && upperLevelColumn.getColumnName().equals(selectExpressionItem.getAlias().getName())) {
                return getFieldConfig(plainSelect, selectExpressionItem);
            }

            if (selectExpressionItem.getExpression() instanceof Column) {
                Column column = (Column) selectExpressionItem.getExpression();
                if (upperLevelColumn.getColumnName().equals(column.getColumnName())) {
                    return getFieldConfig(plainSelect, selectExpressionItem);
                }
            }
        }

        return null;
    }

    private static String getTableAlias(PlainSelect plainSelect) {
        if (plainSelect.getFromItem() instanceof SubSelect) {
            SubSelect subSelect = (SubSelect) plainSelect.getFromItem();
            return subSelect.getAlias() != null ? subSelect.getAlias().getName() : null;
        } else if (plainSelect.getFromItem() instanceof Table) {
            Table table = (Table) plainSelect.getFromItem();
            return table.getAlias() != null ? table.getAlias().getName() : table.getName();
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
        referenceColumnExpression.append(getServiceColumnName(DaoUtils.unwrap(column.getColumnName()), postfix));

        if (selectExpressionItem.getAlias() != null) {
            referenceColumnExpression.append(" as ")
                    .append(getServiceColumnName(selectExpressionItem.getAlias().getName(), postfix));
        }

        SelectExpressionItem referenceFieldTypeItem = new SelectExpressionItem();
        referenceFieldTypeItem.setExpression(new Column(new Table(), referenceColumnExpression.toString()));
        return referenceFieldTypeItem;
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
