package ru.intertrust.cm.core.dao.impl;

import ru.intertrust.cm.core.dao.access.AccessToken;

import static ru.intertrust.cm.core.dao.api.DomainObjectDao.ID_COLUMN;
import static ru.intertrust.cm.core.dao.api.DomainObjectDao.TYPE_COLUMN;
import static ru.intertrust.cm.core.dao.impl.DataStructureNamingHelper.getReferenceTypeColumnName;
import static ru.intertrust.cm.core.dao.impl.DataStructureNamingHelper.getSqlAlias;
import static ru.intertrust.cm.core.dao.impl.utils.DaoUtils.wrap;

/**
 * Класс для генерации sql-запросов для {@link ru.intertrust.cm.core.dao.impl.PersonManagementServiceDaoImpl}
 *
 */
public class PersonManagementQueryHelper extends DomainObjectQueryHelper {

    /**
     * Создает SQL запрос для нахождения персон из группы
     * @param typeName тип доменного объекта
     * @return SQL запрос для нахождения персон из группы
     */
    public String generateFindPersonsInGroupQuery(String typeName, AccessToken accessToken) {
        String tableAlias = getSqlAlias(typeName);

        StringBuilder joinClause = new StringBuilder();
        joinClause.append("inner join ").append(wrap("group_member")).append(" gm on (").
                append("gm.").append(wrap("person_id")).append(" = ").
                append(tableAlias).append(".").append(wrap(ID_COLUMN)).append(" and ").
                append("gm.").append(wrap(getReferenceTypeColumnName("person_id"))).append(" = ").
                append(tableAlias).append(".").append(wrap(TYPE_COLUMN)).append(")");

        StringBuilder whereClause = new StringBuilder();
        whereClause.append("gm.").append(wrap("usergroup")).append("=:id and gm.").
                append(wrap(getReferenceTypeColumnName("usergroup"))).append("=:id_type");

        return generateFindQuery(typeName, accessToken, false, joinClause, whereClause, null);
    }

    /**
     * Создает SQL запрос для нахождения персон группы с учётом наследования
     * @return SQL запрос для нахождения персон группы с учётом наследования
     */
    public String generateFindAllPersonsInGroupQuery(String typeName, AccessToken accessToken) {
        String tableAlias = getSqlAlias(typeName);

        StringBuilder joinClause = new StringBuilder();
        joinClause.append("inner join ").append(wrap("group_member")).append(" gm on (").
                append("gm.").append(wrap("person_id")).append(" = ").
                append(tableAlias).append(".").append(wrap(ID_COLUMN)).append(" and ").
                append("gm.").append(wrap(getReferenceTypeColumnName("person_id"))).append(" = ").
                append(tableAlias).append(".").append(wrap(TYPE_COLUMN)).append(") ");
        joinClause.append("inner join ").append(wrap("group_group")).append(" gg on (").
                append("gg.").append(wrap("child_group_id")).append(" = ").
                append("gm.").append(wrap("usergroup")).append(" and ").
                append("gg.").append(wrap(getReferenceTypeColumnName("child_group_id"))).append(" = ").
                append("gm.").append(wrap(getReferenceTypeColumnName("usergroup"))).append(")");


        StringBuilder whereClause = new StringBuilder();
        whereClause.append("gg.").append(wrap("parent_group_id")).append("=:id and gg.").
                append(wrap(getReferenceTypeColumnName("parent_group_id"))).append("=:id_type");

        return generateFindQuery(typeName, accessToken, false, joinClause, whereClause, null);
    }

    /**
     * Создает SQL запрос для групп, в которые входит пользователь
     * @return SQL запрос для групп, в которые входит пользователь
     */
    public String generateFindPersonGroups(String typeName, AccessToken accessToken) {
        String tableAlias = getSqlAlias(typeName);

        StringBuilder joinClause = new StringBuilder();
        joinClause.append(" inner join ").append(wrap("group_group")).append(" gg on (").
                append("gg.").append(wrap("parent_group_id")).append(" = ").
                append(tableAlias).append(".").append(wrap(ID_COLUMN)).append(" and ").
                append("gg.").append(wrap(getReferenceTypeColumnName("parent_group_id"))).append(" = ").
                append(tableAlias).append(".").append(wrap(TYPE_COLUMN)).append(") ");
        joinClause.append("inner join ").append(wrap("group_member")).append(" gm on (").
                append("gm.").append(wrap("usergroup")).append(" = ").
                append("gg.").append(wrap("child_group_id")).append(" and ").
                append("gm.").append(wrap(getReferenceTypeColumnName("usergroup"))).append(" = ").
                append("gg.").append(wrap("child_group_id")).append(")");

        StringBuilder whereClause = new StringBuilder();
        whereClause.append("gm.").append(wrap("person_id")).append("=:id and gm.").
                append(wrap(getReferenceTypeColumnName("person_id"))).append("=:id_type");

        return generateFindQuery(typeName, accessToken, false, joinClause, whereClause, null);
    }

