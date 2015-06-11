package ru.intertrust.cm.core.dao.impl.access;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.config.ReferenceFieldConfig;
import ru.intertrust.cm.core.config.base.Configuration;
import ru.intertrust.cm.core.dao.access.UserGroupGlobalCache;
import ru.intertrust.cm.core.dao.impl.PostgreSqlQueryHelper;
import ru.intertrust.cm.core.dao.impl.utils.ConfigurationExplorerUtils;

import java.util.ArrayList;
import java.util.List;

import static ru.intertrust.cm.core.dao.impl.DataStructureNamingHelper.getSqlName;

/**
 * Утилитный класс системы контроля доступа.
 * @author atsvetkov
 *
 */
public class AccessControlUtility {

    public static String getAclTableName(String doTypeName) {
        return getAclTableNameFor(doTypeName);
    }

    public static String getAclTableNameFor(String domainObjectTable) {
        return getSqlName(domainObjectTable + PostgreSqlQueryHelper.ACL_TABLE_SUFFIX);
    }

    public static String getAclReadTableName(ConfigurationExplorer configurationExplorer, String doTypeName) {
        return getAclReadTableNameFor(configurationExplorer, doTypeName);
    }

    public static String getAclReadTableNameFor(ConfigurationExplorer configurationExplorer, String domainObjectTable) {
        String topLevelParentType = ConfigurationExplorerUtils.getTopLevelParentType(configurationExplorer, domainObjectTable);
        return getSqlName(topLevelParentType + PostgreSqlQueryHelper.READ_TABLE_SUFFIX);
    }

    /**
     * Конвертирует список объектов {@see RdbmsId} в список объектов типа {@see Long}
     * @param objectIds
     * @return
     */
    public static List<Long> convertRdbmsIdsToLongIds(List<Id> objectIds) {
        List<Long> idList = new ArrayList<>(objectIds.size());
        for (Id id : objectIds) {
            if (id != null && id.getClass().equals(RdbmsId.class)) {
                idList.add(((RdbmsId) id).getId());
            }
        }
        return idList;
    }
    
    /**
     * Возвращает список идентификаторов неизменяемых (immutable) родительских объектов, на которые ссылается данный
     * объект.
     * @param domainObject
     * @return
     */
    public static Id[] getImmutableParentIds(DomainObject domainObject, ConfigurationExplorer configurationExplorer) {
        String domainObjectType = domainObject.getTypeName();
        DomainObjectTypeConfig domainObjectTypeConfig =
                configurationExplorer.getConfig(DomainObjectTypeConfig.class, domainObjectType);

        List<Id> parentIds = new ArrayList<>();

        for (FieldConfig fieldConfig : domainObjectTypeConfig.getFieldConfigs()) {
            if (fieldConfig instanceof ReferenceFieldConfig) {

                if (((ReferenceFieldConfig) fieldConfig).isImmutable()) {
                    Id parentObject = domainObject.getReference(fieldConfig.getName());
                    parentIds.add(parentObject);
                }
            }
        }
        return parentIds.toArray(new Id[parentIds.size()]);
    }

    public static String getRelevantType(String typeName, ConfigurationExplorer configurationExplorer) {
        if (configurationExplorer.isAuditLogType(typeName)) {
            typeName = typeName.replace(Configuration.AUDIT_LOG_SUFFIX, "");
        }
        return typeName;
    }
    
    public static boolean isAdministratorWithAllPermissions(Id personId, String domainObjectType, UserGroupGlobalCache userGroupCache,
            ConfigurationExplorer configurationExplorer) {
        if (personId == null) {
            return false;
        }
        return userGroupCache.isAdministrator(personId) && configurationExplorer.getAccessMatrixByObjectType(domainObjectType) == null;
    }

}
