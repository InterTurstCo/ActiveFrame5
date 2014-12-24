package ru.intertrust.cm.core.dao.impl;

import ru.intertrust.cm.core.dao.access.AccessToken;

import static ru.intertrust.cm.core.dao.api.DomainObjectDao.ID_COLUMN;
import static ru.intertrust.cm.core.dao.api.DomainObjectDao.TYPE_COLUMN;
import static ru.intertrust.cm.core.dao.impl.DataStructureNamingHelper.getReferenceTypeColumnName;
import static ru.intertrust.cm.core.dao.impl.DataStructureNamingHelper.getSqlAlias;
import static ru.intertrust.cm.core.dao.impl.utils.DaoUtils.wrap;

/**
 * Класс для генерации sql-запросов для {@link ru.intertrust.cm.core.dao.impl.SchedulerDaoImpl}
 *
 */
public class SchedulerQueryHelper extends DomainObjectQueryHelper {

    public String generateFindTasksByStatusQuery(String typeName, AccessToken accessToken, boolean activeOnly) {
        String tableAlias = getSqlAlias(typeName);

        StringBuilder joinClause = new StringBuilder();
        joinClause.append("inner join ").append(wrap("status")).append(" s on (").
                append("s.").append(wrap(ID_COLUMN)).append(" = ").
                append(tableAlias).append(".").append(wrap("status")).append(" and ").
                append("s.").append(wrap(TYPE_COLUMN)).append(" = ").
                append(tableAlias).append(".").append(wrap(getReferenceTypeColumnName("status"))).append(")");

        StringBuilder whereClause = new StringBuilder();
        whereClause.append("s").append(".").append(wrap("name")).append("=:status");
        if (activeOnly) {
            whereClause.append(" and active = 1");
        }

        StringBuilder orderClause = new StringBuilder();
        orderClause.append(tableAlias).append(".").append(wrap("priority"));

        return generateFindQuery(typeName, accessToken, false, joinClause, whereClause, orderClause);
    }

    public String generateFindNotInStatusTasksQuery(String typeName, AccessToken accessToken) {
        String tableAlias = getSqlAlias(typeName);

        StringBuilder joinClause = new StringBuilder();
        joinClause.append(" inner join ").append(wrap("status")).append(" s on (").
                append("s.").append(wrap(ID_COLUMN)).append(" = ").
                append(tableAlias).append(".").append(wrap("status")).append(" and ").
                append("s.").append(wrap(TYPE_COLUMN)).append(" = ").
                append(tableAlias).append(".").append(wrap(getReferenceTypeColumnName("status"))).append(")");

        StringBuilder whereClause = new StringBuilder();
        whereClause.append("s").append(".").append(wrap("name")).append("!=:status");

        return generateFindQuery(typeName, accessToken, false, joinClause, whereClause, null);
    }
}