    /**
     * Создаёт запрос для нахождения всех родительских групп
     * @return запрос для нахождения всех родительских групп
     */
    public String generateFindAllParentGroups(String typeName, AccessToken accessToken) {
        String tableAlias = getSqlAlias(typeName);

        StringBuilder joinClause = new StringBuilder();
        joinClause.append(" inner join ").append(wrap("group_group")).append(" gg on (").
                append("gg.").append(wrap("parent_group_id")).append(" = ").
                append(tableAlias).append(".").append(wrap(ID_COLUMN)).append(" and ").
                append("gg.").append(wrap(getReferenceTypeColumnName("parent_group_id"))).append(" = ").
                append(tableAlias).append(".").append(wrap(TYPE_COLUMN)).append(")");

        StringBuilder whereClause = new StringBuilder();
        whereClause.append("gg.").append(wrap("child_group_id")).append("=:id and gg.").
                append(wrap(getReferenceTypeColumnName("child_group_id"))).append("=:id_type and (").
                append(wrap(tableAlias)).append(".").append(wrap(ID_COLUMN)).append(" <> :id or ").
                append(wrap(tableAlias)).append(".").append(wrap(TYPE_COLUMN)).append(" <> :id_type)");

        return generateFindQuery(typeName, accessToken, false, joinClause, whereClause, null);
    }

    /**
     * Создаёт запрос для нахождения всех наследуемых групп
     * @return запрос для нахождения всех наследуемых групп
     */
    public String generateFindChildGroups(String typeName, AccessToken accessToken) {
        String tableAlias = getSqlAlias(typeName);

        StringBuilder joinClause = new StringBuilder();
        joinClause.append(" inner join ").append(wrap("group_group_settings")).append(" ggs on (").
                append("ggs.").append(wrap("child_group_id")).append(" = ").
                append(tableAlias).append(".").append(wrap(ID_COLUMN)).append(" and ").
                append("ggs.").append(wrap(getReferenceTypeColumnName("child_group_id"))).append(" = ").
                append(tableAlias).append(".").append(wrap(TYPE_COLUMN)).append(")");

        StringBuilder whereClause = new StringBuilder();
        whereClause.append("ggs.").append(wrap("parent_group_id")).append("=:id and ggs.").
                append(wrap(getReferenceTypeColumnName("parent_group_id"))).append("=:id_type");

        return generateFindQuery(typeName, accessToken, false, joinClause, whereClause, null);
    }

    /**
     * Создаёт запрос для нахождения всех порожденных групп с учётом наследования
     * @return запрос для нахождения всех порожденных групп с учётом наследования
     */
    public String generateFindAllChildGroups(String typeName, AccessToken accessToken) {
        String tableAlias = getSqlAlias(typeName);

        StringBuilder joinClause = new StringBuilder();
        joinClause.append(" inner join ").append(wrap("group_group")).append(" gg on (").
                append("gg.").append(wrap("child_group_id")).append(" = ").
                append(tableAlias).append(".").append(wrap(ID_COLUMN)).append(" and ").
                append("gg.").append(wrap(getReferenceTypeColumnName("child_group_id"))).append(" = ").
                append(tableAlias).append(".").append(wrap(TYPE_COLUMN)).append(")");

        StringBuilder whereClause = new StringBuilder();
        whereClause.append("gg.").append(wrap("parent_group_id")).append("=:id and gg.").
                append(wrap(getReferenceTypeColumnName("parent_group_id"))).append("=:id_type and (").
                append(wrap(tableAlias)).append(".").append(wrap(ID_COLUMN)).append(" <> :id or ").
                append(wrap(tableAlias)).append(".").append(wrap(TYPE_COLUMN)).append(" <> :id_type)");

        return generateFindQuery(typeName, accessToken, false, joinClause, whereClause, null);
    }

    /**
     * Создаёт запрос для нахождения динамической группы
     * @return запрос для нахождения динамической группы
     */
    public String generateFindDynamicGroup(String typeName, AccessToken accessToken) {
        String tableAlias = getSqlAlias(typeName);

        StringBuilder whereClause = new StringBuilder();
        whereClause.append(wrap(tableAlias)).append(".").append(wrap("object_id")).append("=:id and ").
                append(wrap(tableAlias)).append(".").append(wrap(getReferenceTypeColumnName("object_id"))).append("=:id_type and ").
                append(wrap(tableAlias)).append(".").append(wrap("group_name")).append("=:name");

        return generateFindQuery(typeName, accessToken, false, null, whereClause, null);
    }
}
