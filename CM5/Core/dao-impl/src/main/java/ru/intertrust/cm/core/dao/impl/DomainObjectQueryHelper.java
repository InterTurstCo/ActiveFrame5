package ru.intertrust.cm.core.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.config.*;
import ru.intertrust.cm.core.config.base.Configuration;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.access.UserGroupGlobalCache;
import ru.intertrust.cm.core.dao.access.UserSubject;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.dao.impl.access.AccessControlUtility;
import ru.intertrust.cm.core.dao.impl.utils.ConfigurationExplorerUtils;
import ru.intertrust.cm.core.dao.impl.utils.DaoUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ru.intertrust.cm.core.business.api.dto.GenericDomainObject.STATUS_FIELD_NAME;
import static ru.intertrust.cm.core.dao.api.DomainObjectDao.*;
import static ru.intertrust.cm.core.dao.impl.DataStructureNamingHelper.*;
import static ru.intertrust.cm.core.dao.impl.utils.DaoUtils.wrap;

/**
 * Класс для генерации sql-запросов для работы с доменными объектами
 *
 */
public class DomainObjectQueryHelper {

    @Autowired
    private ConfigurationExplorer configurationExplorer;

    @Autowired
    private CurrentUserAccessor currentUserAccessor;
    
    @Autowired
    private UserGroupGlobalCache userGroupCache;

    public void setCurrentUserAccessor(CurrentUserAccessor currentUserAccessor) {
        this.currentUserAccessor = currentUserAccessor;
    }

    public void setUserGroupCache(UserGroupGlobalCache userGroupCache) {
        this.userGroupCache = userGroupCache;
    }

    /**
     * Устанавливает {@link #configurationExplorer}
     *
     * @param configurationExplorer {@link #configurationExplorer}
     */
    public void setConfigurationExplorer(
            ConfigurationExplorer configurationExplorer) {
        this.configurationExplorer = configurationExplorer;
    }


    /**
     * Создает SQL запрос для нахождения доменного объекта
     *
     * @param typeName тип доменного объекта
     * @param lock блокировка доменного объекта от изменений
     * @return SQL запрос для нахождения доменного объекта
     */
    public String generateFindQuery(String typeName, AccessToken accessToken, boolean lock) {
        StringBuilder whereClause = new StringBuilder();
        whereClause.append(getSqlAlias(typeName)).append(".").append(wrap(ID_COLUMN)).append("=:id");

        return generateFindQuery(typeName, accessToken, lock, null, whereClause, null, true);
    }

    /**
     * Создает SQL запрос для нахождения нескольких доменных объектов одного типа
     *
     * @param typeName тип доменного объекта
     * @param lock блокировка доменного объекта от изменений
     * @return SQL запрос для нахождения доменного объекта
     */
    public String generateMultiObjectFindQuery(String typeName, AccessToken accessToken, boolean lock) {
        StringBuilder whereClause = new StringBuilder();
        whereClause.append(getSqlAlias(typeName)).append(".").append(wrap(ID_COLUMN)).append(" in (:ids)");

        return generateFindQuery(typeName, accessToken, lock, null, whereClause, null, false);
    }

    /**
     * Создает SQL запрос для нахождения доменного объекта по уникальному ключу
     *
     * @param typeName тип доменного объекта
     * @return SQL запрос для нахождения доменного объекта
     */
    public String generateFindQuery(String typeName, UniqueKeyConfig uniqueKeyConfig, AccessToken accessToken, boolean lock) {
        StringBuilder whereClause = new StringBuilder();

        String tableAlias = getSqlAlias(typeName);
        int paramCounter = 0;

        for (UniqueKeyFieldConfig uniqueKeyFieldConfig : uniqueKeyConfig.getUniqueKeyFieldConfigs()) {
            if (paramCounter > 0) {
                whereClause.append(" and ");
            }

            String name = uniqueKeyFieldConfig.getName();

            whereClause.append(tableAlias).append(".").append(wrap(getSqlName(name))).append(" = :").
                    append(uniqueKeyFieldConfig.getName().toLowerCase());

            FieldConfig fieldConfig = configurationExplorer.getFieldConfig(typeName, name);
            if (fieldConfig instanceof ReferenceFieldConfig) {
                whereClause.append(" and ");
                whereClause.append(tableAlias).append(".").append(wrap(getReferenceTypeColumnName(name))).append(" = :").
                        append(getReferenceTypeColumnName(name));
            } else if (fieldConfig instanceof DateTimeWithTimeZoneFieldConfig) {
                whereClause.append(" and ");
                whereClause.append(tableAlias).append(".").append(wrap(getTimeZoneIdColumnName(name))).append(" = :").
                        append(getTimeZoneIdColumnName(name));
            }

            paramCounter++;
        }

        return generateFindQuery(typeName, accessToken, lock, null, whereClause, null, true);
    }

