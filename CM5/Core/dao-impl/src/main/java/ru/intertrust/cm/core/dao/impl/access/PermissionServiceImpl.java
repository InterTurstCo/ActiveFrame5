package ru.intertrust.cm.core.dao.impl.access;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.RdbmsId;
import ru.intertrust.cm.core.config.ConfigurationException;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.model.AccessMatrixConfig;
import ru.intertrust.cm.core.config.model.BaseOperationPermitConfig;
import ru.intertrust.cm.core.config.model.BasePermit;
import ru.intertrust.cm.core.config.model.ContextRoleConfig;
import ru.intertrust.cm.core.config.model.CreateChildConfig;
import ru.intertrust.cm.core.config.model.DeleteConfig;
import ru.intertrust.cm.core.config.model.DynamicGroupConfig;
import ru.intertrust.cm.core.config.model.ExecuteActionConfig;
import ru.intertrust.cm.core.config.model.GroupConfig;
import ru.intertrust.cm.core.config.model.PermitGroup;
import ru.intertrust.cm.core.config.model.PermitRole;
import ru.intertrust.cm.core.config.model.ReadConfig;
import ru.intertrust.cm.core.config.model.WriteConfig;
import ru.intertrust.cm.core.config.model.doel.DoelExpression;
import ru.intertrust.cm.core.dao.access.AccessType;
import ru.intertrust.cm.core.dao.access.CreateChildAccessType;
import ru.intertrust.cm.core.dao.access.DomainObjectAccessType;
import ru.intertrust.cm.core.dao.access.ExecuteActionAccessType;
import ru.intertrust.cm.core.dao.access.PermissionService;

/**
 * Реализвация сервиса обновления списков доступа.
 * @author atsvetkov
 */
public class PermissionServiceImpl extends BaseDynamicGroupServiceImpl implements PermissionService {

    @Autowired
    private ConfigurationExplorer configurationExplorer;


    public void setConfigurationExplorer(ConfigurationExplorer configurationExplorer) {
        this.configurationExplorer = configurationExplorer;
        doelResolver.setConfigurationExplorer(configurationExplorer);
    }

    @Override
    public void refreshAclFor(Id objectId) {
        RdbmsId rdbmsId = (RdbmsId) objectId;
        String domainObjectType = domainObjectTypeIdCache.getName(rdbmsId.getTypeId());
        String status = getStatusFor(objectId);
        AccessMatrixConfig accessMatrixConfig =
                configurationExplorer.getAccessMatrixByObjectTypeAndStatus(domainObjectType, status);

        if (accessMatrixConfig == null || accessMatrixConfig.getStatus() == null
                || accessMatrixConfig.getStatus().getPermissions() == null) {
            return;
        }

        cleanAclFor(objectId);

        for (BaseOperationPermitConfig operationPermitConfig : accessMatrixConfig.getStatus().getPermissions()) {
            AccessType accessType = getAccessType(operationPermitConfig);
            processOperationPermissions(objectId, operationPermitConfig, accessType);
        }

    }

    /**
     * Определяет тип операции {@link AccessType} по конфигурации операции.
     * @param operationPermitConfig конфигурации операции
     * @return тип операции
     */
    private AccessType getAccessType(BaseOperationPermitConfig operationPermitConfig) {
        AccessType accessType = null;
        if (operationPermitConfig.getClass().equals(ReadConfig.class)) {
            accessType = DomainObjectAccessType.READ;
        } else if (operationPermitConfig.getClass().equals(WriteConfig.class)) {
            accessType = DomainObjectAccessType.WRITE;
        } else if (operationPermitConfig.getClass().equals(DeleteConfig.class)) {
            accessType = DomainObjectAccessType.DELETE;
        } else if (operationPermitConfig.getClass().equals(ExecuteActionConfig.class)) {
            String actionName = ((ExecuteActionConfig) operationPermitConfig).getName();
            accessType = new ExecuteActionAccessType(actionName);
        } else if (operationPermitConfig.getClass().equals(CreateChildConfig.class)) {
            String childType = ((CreateChildConfig) operationPermitConfig).getType();
            accessType = new CreateChildAccessType(childType);
        }

        return accessType;
    }

