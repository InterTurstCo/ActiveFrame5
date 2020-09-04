package ru.intertrust.cm.core.dao.impl.access;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.config.AccessMatrixConfig;
import ru.intertrust.cm.core.config.AccessMatrixConfig.BorrowPermissisonsMode;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.config.ReferenceFieldConfig;
import ru.intertrust.cm.core.config.base.Configuration;
import ru.intertrust.cm.core.dao.access.UserGroupGlobalCache;
import ru.intertrust.cm.core.dao.impl.PostgreSqlQueryHelper;
import ru.intertrust.cm.core.dao.impl.utils.ConfigurationExplorerUtils;

import java.util.*;

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
    public static List<ImmutableFieldData> getImmutableParentIds(DomainObject domainObject, String typeName, ConfigurationExplorer configurationExplorer) {
        String domainObjectType = typeName;
        Set<ReferenceFieldConfig> refFields = configurationExplorer.getImmutableReferenceFieldConfigs(domainObjectType);
        List<ImmutableFieldData> parentIds = new ArrayList<ImmutableFieldData>(refFields.size());
        
        //Кроме имени поля нужна информация еще в каком непосредственно типе оно объявлено
        //TODO возможно надо оптимизировать, вынести этот код в configurationExplorer и закэшировать
        String[] types = configurationExplorer.getDomainObjectTypesHierarchyBeginningFromType(domainObjectType);
        for (String type : types) {
            for (ReferenceFieldConfig fieldConfig : refFields) {
                FieldConfig typeFieldConfig = configurationExplorer.getFieldConfig(type, fieldConfig.getName(), false);
                if (typeFieldConfig != null){
                    Id parentObject = domainObject.getReference(fieldConfig.getName());
                    if (parentObject != null){
                        ImmutableFieldData immutableFieldData = new ImmutableFieldData();
                        immutableFieldData.setValue(parentObject);
                        immutableFieldData.setСonfig(fieldConfig);
                        immutableFieldData.setTypeName(type);
                        parentIds.add(immutableFieldData);
                    }
                }
            }
        }
        return parentIds;
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
        return userGroupCache.isAdministrator(personId);
    }

    /**
     * Получение всех дочерних типов переданного типа
     *
     * @param type
     * @return
     */
    public static List<String> getSubTypes(String type, ConfigurationExplorer configurationExplorer) {
        // Получение всех конфигураций доменных объектов
        Collection<DomainObjectTypeConfig> configs = configurationExplorer.getConfigs(DomainObjectTypeConfig.class);
        HashMap<String, HashSet<String>> directInheritors = new HashMap<>(configs.size() / 5);
        for (DomainObjectTypeConfig config : configs) {
            String typeExtended = config.getExtendsAttribute();
            if (typeExtended == null) {
                continue;
            }
            HashSet<String> inheritors = directInheritors.get(typeExtended.toLowerCase());
            if (inheritors == null) {
                inheritors = new HashSet<>();
                directInheritors.put(typeExtended.toLowerCase(), inheritors);
            }
            inheritors.add(config.getName().toLowerCase());
        }
        return getSubTypes(type, directInheritors);
    }

    private static List<String> getSubTypes(String type, HashMap<String, HashSet<String>> directInheritors) {
        // Получение всех конфигураций доменных оьъектов
        HashSet<String> typeInheritors = directInheritors.get(type.toLowerCase());
        if (typeInheritors == null) {
            return Collections.emptyList();
        }
        List<String> result = new ArrayList<>();
        for (String inheritor : typeInheritors) {
            result.add(inheritor);
            result.addAll(getSubTypes(inheritor, directInheritors));
        }
        return result;
    }
    
    public static boolean isCombineMatrixReference(AccessMatrixConfig accessMatrix){
        return accessMatrix!= null 
                && accessMatrix.getMatrixReference() != null 
                && accessMatrix.getBorrowPermissisons() != null 
                && accessMatrix.getBorrowPermissisons() == BorrowPermissisonsMode.read;
    }
}