    /**
     * Инициализирует параметр c id доменного объекта
     *
     * @param id
     *            идентификатор доменного объекта
     * @return карту объектов содержащую имя параметра и его значение
     */
    public Map<String, Object> initializeParameters(Id id) {
        RdbmsId rdbmsId = (RdbmsId) id;
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("id", rdbmsId.getId());
        parameters.put("id_type", rdbmsId.getTypeId());
        return parameters;
    }

    /**
     * Инициализирует параметр cо списком id доменных объектов
     *
     * @param ids
     *            идентификаторы доменных объектов
     * @return карту объектов содержащую имя параметра и его значение
     */
    public Map<String, Object> initializeParameters(List<Id> ids) {
        Map<String, Object> parameters = new HashMap<>();
        List<Long> idNumbers = AccessControlUtility.convertRdbmsIdsToLongIds(ids);
        parameters.put("ids", idNumbers);

        return parameters;
    }

    /**
     * Инициализирует параметры прав доступа
     *
     * @param accessToken accessToken
     * @return карту объектов содержащую имя параметра и его значение
     */
    public Map<String, Object> initializeParameters(AccessToken accessToken) {
        Map<String, Object> parameters = new HashMap<>();

        if (accessToken.isDeferred()) {
            long userId = ((UserSubject) accessToken.getSubject()).getUserId();
            parameters.put("user_id", userId);
        }

        return parameters;
    }

    /**
     * Инициализирует параметры прав доступа и идентификатора
     *
     * @param accessToken accessToken
     * @param id accessToken
     * @return карту объектов содержащую имена параметров и их значения
     */
    public Map<String, Object> initializeParameters(Id id, AccessToken accessToken) {
        Map<String, Object> parameters = initializeParameters(accessToken);
        parameters.putAll(initializeParameters(id));
        return parameters;
    }

    /**
     * Инициализирует параметры прав доступа и списка идентификаторов
     *
     * @param accessToken accessToken
     * @param id accessToken
     * @return карту объектов содержащую имена параметров и их значения
     */
    public Map<String, Object> initializeParameters(List<Id> ids, AccessToken accessToken) {
        Map<String, Object> parameters = initializeParameters(accessToken);
        parameters.putAll(initializeParameters(ids));
        return parameters;
    }

    /**
     * Возвращает имя типа или исходное имя типа, если передан активити-лог-тип
     * @param typeName имя типа
     * @return имя типа или исходное имя типа, если передан активити-лог-тип
     */
    public String getRelevantType(String typeName) {
        if (configurationExplorer.isAuditLogType(typeName)) {
            typeName = typeName.replace(Configuration.AUDIT_LOG_SUFFIX, "");
        }
        return typeName;
    }

    protected String generateFindQuery(String typeName, AccessToken accessToken, boolean lock, StringBuilder joinClause,
                                     StringBuilder whereClause, StringBuilder orderClause, boolean isSingleDomainObject) {
        String tableAlias = getSqlAlias(typeName);

        StringBuilder query = new StringBuilder();
        query.append("select ");
        appendColumnsQueryPart(query, typeName);

        query.append(" from ");
        appendTableNameQueryPart(query, typeName);
        if (joinClause != null) {
            query.append(" ").append(joinClause);
        }

        query.append(" where ").append(whereClause);
        appendAccessRightsPart(typeName, accessToken, tableAlias, query, isSingleDomainObject);

        if (orderClause != null) {
            query.append(" order by ").append(orderClause);
        }

        if (lock) {
            query.append(" for update");
        }

        return query.toString();
    }