    /**
     * Обрабатывает все разрешения на выполнение переданной операции.
     * @param objectId идентификатор доменного объекта, для которого расчитывается список доступа
     * @param operationPermitConfig конфигурация разрешений для операции
     * @param accessType тип операции
     */
    private void processOperationPermissions(Id objectId, BaseOperationPermitConfig operationPermitConfig,
            AccessType accessType) {
        RdbmsId rdbmsId = (RdbmsId) objectId;
        String domainObjectType = domainObjectTypeIdCache.getName(rdbmsId.getTypeId());

        for (BasePermit permit : operationPermitConfig.getPermitConfigs()) {
            if (permit.getClass().equals(PermitRole.class)) {
                String contextRoleName = permit.getName();
                ContextRoleConfig contextRoleConfig =
                        configurationExplorer.getContextRoleByName(contextRoleName);
                if (contextRoleConfig == null) {
                    throw new ConfigurationException("Context role : " + contextRoleName
                            + " not found in configuaration");

                }
                validateRoleContextType(domainObjectType, contextRoleConfig);

                for (Object groupConfig : contextRoleConfig.getGroups().getGroups()) {
                    if (groupConfig.getClass().equals(GroupConfig.class)) {
                        processAclForDynamicGroup(objectId, groupConfig, accessType);

                    }
                }
            } else if (permit.getClass().equals(PermitGroup.class)) {
                String dynamicGroupName = permit.getName();
                
                DynamicGroupConfig dynamicGroupConfig = findAndCheckDynamicGroupByName(dynamicGroupName);
                
                if (dynamicGroupConfig.getContext() != null
                        && dynamicGroupConfig.getContext().getDomainObject() != null) {

                    // контекстным объектом являетя текущий объект (для которого пересчитываются списки доступа)
                    Long contextObjectId = ((RdbmsId) objectId).getId();
                    processAclForDynamicGroupWithContext(objectId, accessType, dynamicGroupName, contextObjectId);

                } else {
                    processAclForDynamicGroupWithoutContext(objectId, accessType, dynamicGroupName);

                }
                                
            }
        }
    }

    private void processAclForDynamicGroupWithContext(Id objectId, AccessType accessType, String dynamicGroupName,
            Long contextObjectId) {
        Id dynamicGroupId = getUserGroupByGroupNameAndObjectId(dynamicGroupName, contextObjectId);
        insertAclRecord(accessType, objectId, dynamicGroupId);
    }

    private DynamicGroupConfig findAndCheckDynamicGroupByName(String dynamicGroupName) {
        DynamicGroupConfig dynamicGroupConfig =
                configurationExplorer.getDynamicGroupByName(dynamicGroupName);
        if (dynamicGroupConfig == null) {
            throw new ConfigurationException("Dynamic Group : " + dynamicGroupName
                    + " not found in configuaration");

        }
        return dynamicGroupConfig;
    }

    /**
     * Пересчитывает список доступа для динамичсекой группы для переданного доменного объекта.
     * @param objectId идентификатор доменного объекта, для которого пересчитывается список доступа
     * @param roleGroupConfig конфигурация динамической группы
     * @param accessType тип доступа для динамичской группы
     */
    private void processAclForDynamicGroup(Id objectId, Object roleGroupConfig, AccessType accessType) {
        GroupConfig groupConfig = (GroupConfig) roleGroupConfig;
        String dynamicGroupName = groupConfig.getName();
        
        DynamicGroupConfig dynamicGroupConfig = findAndCheckDynamicGroupByName(dynamicGroupName);
        
        if (dynamicGroupConfig.getContext() != null && dynamicGroupConfig.getContext().getDomainObject() != null) {
        
            if (groupConfig.getBindContext() != null && groupConfig.getBindContext().getDoel() != null) {
                String doel = groupConfig.getBindContext().getDoel();
                List<Long> contextObjectids = getDynamicGroupContextObject(objectId, doel);
                for (Long contextObjectid : contextObjectids) {
                    processAclForDynamicGroupWithContext(objectId, accessType, dynamicGroupName, contextObjectid);

                }
            } else {
                // если путь к контекстному объекту не указан внутри тега group, то контекстным объектом является
                // текущий объект
                Long contextObjectId = ((RdbmsId) objectId).getId();
                processAclForDynamicGroupWithContext(objectId, accessType, dynamicGroupName, contextObjectId);
            }
            
        } else {
            processAclForDynamicGroupWithoutContext(objectId, accessType, dynamicGroupName);

        }
    }

    private void processAclForDynamicGroupWithoutContext(Id objectId, AccessType accessType, String dynamicGroupName) {
        Id dynamicGroupId = getUserGroupByGroupName(dynamicGroupName);
        if(dynamicGroupId == null) {
            dynamicGroupId = createUserGroup(dynamicGroupName, null);
        }
        insertAclRecord(accessType, objectId, dynamicGroupId);
    }

    /**
     * Добавляет запись в _ACl (_READ) таблицу.
     * @param accessType тип доступа
     * @param objectId идентификатор доменного объекта
     * @param dynamicGroupId идентификатор группы пользователей
     */
    private void insertAclRecord(AccessType accessType, Id objectId, Id dynamicGroupId) {
        RdbmsId rdbmsObjectId = (RdbmsId) objectId;
        RdbmsId rdbmsDynamicGroupId = (RdbmsId) dynamicGroupId;

        String query = null;
        if (accessType == DomainObjectAccessType.READ) {
            query = generateInsertAclReadRecordQuery(rdbmsObjectId);
        } else {
            query = generateInsertAclRecordQuery(rdbmsObjectId);

        }

        Map<String, Object> parameters =
                initializeInsertAclRecordParameters(accessType, rdbmsObjectId, rdbmsDynamicGroupId);
        jdbcTemplate.update(query, parameters);

    }

