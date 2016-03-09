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

    public static final String ID_COL = wrap(ID_COLUMN);
    public static final String OBJECT_ID_COL = wrap("object_id");
    public static final String ACCESS_OBJECT_ID_COL = wrap("access_object_id");
    public static final String GROUP_ID_COL = wrap("group_id");
    public static final String PARENT_GROUP_ID_COL = wrap("parent_group_id");
    @Autowired
    protected ConfigurationExplorer configurationExplorer;

    @Autowired
    protected CurrentUserAccessor currentUserAccessor;
    
    @Autowired
    protected UserGroupGlobalCache userGroupCache;

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
        StringBuilder whereClause = new StringBuilder(50);
        whereClause.append(getSqlAlias(typeName)).append(".").append(ID_COL).append("=:id");

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
        StringBuilder whereClause = new StringBuilder(50);
        whereClause.append(getSqlAlias(typeName)).append(".").append(ID_COL).append(" in (:ids)");

        return generateFindQuery(typeName, accessToken, lock, null, whereClause, null, false);
    }

    /**
     * Создает SQL запрос для нахождения доменного объекта по уникальному ключу
     *
     * @param typeName тип доменного объекта
     * @return SQL запрос для нахождения доменного объекта
     */
    public String generateFindQuery(String typeName, UniqueKeyConfig uniqueKeyConfig, AccessToken accessToken, boolean lock) {
        StringBuilder whereClause = new StringBuilder(50);

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
     * @param accessToken accessToken
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

    public void appendAccessControlLogicToQuery(StringBuilder query, String typeName) {
        //Добавляем учет ReadPermittedToEverybody
        if (accessRightsCheckIsNeeded(typeName)) {
            doAppendAccessControlLogicToQuery(query, typeName, null);
        }
    }

    private void doAppendAccessControlLogicToQuery(StringBuilder query, String typeName, String originalLinkedTypeOrAlias) {
        if (originalLinkedTypeOrAlias == null) {
            originalLinkedTypeOrAlias = DataStructureNamingHelper.getSqlName(typeName);
        }

        // Проверка прав для аудит лог объектов выполняются от имени родительского объекта.
        String baseTypeName = getRelevantType(typeName);

        //В случае заимствованных прав формируем запрос с "чужой" таблицей xxx_read
        String matrixReferenceTypeName = configurationExplorer.getMatrixReferenceTypeName(baseTypeName);
        String childAclReadTable;
        if (matrixReferenceTypeName != null){
            childAclReadTable = AccessControlUtility.getAclReadTableNameFor(configurationExplorer, matrixReferenceTypeName);
        } else{
            childAclReadTable = AccessControlUtility.getAclReadTableNameFor(configurationExplorer, baseTypeName);
        }
        String topLevelParentType = ConfigurationExplorerUtils.getTopLevelParentType(configurationExplorer, baseTypeName);
        String domainObjectBaseTable = DataStructureNamingHelper.getSqlName(topLevelParentType);

        appendWithPart(query);
        if (topLevelParentType.equalsIgnoreCase(baseTypeName)) {
            appendBaseTypeSubQuery(query, originalLinkedTypeOrAlias, childAclReadTable);
        } else {
            final boolean isAuditLog = configurationExplorer.isAuditLogType(typeName);
            appendInheritedTypeSubQuery(query, isAuditLog, originalLinkedTypeOrAlias, childAclReadTable, topLevelParentType, domainObjectBaseTable);
        }
    }

    protected void appendInheritedTypeSubQuery(StringBuilder query, boolean isAuditLog, String originalLinkedTypeOrAlias, String aclReadTable, String topLevelParentType, String domainObjectBaseTable) {
        query.append(" and exists (select 1 from ").append(wrap(aclReadTable)).append(" r");

        if (isAuditLog) {
            String topLevelAuditTable = getALTableSqlName(topLevelParentType);
            query.append(" inner join ").append(wrap(topLevelAuditTable)).append(" pal ")
                    .append("on r.").append(OBJECT_ID_COL).append(" = pal.").append(ACCESS_OBJECT_ID_COL);
        } else {
            query.append(" inner join ").append(DaoUtils.wrap(domainObjectBaseTable)).append(" rt " )
                    .append("on r.").append(OBJECT_ID_COL).append(" = rt.").append(ACCESS_OBJECT_ID_COL);
        }

        query.append(" where r.").append(GROUP_ID_COL).append(" in (select ").append(PARENT_GROUP_ID_COL).append(" from cur_user_groups)");
        if (isAuditLog) {
            query.append(" and ").append(originalLinkedTypeOrAlias).append(".").append(ID_COL).append(" = pal.").append(ID_COL);
        } else {
            query.append(" and rt.").append(ID_COL).append(" = ").append(originalLinkedTypeOrAlias).append(".").append(ID_COL);
        }
        query.append(")");
    }

    protected void appendBaseTypeSubQuery(StringBuilder query, String originalLinkedTypeOrAlias, String childAclReadTable) {
        query.append(" and exists (select 1 from ").append(wrap(childAclReadTable)).append(" r");

        query.append(" where r.").append(GROUP_ID_COL).append(" in (select ").append(PARENT_GROUP_ID_COL).append(" from cur_user_groups) and ");
        query.append("r.").append(OBJECT_ID_COL).append(" = ").append(originalLinkedTypeOrAlias).append(".").append(ACCESS_OBJECT_ID_COL);
        query.append(")");
    }

    protected boolean accessRightsCheckIsNeeded(String typeName) {
        // Проверка прав для аудит лог объектов выполняются от имени родительского объекта.
        typeName = getRelevantType(typeName);

        Id personId = currentUserAccessor.getCurrentUserId();
        boolean isAdministratorWithAllPermissions = isAdministratorWithAllPermissions(personId, typeName);

        //Добавляем учет ReadPermittedToEverybody
        if (!(configurationExplorer.isReadPermittedToEverybody(typeName) || isAdministratorWithAllPermissions)) {
            return true;
        } else {
            return false;
        }
    }

    protected void appendAccessRightsPart(String typeName, AccessToken accessToken, String tableAlias, StringBuilder query, boolean isSingleDomainObject) {
        if (accessRightsCheckIsNeeded(typeName, accessToken)) {
            doAppendAccessControlLogicToQuery(query, typeName, tableAlias);
        }
    }

    protected boolean accessRightsCheckIsNeeded(String typeName, AccessToken accessToken) {
        boolean isDomainObject = configurationExplorer.getConfig(DomainObjectTypeConfig.class, DaoUtils.unwrap(typeName)) != null;

        if (accessToken.isDeferred() && isDomainObject) {
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
            //В полученной матрице получаем флаг read-evrybody и если его нет то добавляем подзапрос с правами
            if (!isReadEveryBody(permissionType) && !isAdministratorWithAllPermissions) {
                return true;
            }
        }

        return false;
    }

    protected boolean isAdministratorWithAllPermissions(Id personId, String domainObjectType) {
        return AccessControlUtility.isAdministratorWithAllPermissions(personId, domainObjectType, userGroupCache, configurationExplorer);
    }

    protected String generateFindQuery(String typeName, AccessToken accessToken, boolean lock, StringBuilder joinClause,
                                       StringBuilder whereClause, StringBuilder orderClause, boolean isSingleDomainObject) {
        String tableAlias = getSqlAlias(typeName);

        StringBuilder query = new StringBuilder(200);
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

    protected void appendWithPart(StringBuilder query) {
        StringBuilder withSubQuery = new StringBuilder();

        String subString = query.substring(0, 3);
        boolean hasWithKeyword = subString.equalsIgnoreCase("with");

        if (!hasWithKeyword) {
            withSubQuery.append("with ");
        }

        withSubQuery.append("cur_user_groups as ").
                append("(select distinct gg.").append(wrap("parent_group_id")).append(" ").
                append("from ").append(wrap("group_member")).append(" gm ").
                append("inner join ").append(wrap("group_group")).append(" gg on gg.").
                append(wrap("child_group_id")).append(" = gm.").append(wrap("usergroup")).
                append(" where gm.").append(wrap("person_id")).append(" = :user_id)");

        if (hasWithKeyword) {
            withSubQuery.append(", ");
            query.insert(5, withSubQuery);
        } else {
            withSubQuery.append(" ");
            query.insert(0, withSubQuery);
        }
    }

    protected boolean isReadEveryBody(String domainObjectType) {
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
        query.append(" on ").append(tableAlias).append(".").append(ID_COL)
                .append(" = ");
        query.append(parentTableAlias).append(".").append(ID_COL);

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