    private void appendAccessRightsPart(String typeName, AccessToken accessToken, String tableAlias, StringBuilder query, boolean isSingleDomainObject) {
        /* IN CASE OF SINGLE DOMAIN OBJECT
         * and exists (
         *      select a."object_id" from "country_read" a
         *      inner join "group_group" gg on a."group_id" = gg."parent_group_id"
         *      inner join "group_member" gm on gg."child_group_id" = gm."usergroup"
         *      inner join "country" o on (o."access_object_id" = a."object_id")
         *      where
         *      gm."person_id" = 4
         *      and o."id" = 29
         *  )
         *
         * IN CASE OF MULTIPLE DOMAIN OBJECTS
         * and exists (
         *      select a."object_id" from "country_read" a
         *      inner join "group_group" gg on a."group_id" = gg."parent_group_id"
         *      inner join "group_member" gm on gg."child_group_id" = gm."usergroup"
         *      where
         *      gm."person_id" = 4
         *      and country1."access_object_id" = a."object_id"
         *      )
         */
        boolean isDomainObject = configurationExplorer.getConfig(DomainObjectTypeConfig.class, DaoUtils.unwrap(typeName)) != null;
        if (accessToken.isDeferred() && isDomainObject) {
            boolean isAuditLog = configurationExplorer.isAuditLogType(typeName);

            // Проверка прав для аудит лог объектов выполняются от имени родительского объекта.
            typeName = getRelevantType(typeName);
            String permissionType = typeName;
            String matrixRefType = configurationExplorer.getMatrixReferenceTypeName(typeName);
            if (matrixRefType != null) {
                permissionType = matrixRefType;
            }

            Id personId = currentUserAccessor.getCurrentUserId();
            boolean isAdministratorWithAllPermissions = AccessControlUtility.isAdministratorWithAllPermissions(personId, typeName, userGroupCache, configurationExplorer);

            //Получаем матрицу для permissionType
            AccessMatrixConfig accessMatrixConfig = configurationExplorer.getAccessMatrixByObjectTypeUsingExtension(permissionType);
            //В полученной матрице получаем флаг read-evrybody и если его нет то добавляем подзапрос с правами
            if (!isReadEveryBody(permissionType) && !isAdministratorWithAllPermissions) {

                //Таблица с правами на read получается с учетом наследования типов
                String aclReadTable = AccessControlUtility
                        .getAclReadTableName(configurationExplorer, permissionType);
                String toplevelParentType = ConfigurationExplorerUtils.getTopLevelParentType(configurationExplorer, typeName);
                String topLevelAuditTable = getALTableSqlName(toplevelParentType);
                String domainObjectBaseTable = DataStructureNamingHelper.getSqlName(toplevelParentType);

                query.append(" and exists (select a.\"object_id\" from ").append(wrap(aclReadTable)).append(" a ");
                query.append(" inner join ").append(wrap("group_group")).append(" gg on a.")
                        .append(wrap("group_id")).append(" = gg.").append(wrap("parent_group_id"));
                query.append(" inner join ").append(wrap("group_member")).append(" gm on gg.")
                        .append(wrap("child_group_id")).append(" = gm.").append(wrap("usergroup"));
                //обавляем в связи с появлением функциональности замещения прав
                if (isSingleDomainObject) {
                    query.append(" inner join ").append(DaoUtils.wrap(domainObjectBaseTable)).append(" o on (o.");
                    query.append(DaoUtils.wrap("access_object_id")).append(" = a.").append(DaoUtils.wrap("object_id")).append(")");
                }
                if (isAuditLog) {
                    query.append(" inner join ").append(wrap(topLevelAuditTable)).append(" pal on ").append(tableAlias).append(".") // todo check usage of tableAlias
                            .append(wrap(Configuration.ID_COLUMN)).append(" = pal.").append(wrap(Configuration.ID_COLUMN));
                }

                query.append(" where gm.").append(wrap("person_id")).append(" = :user_id and ");

                if (isAuditLog) {
                    query.append("o.").append(wrap("id")).append(" = ").append(topLevelAuditTable).append(".").append(DaoUtils.wrap(Configuration.DOMAIN_OBJECT_ID_COLUMN));

                } else {
                    if (isSingleDomainObject) {
                        query.append("o.").append(wrap("id")).append(" = :id");
                    } else {
                        query.append(getSqlAlias(domainObjectBaseTable)).append(".\"access_object_id\" = a.\"object_id\"");
                    }
                }
                query.append(")");
            }

        }
    }

