package ru.intertrust.cm.core.dao.impl;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.*;
import ru.intertrust.cm.core.dao.exception.CollectionQueryException;
import ru.intertrust.cm.core.dao.impl.access.AccessControlUtility;

import java.util.List;

/**
 * Модифицирует SQL запросы. Добавляет поле Тип Объекта идентификатора в SQL запрос получения данных для коллекции, добавляет ACL фильтр в
 * SQL получения данных данных для коллекции
 * @author atsvetkov
 */
public class SqlQueryModifier {

    private static final String USER_ID_PARAM = "USER_ID_PARAM";
    private static final String USER_ID_VALUE = ":user_id";

    public static final String DOMAIN_OBJECT_TYPE_ALIAS = "TYPE_CONSTANT";

    /**
     * Добавляет поле Тип Объекта идентификатора в SQL запрос получения данных для коллекции. Переданный SQL запрос
     * должен быть запросом чтения (SELECT) либо объединением запросов чтения (UNION). После ключевого слова FROM должно
     * идти название таблицы для Доменного Объекта, тип которго будет типом уникального идентификатора возвращаемых
     * записей.
     * @param query первоначальный SQL запрос
     * @return запрос с добавленным полем Тип Объекта идентификатора
     */
    public String addTypeColumn(String query) {
        String modifiedQuery = null;
        SqlQueryParser sqlParser = new SqlQueryParser(query);

        SelectBody selectBody = sqlParser.getSelectBody();
        if (selectBody.getClass().equals(PlainSelect.class)) {
            PlainSelect plainSelect = (PlainSelect) selectBody;
            addTypeColumnInPlainSelect(plainSelect);

            modifiedQuery = plainSelect.toString();

        } else if (selectBody.getClass().equals(Union.class)) {
            Union union = (Union) selectBody;
            List plainSelects = union.getPlainSelects();
            for (Object plainSelect : plainSelects) {

                addTypeColumnInPlainSelect((PlainSelect) plainSelect);
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

    private void addTypeColumnInPlainSelect(PlainSelect plainSelect) {
        String dominObjectType = getDomainObjectTypeFromSelect(plainSelect);

        List existingSelectItems = plainSelect.getSelectItems();

        SelectExpressionItem objectTypeSelectItem = createObjectTypeSelectItem(dominObjectType);

        existingSelectItems.add(objectTypeSelectItem);
    }

    private String getDomainObjectTypeFromSelect(PlainSelect plainSelect) {
        FromItem fromItem = plainSelect.getFromItem();
        validateFromItem(fromItem);

        String dominObjectType = ((Table) fromItem).getName();
        return dominObjectType;
    }

    private void validateFromItem(FromItem fromItem) {
        if (!fromItem.getClass().equals(Table.class)) {
            throw new CollectionQueryException(
                    "Domain object type should follow immediatly after FROM clause in collection query");
        }
    }

    private SelectExpressionItem createObjectTypeSelectItem(String dominObjectType) {
        SelectExpressionItem objectTypeItem = new SelectExpressionItem();
        objectTypeItem.setAlias(DOMAIN_OBJECT_TYPE_ALIAS);
        objectTypeItem.setExpression(new net.sf.jsqlparser.expression.StringValue("'" + dominObjectType + "'"));
        return objectTypeItem;
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
