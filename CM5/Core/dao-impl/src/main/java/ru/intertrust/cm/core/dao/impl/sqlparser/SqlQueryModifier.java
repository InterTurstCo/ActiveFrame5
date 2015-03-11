package ru.intertrust.cm.core.dao.impl.sqlparser;


import static ru.intertrust.cm.core.dao.api.DomainObjectDao.REFERENCE_POSTFIX;
import static ru.intertrust.cm.core.dao.api.DomainObjectDao.REFERENCE_TYPE_POSTFIX;
import static ru.intertrust.cm.core.dao.api.DomainObjectDao.TIME_ID_ZONE_POSTFIX;
import static ru.intertrust.cm.core.dao.impl.DataStructureNamingHelper.getReferenceTypeColumnName;
import static ru.intertrust.cm.core.dao.impl.DataStructureNamingHelper.getServiceColumnName;
import static ru.intertrust.cm.core.dao.impl.utils.DaoUtils.wrap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.CaseExpression;
import net.sf.jsqlparser.expression.CastExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.JdbcNamedParameter;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.WhenClause;
import net.sf.jsqlparser.expression.operators.arithmetic.Concat;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SetOperationList;
import net.sf.jsqlparser.statement.select.SubSelect;
import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.IdsExcludedFilter;
import ru.intertrust.cm.core.business.api.dto.IdsIncludedFilter;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.DateTimeWithTimeZoneFieldConfig;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.config.ReferenceFieldConfig;
import ru.intertrust.cm.core.dao.exception.CollectionQueryException;
import ru.intertrust.cm.core.dao.exception.DaoException;
import ru.intertrust.cm.core.dao.impl.utils.DaoUtils;
import ru.intertrust.cm.core.model.FatalException;
import ru.intertrust.cm.core.util.ObjectCloner;


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
        SqlQueryParser sqlParser = new SqlQueryParser(query);
        SelectBody selectBody = sqlParser.getSelectBody();

        SelectBody modifiedSelectBody = processQuery(selectBody, new AddServiceColumnsQueryProcessor());
        return modifiedSelectBody.toString();
    }

    /**
     * Оборачивает имена сущностей бд в кавычки и приводит их к нижнему регистру
     * @param query запрос
     * @return модифицированный запрос
     */
    public static String wrapAndLowerCaseNames(String query) {
        SqlQueryParser sqlParser = new SqlQueryParser(query);
        SelectBody selectBody = sqlParser.getSelectBody();

        selectBody.accept(new WrapAndLowerCaseSelectVisitor());
        return selectBody.toString();
    }

    /**
     * Создает count-запрос из select-запроса
     * @param query запрос
     * @return модифицированный запрос
     * throws FatalException если query не является простым select-запросом (используется union и т.п.)
     */
    public static String transformToCountQuery(String query) {
        SqlQueryParser sqlParser = new SqlQueryParser(query);
        SelectBody selectBody = sqlParser.getSelectBody();
        if (!(selectBody instanceof PlainSelect)) {
            throw new FatalException("Counting prototype query must be provided for queries that are not plain selects");
        }

        PlainSelect plainSelect = (PlainSelect) selectBody;
        if (plainSelect.getSelectItems() == null) {
            plainSelect.setSelectItems(new ArrayList<SelectItem>());
        }
        plainSelect.getSelectItems().clear();

        Function countExpression = new Function();
        countExpression.setName("count");
        countExpression.setAllColumns(true);
        plainSelect.getSelectItems().add(new SelectExpressionItem(countExpression));

        return plainSelect.toString();
    }

    public SelectBody addIdBasedFilters(SelectBody selectBody, final List<? extends Filter> filterValues, final String idField) {
        return processQuery(selectBody, new QueryProcessor() {
            @Override
            protected void processPlainSelect(PlainSelect plainSelect) {
                addIdBasedFiltersInPlainSelect(plainSelect, filterValues, idField);
            }
        });
    }    

    public Map<String, FieldConfig> buildColumnToConfigMapForParameters(SelectBody selectBody) {
        final Map<String, FieldConfig> columnToTableMapping = new HashMap<>();

        //TODO перенести всю логику поиска конфигурации колонок в CollectingColumnConfigVisitor
        processQuery(selectBody, new QueryProcessor() {
            @Override
            protected void processPlainSelect(PlainSelect plainSelect) {
                buildColumnToConfigMapInPlainSelect(plainSelect, columnToTableMapping);
            }
        });

        processQuery(selectBody, new QueryProcessor() {
            @Override
            protected void processPlainSelect(PlainSelect plainSelect) {
                buildColumnToConfigMapUsingVisitor(plainSelect, columnToTableMapping);
            }
        });
        
        return columnToTableMapping;
    }
    
    private void buildColumnToConfigMapUsingVisitor(PlainSelect plainSelect, final Map<String, FieldConfig> columnToTableMapping) {        
        CollectingColumnConfigVisitor collectColumnConfigVisitor = new CollectingColumnConfigVisitor(configurationExplorer, plainSelect);        

        plainSelect.accept(collectColumnConfigVisitor);
        
        for (String column : collectColumnConfigVisitor.getColumnToConfigMapping().keySet()) {
            FieldConfig fieldConfig = collectColumnConfigVisitor.getColumnToConfigMapping().get(column);
            if (fieldConfig != null) {
                columnToTableMapping.put(column, fieldConfig);
            }
        }
    }

    public Map<String, FieldConfig> buildColumnToConfigMapForSelectItems(SelectBody selectBody) {
        final Map<String, FieldConfig> columnToTableMapping = new HashMap<>();

        processQuery(selectBody, new QueryProcessor() {
            @Override
            protected void processPlainSelect(PlainSelect plainSelect) {
                buildColumnToConfigMapInPlainSelect(plainSelect, columnToTableMapping);
            }
        });
        processQuery(selectBody, new QueryProcessor() {
            @Override
            protected void processPlainSelect(PlainSelect plainSelect) {
                buildSelectItemConfigMapUsingVisitor(plainSelect, columnToTableMapping);
            }
        });
        
        return columnToTableMapping;
    }
    
    private void buildSelectItemConfigMapUsingVisitor(PlainSelect plainSelect, final Map<String, FieldConfig> columnToTableMapping) {        
        CollectingSelectItemConfigVisitor collectSelectItemConfigVisitor = new CollectingSelectItemConfigVisitor(configurationExplorer, plainSelect);        

        plainSelect.accept(collectSelectItemConfigVisitor);
        
        for (String column : collectSelectItemConfigVisitor.getColumnToConfigMapping().keySet()) {
            FieldConfig fieldConfig = collectSelectItemConfigVisitor.getColumnToConfigMapping().get(column);
            if (fieldConfig != null) {
                columnToTableMapping.put(column, fieldConfig);
            }
        }
    }

    /**
     * Заменяет параметризованный фильтр по Reference полю (например, t.id = {0}) на рабочий вариант этого фильтра
     * {например, t.id = 1 and t.id_type = 2 }
     * @param query SQL запрос
     * @param params список переданных параметров
     * @return
     */
    public String modifyQueryWithParameters(String query, final List<? extends Value> params, final Map<String, FieldConfig> columnToConfigMap,
            final Map<String, Object> parameters) {

        final Map<String, String> replaceExpressions = new HashMap<>();

        String modifiedQuery = processQuery(query, new QueryProcessor() {
            @Override
            protected void processPlainSelect(PlainSelect plainSelect) {
                ReferenceParamsProcessingVisitor modifyReferenceFieldParameter =
                        new ReferenceParamsProcessingVisitor(params, columnToConfigMap);

                plainSelect.accept(modifyReferenceFieldParameter);
                replaceExpressions.putAll(modifyReferenceFieldParameter.getReplaceExpressions());
                parameters.putAll(modifyReferenceFieldParameter.getJdbcParameters());

            }
        });

        for (Map.Entry<String, String> entry : replaceExpressions.entrySet()) {
            modifiedQuery = modifiedQuery.replaceAll(Pattern.quote(entry.getKey()), Matcher.quoteReplacement(entry.getValue()));
        }

        return modifiedQuery;
    }

    /**
     * Заменяет параметризованный фильтр по Reference полю (например, t.id = {0}) на рабочий вариант этого фильтра
     * {например, t.id = 1 and t.id_type = 2 }
     * @param query SQL запрос
     * @param filterValues список фильтров
     * @return
     */
    public String modifyQueryWithReferenceFilterValues(String query, final List<? extends Filter> filterValues, final Map<String, FieldConfig> columnToConfigMap, 
            final Map<String, Object> parameters) {

        final Map<String, String> replaceExpressions = new HashMap<>();

        SqlQueryParser sqlParser = new SqlQueryParser(query);
        SelectBody selectBody = sqlParser.getSelectBody();

        selectBody = processQuery(selectBody, new QueryProcessor() {
            @Override
            protected void processPlainSelect(PlainSelect plainSelect) {
                ReferenceFilterValuesProcessingVisitor visitor = new ReferenceFilterValuesProcessingVisitor(filterValues, columnToConfigMap);

                plainSelect.accept(visitor);
                replaceExpressions.putAll(visitor.getReplaceExpressions());
                parameters.putAll(visitor.getJdbcParameters());
            }
        });

        String modifiedQuery = selectBody.toString();
        for (Map.Entry<String, String> entry : replaceExpressions.entrySet()) {
            modifiedQuery = modifiedQuery.replaceAll(Pattern.quote(entry.getKey()), Matcher.quoteReplacement(entry.getValue()));
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

    public SelectBody addAclQuery(SelectBody selectBody) {
        AddAclVisitor aclVistor = new AddAclVisitor(configurationExplorer);
        selectBody.accept(aclVistor);
        String modifiedQuery = selectBody.toString();
        modifiedQuery = modifiedQuery.replaceAll(USER_ID_PARAM, USER_ID_VALUE);
        SqlQueryParser sqlParser = new SqlQueryParser(modifiedQuery);
        return sqlParser.getSelectBody();
    }

    public void checkDuplicatedColumns(SelectBody selectBody) {
        processQuery(selectBody, new QueryProcessor() {
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
            column = DaoUtils.unwrap(column.toLowerCase());
            if (!columns.add(column)) {
                throw new CollectionQueryException("Collection query contains duplicated columns: " +
                        plainSelect.toString());
            }
        }
    }

    private void addServiceColumnsInPlainSelect(PlainSelect plainSelect) {

        Map<String, FieldConfig> columnToConfigMapForSelectItems = buildColumnToConfigMapForSelectItems(plainSelect);
        
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
                SelectExpressionItem serviceExpressionItem = getServiceExpression(plainSelect, selectExpressionItem, columnToConfigMapForSelectItems);
                if (serviceExpressionItem != null) {
                    selectItems.add(serviceExpressionItem);
                }
            } else if (selectExpressionItem.getExpression() instanceof CaseExpression) {
                CaseExpression caseExpression = (CaseExpression) selectExpressionItem.getExpression();
                boolean returnsId = caseExpressionReturnsId(caseExpression, plainSelect);

                if (returnsId) {
                    // TODO клон CaseExpression не работает
                    ObjectCloner objectCloner = new ObjectCloner();
                    CaseExpression idTypeExpression = objectCloner.cloneObject(caseExpression, caseExpression.getClass());

                    for (Expression whenExpression : idTypeExpression.getWhenClauses()) {
                        WhenClause whenClause = (WhenClause) whenExpression;
                        if (whenClause.getThenExpression() instanceof Column) {
                            Column column = (Column) whenClause.getThenExpression();
                            FieldConfig fieldConfig = columnToConfigMapForSelectItems.get(getColumnName(column));

                            if (fieldConfig instanceof ReferenceFieldConfig) {
                                column.setColumnName(wrap(getReferenceTypeColumnName(column.getColumnName())));
                            }
                        }
                    }

                    if (idTypeExpression.getElseExpression() instanceof Column) {
                        Column column = (Column) idTypeExpression.getElseExpression();
                        FieldConfig fieldConfig = columnToConfigMapForSelectItems.get(getColumnName(column));

                        if (fieldConfig instanceof ReferenceFieldConfig) {
                            column.setColumnName(wrap(getReferenceTypeColumnName(column.getColumnName())));
                        }
                    }

                    SelectExpressionItem idTypeSelectExpressionItem = new SelectExpressionItem();
               idTypeSelectExpressionItem.setExpression(idTypeExpression);
                    if (selectExpressionItem.getAlias() != null) {
                        idTypeSelectExpressionItem.setAlias(
                                new Alias(getReferenceTypeColumnName(DaoUtils.unwrap(selectExpressionItem.getAlias().getName())), false));
                    }
                     
                    if (!containsExpressionInPlainselect(plainSelect, idTypeSelectExpressionItem)) {
                        selectItems.add(idTypeSelectExpressionItem);
                    }
                }
            } else if (selectExpressionItem.getAlias() != null && selectExpressionItem.getAlias().getName().endsWith(REFERENCE_POSTFIX)) {
                Alias alias = selectExpressionItem.getAlias();
                if (selectExpressionItem.getExpression() instanceof NullValue) {
                    SelectExpressionItem referenceFieldTypeItem = new SelectExpressionItem();
                    referenceFieldTypeItem.setAlias(new Alias(getServiceColumnName(DaoUtils.unwrap(alias.getName()), REFERENCE_TYPE_POSTFIX), false));
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
                    referenceFieldTypeItem.setAlias(new Alias(getServiceColumnName(DaoUtils.unwrap(alias.getName()), REFERENCE_TYPE_POSTFIX), false));
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

        if (plainSelect.getGroupByColumnReferences() != null ) {
            List groupByExpressions = new ArrayList(plainSelect.getGroupByColumnReferences().size());
            for (Expression expression : plainSelect.getGroupByColumnReferences()) {
                groupByExpressions.add(expression);

                if (! (expression instanceof Column)) {
                    continue;
                }

                SelectExpressionItem selectExpressionItem = new SelectExpressionItem(expression);
                SelectExpressionItem serviceExpressionItem = getServiceExpression(plainSelect, selectExpressionItem, columnToConfigMapForSelectItems);

                if (serviceExpressionItem == null) {
                    selectExpressionItem = findSelectExpressionItemByAlias(plainSelect, ((Column) expression).getColumnName());
                    if (selectExpressionItem != null) {
                        serviceExpressionItem = getServiceExpression(plainSelect, selectExpressionItem, columnToConfigMapForSelectItems);
                    }
                }
                if (serviceExpressionItem != null) {
                    if (serviceExpressionItem.getAlias() != null && serviceExpressionItem.getAlias().getName() != null &&
                            !serviceExpressionItem.getAlias().getName().isEmpty()) {
                        groupByExpressions.add(new Column(new Table(), serviceExpressionItem.getAlias().getName()));
                    } else {
                        groupByExpressions.add(serviceExpressionItem.getExpression());
                    }
                }
            }

            plainSelect.setGroupByColumnReferences(groupByExpressions);
        }
    }

    private SelectExpressionItem findSelectExpressionItemByAlias(PlainSelect plainSelect, String alias) {
        for (Object selectItem : plainSelect.getSelectItems()) {
            if (!(selectItem instanceof SelectExpressionItem)) {
                continue;
            }

            SelectExpressionItem selectExpressionItem = (SelectExpressionItem) selectItem;
            if (selectExpressionItem.getAlias() != null && alias.equals(selectExpressionItem.getAlias().getName())) {
                return selectExpressionItem;
            }
        }

        return null;
    }

    private SelectExpressionItem getServiceExpression(PlainSelect plainSelect, SelectExpressionItem selectExpressionItem,
            Map<String, FieldConfig> columnToConfigMapForSelectItems) {
        if (!(selectExpressionItem.getExpression() instanceof Column)) {
            return null;
        }

        Column column = (Column) selectExpressionItem.getExpression();
        FieldConfig fieldConfig = columnToConfigMapForSelectItems.get(getColumnName(column));

        if (fieldConfig instanceof ReferenceFieldConfig) {
            return createReferenceFieldTypeSelectItem(selectExpressionItem);
        } else if (fieldConfig instanceof DateTimeWithTimeZoneFieldConfig) {
            return createTimeZoneIdSelectItem(selectExpressionItem);
        }

        return null;
    }

    private String getColumnName(Column column) {
        return DaoUtils.unwrap(column.getColumnName().toLowerCase());
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
            } else if (selectExpressionItem.getExpression() instanceof SubSelect) {

                SubSelect subSelect = (SubSelect) selectExpressionItem.getExpression();
                if (subSelect.getSelectBody() instanceof PlainSelect) {
                    PlainSelect plainSubSelect = (PlainSelect) subSelect.getSelectBody();
                    buildColumnToConfigMapUsingVisitor(plainSubSelect, columnToConfigMap);
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
        idEqualsTo.setLeftExpression(new Column(table, wrap(columnName)));
        idEqualsTo.setRightExpression(jdbcNamedParameter);
        return idEqualsTo;
    }

    private Expression getIdNotEqualsExpression(Filter filter, Integer key, Table table, String columnName, boolean isType) {
        JdbcNamedParameter jdbcNamedParameter = new JdbcNamedParameter();
        jdbcNamedParameter.setName(filter.getFilter() + key + (isType ? REFERENCE_TYPE_POSTFIX : ""));

        NotEqualsTo idNotEqualsTo = new NotEqualsTo();
        idNotEqualsTo.setLeftExpression(new Column(table, wrap(columnName)));
        idNotEqualsTo.setRightExpression(jdbcNamedParameter);
        return idNotEqualsTo;
    }

    /**
     * Возвращает имя таблицы, в которой находится данная колонка. Елси алиас для таблицы не был использован в SQL
     * запросе, то берется название первой таблицы в FROM выражении. Если поле вычисляемое, то возвращается null.
     * Если тип доменного объекта не найден, возвращается null.
     * @param plainSelect SQL запрос
     * @param column колока (поле) в запросе.
     * @return
     */
    public static String getDOTypeName(PlainSelect plainSelect, Column column, boolean forSubSelect) {
        Column clonedColumn = new Column(column.getTable(), column.getColumnName());
        
        if (hasEvaluatedExpressionWithSameAliasInPlainSelect(plainSelect, clonedColumn)) {
            return null;
        }

        if (plainSelect.getFromItem() instanceof SubSelect) {
            SubSelect subSelect = (SubSelect) plainSelect.getFromItem();
            PlainSelect plainSubSelect = getPlainSelect(subSelect.getSelectBody());
            // если название таблицы у колонки совпадает с алиасом подзапроса, то таблица данной колоки - первая таблица
            // из From выражения подзапроса.
            if (clonedColumn.getTable() != null && clonedColumn.getTable().getName() != null && subSelect.getAlias() != null
                    && clonedColumn.getTable().getName().equals(subSelect.getAlias().getName())) {
                clonedColumn.setTable(null);
                return getDOTypeName(plainSubSelect, clonedColumn, true);
            }

            String resultType = processJoins(plainSelect, clonedColumn);
            if (resultType != null) {
                return resultType;
            } else {
                return getDOTypeName(plainSubSelect, clonedColumn, true);
            }
        } else if (plainSelect.getFromItem() instanceof Table) {
            Table fromItem = (Table) plainSelect.getFromItem();

            if (forSubSelect) {
                for (Object selectItem : plainSelect.getSelectItems()) {
                    if (selectItem instanceof AllColumns) {
                        return DaoUtils.unwrap(fromItem.getName());
                    }
                }
            }
            
            // если колока колока не имеет названия таблицы - берется перая таблица из from выражения
            if ((clonedColumn.getTable() == null || clonedColumn.getTable().getName() == null)) {
                return DaoUtils.unwrap(fromItem.getName());
            }

            if ((fromItem.getAlias() != null && clonedColumn.getTable().getName().equals(fromItem.getAlias().getName())) ||
                    clonedColumn.getTable().getName().equals(fromItem.getName())) {
                return DaoUtils.unwrap(fromItem.getName());
            }

            List joinList = plainSelect.getJoins();

            if (joinList != null) {
                for (Object joinObject : joinList) {
                    Join join = (Join) joinObject;

                    if (join.getRightItem() instanceof SubSelect) {
                        SubSelect subSelect = (SubSelect) join.getRightItem();
                        if (clonedColumn.getTable() != null && clonedColumn.getTable().getName() != null) {
                            if (subSelect.getAlias() != null && clonedColumn.getTable().getName().equalsIgnoreCase(subSelect.getAlias().getName())) {
                                PlainSelect plainSubSelect = getPlainSelect(subSelect.getSelectBody());
                                return getDOTypeName(plainSubSelect, clonedColumn, true);
                            }
                        }
                    } else if (join.getRightItem() instanceof Table) {
                        Table joinTable = (Table) join.getRightItem();
                        if (joinTable.getAlias() != null
                                && clonedColumn.getTable().getName().equalsIgnoreCase(joinTable.getAlias().getName()) ||
                                clonedColumn.getTable().getName().equalsIgnoreCase(joinTable.getName())) {
                            return DaoUtils.unwrap(joinTable.getName());
                        }

                    }

                }
            }
        }
        return null;
    }

   public static String prepareColumnNames(PlainSelect plainSelect, Column column, boolean forSubSelect) {
        

        if (plainSelect.getFromItem() instanceof SubSelect) {
            SubSelect subSelect = (SubSelect) plainSelect.getFromItem();
            PlainSelect plainSubSelect = getPlainSelect(subSelect.getSelectBody());
            // если название таблицы у колонки совпадает с алиасом подзапроса, то таблица данной колоки - первая таблица
            // из From выражения подзапроса.
            if (column.getTable() != null && column.getTable().getName() != null && subSelect.getAlias() != null
                    && column.getTable().getName().equals(subSelect.getAlias().getName())) {
                column.setTable(null);
                return prepareColumnNames(plainSubSelect, column, true);
            }

        } else if (plainSelect.getFromItem() instanceof Table) {
            Table fromItem = (Table) plainSelect.getFromItem();

            if (forSubSelect) {
                for (Object selectItem : plainSelect.getSelectItems()) {
                    //если колонка называется как алиас колонки в подзапросе, то имя колонки нужно переопределить - взять имя колонки из подзапроса по алиасу.
                    if (selectItem instanceof SelectExpressionItem) {                        
                        SelectExpressionItem selectExpressionItem = (SelectExpressionItem) selectItem;
                        if (selectExpressionItem.getExpression() instanceof Column) {
                            Column selectColumn = (Column) selectExpressionItem.getExpression();

                            if (selectExpressionItem.getAlias() != null
                                    && column.getColumnName().equalsIgnoreCase(DaoUtils.unwrap(selectExpressionItem.getAlias().getName()))) {
                                column.setColumnName(wrap(selectColumn.getColumnName()));

                                return DaoUtils.unwrap(fromItem.getName());
                            }
                        }
                    }
                }
            }
            

        }
        return null;
    }

    private static String processJoins(PlainSelect plainSelect, Column column) {
        if (column == null) {
            return null;
        }
        List joinList = plainSelect.getJoins();

        if (joinList != null) {
            for (Object joinObject : joinList) {
                Join join = (Join) joinObject;

                if (join.getRightItem() instanceof SubSelect) {
                    SubSelect subSelect = (SubSelect) join.getRightItem();
                    if (column.getTable() != null && column.getTable().getName() != null) {
                        if (subSelect.getAlias() != null && column.getTable().getName().equalsIgnoreCase(subSelect.getAlias().getName())) {
                            PlainSelect plainSubSelect = getPlainSelect(subSelect.getSelectBody());
                            return getDOTypeName(plainSubSelect, column, true);
                        }
                    }
                } else if (join.getRightItem() instanceof Table) {
                    try {
                        Table joinTable = (Table) join.getRightItem();
                        if (joinTable.getAlias() != null && column.getTable() != null && column.getTable().getName() != null) {
                            if (column.getTable().getName().equals(joinTable.getAlias().getName()) ||
                                    column.getTable().getName().equals(joinTable.getName())) {
                                return DaoUtils.unwrap(joinTable.getName());
                            }

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }

            }
        }
        return null;
    }
   
    /**
     * Проверяет, объявлена ли колонка (основного SQL запроса) в подзапросе как вычисляемая колонка
     * @param plainSelect
     * @param column
     * @return
     */
    private static boolean hasEvaluatedExpressionWithSameAliasInPlainSelect(PlainSelect plainSelect, Column column) {
        if (plainSelect.getSelectItems() != null) {
            for (SelectItem selectItem : plainSelect.getSelectItems()) {
                if (!SelectExpressionItem.class.equals(selectItem.getClass())) {
                    continue;
                }
                SelectExpressionItem selectExpressionItem = (SelectExpressionItem) selectItem;
                Expression expressionValue = selectExpressionItem.getExpression();
                if (isEvaluatedExpression(expressionValue)) {

                    if (selectExpressionItem.getAlias() != null) {
                        String columnName = DaoUtils.unwrap(column.getColumnName());
                        String expressionAliasName = DaoUtils.unwrap(selectExpressionItem.getAlias().getName());
                        if (columnName.equalsIgnoreCase(expressionAliasName)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private static boolean isEvaluatedExpression(Expression expressionValue) {
        return expressionValue instanceof StringValue || expressionValue instanceof Function || expressionValue instanceof Concat
                || expressionValue instanceof CaseExpression || expressionValue instanceof CastExpression;
    }

    //TODO
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
                if (selectExpressionItem.getExpression() instanceof CaseExpression) {
                    CaseExpression caseExpression = (CaseExpression) selectExpressionItem.getExpression();
                    if (caseExpressionReturnsId(caseExpression, plainSelect)) {
                        ReferenceFieldConfig fieldConfig = new ReferenceFieldConfig();
                        fieldConfig.setName(DaoUtils.unwrap(selectExpressionItem.getAlias().getName().toLowerCase()));
                        return fieldConfig;
                    }
                }

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

        return "";

//        throw new CollectionQueryException("Unsupported FromItem type.");
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

        SelectExpressionItem referenceFieldTypeItem = new SelectExpressionItem();

        if (selectExpressionItem.getAlias() != null) {
            String serviceColumnAlias = createServiceColumnAlias(selectExpressionItem, postfix);
            referenceFieldTypeItem.setAlias(new Alias(serviceColumnAlias, false));
        }

        String tableName = column.getTable() != null && column.getTable().getName() != null ? column.getTable().getName() : "";
        String columnName = getServiceColumnName(DaoUtils.unwrap(column.getColumnName()), postfix);
        referenceFieldTypeItem.setExpression(new Column(new Table(tableName), columnName));

        return referenceFieldTypeItem;
    }

    private String createServiceColumnAlias(SelectExpressionItem selectExpressionItem, String postfix) {
        String baseAlias = DaoUtils.unwrap(selectExpressionItem.getAlias().getName());
        String serviceColumnAlias = wrap(getServiceColumnName(baseAlias, postfix));
        return serviceColumnAlias;
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

    /**
     * Метод создан для уменьшения количества раз распраршивания SQL запроса. Принимает распаршенный запрос в качестве
     * параметра.
     * @param selectBody
     * @param processor
     * @return
     */
    private SelectBody processQuery(SelectBody selectBody, QueryProcessor processor) {
        selectBody = processor.process(selectBody);
        return selectBody;
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
