package ru.intertrust.cm.core.dao.impl;

import ru.intertrust.cm.core.dao.access.AccessToken;

/**
 * PostgreSql-специфичный класс для генерации sql-запросов для работы с доменными объектами
 *
 */
public class PostgreSqlDomainObjectQueryHelper extends DomainObjectQueryHelper {

    @Override
    public void appendAccessControlLogicToQuery(StringBuilder query, String typeName) {
        super.appendAccessControlLogicToQuery(query, typeName);

        if (accessRightsCheckIsNeeded(typeName)) {
            query.setCharAt(query.length() - 1, ' ');
            query.append("limit 1)");
        }
    }

    @Override
    protected void appendAccessRightsPart(String typeName, AccessToken accessToken, String tableAlias, StringBuilder query, boolean isSingleDomainObject) {
        super.appendAccessRightsPart(typeName, accessToken, tableAlias, query, isSingleDomainObject);

        if (accessRightsCheckIsNeeded(typeName, accessToken)) {
            query.setCharAt(query.length() - 1, ' ');
            query.append("limit 1)");
        }
    }
}
