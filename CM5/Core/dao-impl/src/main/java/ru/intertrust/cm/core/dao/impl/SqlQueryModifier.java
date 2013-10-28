package ru.intertrust.cm.core.dao.impl;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.*;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.model.DateTimeWithTimeZoneFieldConfig;
import ru.intertrust.cm.core.config.model.FieldConfig;
import ru.intertrust.cm.core.config.model.ReferenceFieldConfig;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.exception.CollectionQueryException;
import ru.intertrust.cm.core.dao.impl.access.AccessControlUtility;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static ru.intertrust.cm.core.dao.api.DomainObjectDao.REFERENCE_TYPE_POSTFIX;
import static ru.intertrust.cm.core.dao.api.DomainObjectDao.TIME_ID_ZONE_POSTFIX;
import static ru.intertrust.cm.core.dao.api.DomainObjectDao.TYPE_COLUMN;
import static ru.intertrust.cm.core.dao.impl.DataStructureNamingHelper.getServiceColumnName;

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
    public String addServiceColumns(String query, ConfigurationExplorer configurationExplorer) {
        String modifiedQuery = null;
        SqlQueryParser sqlParser = new SqlQueryParser(query);

        SelectBody selectBody = sqlParser.getSelectBody();
        if (selectBody.getClass().equals(PlainSelect.class)) {
            PlainSelect plainSelect = (PlainSelect) selectBody;
            addServiceColumnsInPlainSelect(plainSelect, configurationExplorer);
            modifiedQuery = plainSelect.toString();

        } else if (selectBody.getClass().equals(Union.class)) {
            Union union = (Union) selectBody;
            List plainSelects = union.getPlainSelects();
            for (Object plainSelect : plainSelects) {
                addServiceColumnsInPlainSelect((PlainSelect) plainSelect, configurationExplorer);
            }
            modifiedQuery = union.toString();

        } else {
            throw new IllegalArgumentException("Unsupported type of select body: " + selectBody.getClass());

        }

        return modifiedQuery;
    }

    /**
     * Добавляет ACL фильтр в SQL получения данных для коллекции.
     * @param query первоначальный запрос
     * @param idField название поля, использующегося в качестве ключевого
     * @return запрос с добавленным ACL фильтром
     */
    public String addAclQuery(String query, String idField) {

        SqlQueryParser sqlParser = new SqlQueryParser(query);

        SelectBody selectBody = sqlParser.getSelectBody();

        if (selectBody.getClass().equals(PlainSelect.class)) {
            PlainSelect plainSelect = (PlainSelect) selectBody;
            applyAclFilterExpression(idField, plainSelect);

        } else if (selectBody.getClass().equals(Union.class)) {
            Union union = (Union) selectBody;
            List plainSelects = union.getPlainSelects();
            for (Object plainSelect : plainSelects) {
                applyAclFilterExpression(idField, (PlainSelect) plainSelect);
            }
        }

        String modifiedQuery = selectBody.toString();
        modifiedQuery = modifiedQuery.replaceAll(USER_ID_PARAM, USER_ID_VALUE);
        return modifiedQuery;

    }

    public void checkDuplicatedColumns(String query) {
        SqlQueryParser sqlParser = new SqlQueryParser(query);

        SelectBody selectBody = sqlParser.getSelectBody();
        if (selectBody.getClass().equals(PlainSelect.class)) {
            PlainSelect plainSelect = (PlainSelect) selectBody;
            checkDuplicatedColumnsInPlainSelect(plainSelect);
        } else if (selectBody.getClass().equals(Union.class)) {
            Union union = (Union) selectBody;
            List plainSelects = union.getPlainSelects();
            for (Object plainSelect : plainSelects) {
                checkDuplicatedColumnsInPlainSelect((PlainSelect) plainSelect);
            }
        } else {
            throw new IllegalArgumentException("Unsupported type of select body: " + selectBody.getClass());
        }
    }

    private void checkDuplicatedColumnsInPlainSelect(PlainSelect plainSelect) {
        Set<String> columns = new HashSet<>();
        for (Object selectItem : plainSelect.getSelectItems()) {
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

    private String getTableName(PlainSelect plainSelect, Column column) {
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

    private String getFromTableAlias(PlainSelect plainSelect) {
        FromItem fromItem = plainSelect.getFromItem();
        validateFromItem(fromItem);
        return fromItem.getAlias();
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

}