    private String generateInsertAclReadRecordQuery(RdbmsId objectId) {
        String tableName = null;
        tableName = AccessControlUtility.getAclReadTableName(domainObjectTypeIdCache.getName(objectId.getTypeId()));

        StringBuilder query = new StringBuilder();
        query.append("insert  into ");
        query.append(tableName).append(" (object_id, group_id)");
        query.append(" values (:object_id, :group_id)");

        return query.toString();
    }

    private String generateInsertAclRecordQuery(RdbmsId objectId) {
        String tableName = null;
        tableName = AccessControlUtility.getAclTableName(domainObjectTypeIdCache.getName(objectId.getTypeId()));

        StringBuilder query = new StringBuilder();
        query.append("insert  into ");
        query.append(tableName).append(" (operation, object_id, group_id)");
        query.append(" values (:operation, :object_id, :group_id)");

        return query.toString();
    }

    private Map<String, Object> initializeInsertAclRecordParameters(AccessType accessType, RdbmsId rdbmsObjectId,
            RdbmsId rdbmsDynamicGroupId) {

        Map<String, Object> parameters = new HashMap<String, Object>();
        if (!(accessType == DomainObjectAccessType.READ)) {
            String accessTypeCode = PostgresDatabaseAccessAgent.makeAccessTypeCode(accessType);
            parameters.put("operation", accessTypeCode);
        }

        parameters.put("object_id", rdbmsObjectId.getId());
        parameters.put("group_id", rdbmsDynamicGroupId.getId());

        return parameters;
    }

    /**
     * Выполняет поиск контекстного объекта динамической группы по отслеживаемому объекту матрицы доступа и doel
     * выражению.
     * @param objectId отслеживаемый объект матрицы
     * @param doel выражение внутри тега <bind-context>
     * @return
     */
    private List<Long> getDynamicGroupContextObject(Id objectId, String doel) {
        DoelExpression expr = DoelExpression.parse(doel);
        List<?> result = doelResolver.evaluate(expr, objectId);

        List<Map<String, Object>> contextObjects = (List<Map<String, Object>>) result;
        List<Long> contextObjectIds = new ArrayList<Long>();

        if (contextObjects != null && contextObjects.size() > 0) {
            for (Map<String, Object> contextObject : contextObjects) {
                if (contextObject.values() != null && contextObject.values().size() > 0) {
                    contextObjectIds.add((Long) contextObject.values().iterator().next());
                }
            }
        }
        return contextObjectIds;
    }

    private void validateRoleContextType(String domainObjectType, ContextRoleConfig contextRoleConfig) {
        String roleContextType = null;
        if (contextRoleConfig.getContext() != null && contextRoleConfig.getContext().getDomainObject() != null) {
            roleContextType = contextRoleConfig.getContext().getDomainObject().getType();
        }

        if (!domainObjectType.equals(roleContextType)) {
            throw new ConfigurationException("Context type for context role : " + contextRoleConfig.getName()
                    + " does not match the domain object type in access matrix configuration for: "
                    + domainObjectType);
        }
    }

    @Override
    public void cleanAclFor(Id objectId) {
        deleteAclRecords(objectId);
        deleteAclReadRecords(objectId);
    }

    private void deleteAclRecords(Id objectId) {
        deleteAclRecords(objectId, false);
    }

    private void deleteAclReadRecords(Id objectId) {
        deleteAclRecords(objectId, true);
    }

    private void deleteAclRecords(Id objectId, boolean isAclReadTable) {
        RdbmsId rdbmsObjectId = (RdbmsId) objectId;
        String query = generateDeleteAclQuery(rdbmsObjectId, isAclReadTable);
        Map<String, Object> parameters = initializeDeleteAclParameters(rdbmsObjectId);
        jdbcTemplate.update(query, parameters);

    }

    private String generateDeleteAclQuery(RdbmsId objectId, boolean isAclReadTable) {
        String tableName = null;
        String typeName = domainObjectTypeIdCache.getName(objectId.getTypeId());
        if (isAclReadTable) {
            tableName = AccessControlUtility.getAclReadTableName(typeName);
        } else {
            tableName = AccessControlUtility.getAclTableName(typeName);
        }

        StringBuilder query = new StringBuilder();
        query.append("delete from ");
        query.append(tableName).append(" o ");
        query.append("where o.object_id = :object_id");

        return query.toString();
    }

    private Map<String, Object> initializeDeleteAclParameters(RdbmsId objectId) {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("object_id", objectId.getId());

        return parameters;
    }

}