    private boolean isReadEveryBody(String domainObjectType) {
        return configurationExplorer.isReadPermittedToEverybody(domainObjectType);
    }

    private void appendTableNameQueryPart(StringBuilder query, String typeName) {
        String tableName = getSqlName(typeName);
        query.append(wrap(tableName)).append(" ").append(getSqlAlias(tableName));
        appendParentTable(query, typeName);
    }

    private void appendColumnsQueryPart(StringBuilder query, String typeName) {
        DomainObjectTypeConfig config = configurationExplorer.getConfig(
                DomainObjectTypeConfig.class, typeName);

        query.append(getSqlAlias(typeName)).append(".*");

        if (isDerived(config)) {
            appendParentColumns(query, config);
        }
    }

    private void appendParentTable(StringBuilder query, String typeName) {
        DomainObjectTypeConfig config = configurationExplorer.getConfig(
                DomainObjectTypeConfig.class, typeName);

        if (config.getExtendsAttribute() == null) {
            return;
        }

        String tableAlias = getSqlAlias(typeName);

        String parentTableName = getSqlName(config.getExtendsAttribute());
        String parentTableAlias = getSqlAlias(config.getExtendsAttribute());

        query.append(" inner join ").append(wrap(parentTableName)).append(" ")
                .append(parentTableAlias);
        query.append(" on ").append(tableAlias).append(".").append(wrap(ID_COLUMN))
                .append(" = ");
        query.append(parentTableAlias).append(".").append(wrap(ID_COLUMN));

        appendParentTable(query, config.getExtendsAttribute());
    }

    private void appendParentColumns(StringBuilder query,
                                     DomainObjectTypeConfig config) {
        DomainObjectTypeConfig parentConfig = configurationExplorer.getConfig(
                DomainObjectTypeConfig.class, config.getExtendsAttribute());

        appendColumnsExceptId(query, parentConfig);

        if (parentConfig.getExtendsAttribute() != null) {
            appendParentColumns(query, parentConfig);
        } else {
            query.append(", ").append(wrap(CREATED_DATE_COLUMN));
            query.append(", ").append(wrap(UPDATED_DATE_COLUMN));
            query.append(", ").append(wrap(CREATED_BY));
            query.append(", ").append(wrap(CREATED_BY_TYPE_COLUMN));
            query.append(", ").append(wrap(UPDATED_BY));
            query.append(", ").append(wrap(UPDATED_BY_TYPE_COLUMN));

            query.append(", ").append(wrap(STATUS_FIELD_NAME));
            query.append(", ").append(wrap(STATUS_TYPE_COLUMN));

            query.append(", ").append(wrap(ACCESS_OBJECT_ID));
        }
    }

    private void appendColumnsExceptId(StringBuilder query, DomainObjectTypeConfig domainObjectTypeConfig) {
        String tableAlias = getSqlAlias(domainObjectTypeConfig.getName());
        for (FieldConfig fieldConfig : domainObjectTypeConfig.getFieldConfigs()) {
            if (ID_COLUMN.equals(fieldConfig.getName())) {
                continue;
            }

            query.append(", ").append(tableAlias).append(".")
                    .append(wrap(getSqlName(fieldConfig)));

            if (fieldConfig instanceof ReferenceFieldConfig) {
                query.append(", ").append(tableAlias).append(".")
                        .append(wrap(getReferenceTypeColumnName(fieldConfig.getName())));
            } else if (fieldConfig instanceof DateTimeWithTimeZoneFieldConfig) {
                query.append(", ").append(tableAlias).append(".")
                        .append(wrap(getTimeZoneIdColumnName(fieldConfig.getName())));
            }
        }
    }

    private boolean isDerived(DomainObjectTypeConfig domainObjectTypeConfig) {
        return domainObjectTypeConfig.getExtendsAttribute() != null;
    }
}
