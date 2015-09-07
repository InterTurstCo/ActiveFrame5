package ru.intertrust.cm.core.dao.impl.access;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.DomainObjectPermission;
import ru.intertrust.cm.core.business.api.dto.DomainObjectPermission.Permission;
import ru.intertrust.cm.core.business.api.dto.FieldModification;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.config.*;
import ru.intertrust.cm.core.config.base.Configuration;
import ru.intertrust.cm.core.config.base.TopLevelConfig;
import ru.intertrust.cm.core.dao.access.*;
import ru.intertrust.cm.core.dao.api.extension.ExtensionPoint;
import ru.intertrust.cm.core.dao.api.extension.OnLoadConfigurationExtensionHandler;
import ru.intertrust.cm.core.dao.exception.DaoException;
import ru.intertrust.cm.core.dao.impl.DataStructureNamingHelper;
import ru.intertrust.cm.core.dao.impl.utils.ConfigurationExplorerUtils;
import ru.intertrust.cm.core.dao.impl.utils.DaoUtils;
import ru.intertrust.cm.core.model.PermissionException;

import javax.annotation.Resource;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.Synchronization;
import javax.transaction.TransactionSynchronizationRegistry;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Реализация сервиса обновления списков доступа.
 * @author atsvetkov
 */
@ExtensionPoint
public class PermissionServiceDaoImpl extends BaseDynamicGroupServiceImpl implements PermissionServiceDao,
        ApplicationContextAware,
        OnLoadConfigurationExtensionHandler {
    @Resource
    private TransactionSynchronizationRegistry txReg;

    private ApplicationContext applicationContext;

    @Autowired
    protected NamedParameterJdbcOperations masterNamedParameterJdbcTemplate; // Use for data modifying operations

    @Autowired
    protected NamedParameterJdbcOperations switchableNamedParameterJdbcTemplate; // User for read operations

    public void setMasterNamedParameterJdbcTemplate(NamedParameterJdbcOperations masterNamedParameterJdbcTemplate) {
        this.masterNamedParameterJdbcTemplate = masterNamedParameterJdbcTemplate;
    }


    //Реестр коллекторов по отслеживаемому типу
    private Hashtable<String, List<ContextRoleRegisterItem>> collectors =
            new Hashtable<String, List<ContextRoleRegisterItem>>();

    //Реестр коллекторов по имени контекстной роли
    private Hashtable<String, List<ContextRoleRegisterItem>> collectorsByContextRoleNames =
            new Hashtable<String, List<ContextRoleRegisterItem>>();

    @Override
    public void notifyDomainObjectDeleted(DomainObject domainObject) {
        notifyDomainObjectChangedInternal(domainObject, getDeletedModificationList(domainObject), false);
        cleanAclFor(domainObject.getId());
    }

    @Override
    public void notifyDomainObjectChanged(DomainObject domainObject, List<FieldModification> modifiedFieldNames) {
        notifyDomainObjectChangedInternal(domainObject, modifiedFieldNames, false);
    }

    @Override
    public void notifyDomainObjectCreated(DomainObject domainObject) {
        notifyDomainObjectChangedInternal(domainObject, getNewObjectModificationList(domainObject), true);
    }

    private void
            notifyDomainObjectChangedInternal(DomainObject domainObject, List<FieldModification> modifiedFieldNames,
                    boolean invalidateCurrent) {
        String typeName = domainObject.getTypeName().toLowerCase();

        List<ContextRoleRegisterItem> typeCollectors = collectors.get(typeName);
        // Формируем мапу динамических групп, требующих пересчета и их
        // коллекторов, исключая дублирование
        Set<Id> invalidContexts = new HashSet<Id>();

        //Для нового объекта и если сменился статус всегда добавляем в не валидный контекст сам создаваемый или измененный объект, 
        //чтобы рассчитались права со статичными или без контекстными группами
        if (invalidateCurrent) {
            invalidContexts.add(domainObject.getId());
        }

        if (typeCollectors != null) {
            for (ContextRoleRegisterItem dynamicGroupCollector : typeCollectors) {
                // Поучаем невалидные контексты и добавляем их в итоговый массив без дублирования
                addAllWithoutDuplicate(invalidContexts,
                        dynamicGroupCollector.getCollector().getInvalidContexts(domainObject,
                                modifiedFieldNames));
            }
        }

        // Непосредственно формирование состава, должно вызываться в конце транзакции
        if (invalidContexts != null) {
            regRecalcInvalidAcl(invalidContexts);
        }
    }

    @Override
    public void refreshAclFor(Id invalidContextId) {
        RdbmsId rdbmsId = (RdbmsId) invalidContextId;
        String domainObjectType = domainObjectTypeIdCache.getName(rdbmsId.getTypeId());
        //Проверка, есть ли вообще матрицы доступа для данного типа доменного объекта. Необходима, что бы лишний раз не читать статус ДО.
        AccessMatrixConfig accessMatrixByTypeConfig =
                configurationExplorer.getAccessMatrixByObjectTypeUsingExtension(domainObjectType);

        if (accessMatrixByTypeConfig == null || accessMatrixByTypeConfig.getName() == null) {
            return;
        }

        String status = getStatusFor(invalidContextId);
        AccessMatrixStatusConfig accessMatrixConfig =
                configurationExplorer.getAccessMatrixByObjectTypeAndStatus(domainObjectType, status);

        if (accessMatrixConfig == null || accessMatrixConfig.getName() == null
                || accessMatrixConfig.getPermissions() == null) {
            //Для текущего статуса не найдено вхождения в матрице, отбираем у всех права
            accessMatrixConfig = new AccessMatrixStatusConfig();
            accessMatrixConfig.setName(status);
            accessMatrixConfig.setPermissions(new ArrayList<BaseOperationPermitConfig>());
        }

        //Переделываем формирование acl. Вместо полного удаления и создания формируем точечные изменения, приводящие acl в актуальное состояниеы
        /*cleanAclFor(invalidContextId);

        for (BaseOperationPermitConfig operationPermitConfig : accessMatrixConfig.getPermissions()) {
            AccessType accessType = getAccessType(operationPermitConfig);
            processOperationPermissions(invalidContextId, operationPermitConfig, accessType);
        }
        */

        //Получение необходимого состава acl
        Set<AclInfo> newAclInfos = new HashSet<>();
        for (BaseOperationPermitConfig operationPermitConfig : accessMatrixConfig.getPermissions()) {
            AccessType accessType = getAccessType(operationPermitConfig);
            //Добавляем без дублирования
            addAllWithoutDuplicate(newAclInfos,
                    processOperationPermissions(invalidContextId, operationPermitConfig, accessType));
        }

        //Получение текущего состава acl из базы
        List<AclInfo> oldAclInfos = getCurrentAclInfo(invalidContextId);

        //Получение разницы в составе acl
        //Получаем новые элементы в acl
        Set<AclInfo> addAclInfo = new HashSet<>();
        if (newAclInfos != null) {
            addAclInfo.addAll(newAclInfos);
        }

        //Получаем те элементы acl которые надо удалить
        Set<AclInfo> deleteAclInfo = new HashSet<AclInfo>();
        if (oldAclInfos != null) {
            deleteAclInfo.addAll(oldAclInfos);
        }

        //Непосредственно удаление или добавление в базу
        deleteAclRecords(invalidContextId, deleteAclInfo);

        insertAclRecords(invalidContextId, addAclInfo);
    }

    /**
     * Получение состава acl из базы
     * @param invalidContextId
     * @return
     */
    private List<AclInfo> getCurrentAclInfo(Id invalidContextId) {

        RdbmsId rdbmsObjectId = (RdbmsId) invalidContextId;

        String tableNameRead =
                AccessControlUtility.getAclReadTableName(configurationExplorer, domainObjectTypeIdCache.getName(rdbmsObjectId.getTypeId()));
        String tableNameAcl =
                AccessControlUtility.getAclTableName(domainObjectTypeIdCache.getName(rdbmsObjectId.getTypeId()));

        StringBuilder query = new StringBuilder();
        query.append("select 'R' as operation, r.").append(DaoUtils.wrap("group_id")).append(" from ").
                append(DaoUtils.wrap(tableNameRead)).append(" r ").append("where r.")
                .append(DaoUtils.wrap("object_id")).
                append(" = :object_id ");
        query.append("union ");
        query.append("select a.").append(DaoUtils.wrap("operation")).append(", a.").append(DaoUtils.wrap("group_id"))
                .append(" from ").
                append(DaoUtils.wrap(tableNameAcl)).append(" a where a.").append(DaoUtils.wrap("object_id"))
                .append(" = :object_id ");

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("object_id", rdbmsObjectId.getId());

        return switchableNamedParameterJdbcTemplate.query(query.toString(), parameters, new ResultSetExtractor<List<AclInfo>>() {

            @Override
            public List<AclInfo> extractData(ResultSet rs) throws SQLException, DataAccessException {
                List<AclInfo> result = new ArrayList<AclInfo>();
                while (rs.next()) {
                    AccessType accessType = null;
                    String operstion = rs.getString("operation");
                    if (operstion.equals("R")) {
                        accessType = DomainObjectAccessType.READ;
                    } else if (operstion.equals("W")) {
                        accessType = DomainObjectAccessType.WRITE;
                    } else if (operstion.equals("D")) {
                        accessType = DomainObjectAccessType.DELETE;
                    } else if (operstion.startsWith("E")) {
                        accessType = new ExecuteActionAccessType(operstion.substring(2));
                    } else if (operstion.startsWith("C")) {
                        accessType = new CreateChildAccessType(operstion.substring(2));
                    }

                    if (accessType != null) {
                        AclInfo info =
                                new AclInfo(accessType, new RdbmsId(domainObjectTypeIdCache.getId("User_Group"), rs
                                        .getLong("group_id")));
                        result.add(info);
                    }
                }
                return result;
            }
        });
    }

    /**
     * Определяет тип операции {@link AccessType} по конфигурации операции.
     * @param operationPermitConfig
     *            конфигурации операции
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
     * @param invalidContextId
     *            идентификатор доменного объекта, для которого расчитывается
     *            список доступа
     * @param operationPermitConfig
     *            конфигурация разрешений для операции
     * @param accessType
     *            тип операции
     */
    private List<AclInfo> processOperationPermissions(Id invalidContextId,
            BaseOperationPermitConfig operationPermitConfig,
            AccessType accessType) {
        RdbmsId rdbmsId = (RdbmsId) invalidContextId;
        String domainObjectType = domainObjectTypeIdCache.getName(rdbmsId.getTypeId());
        List<AclInfo> result = new ArrayList<AclInfo>();
        for (BasePermit permit : operationPermitConfig.getPermitConfigs()) {
            if (permit.getClass().equals(PermitRole.class)) {
                String contextRoleName = permit.getName();
                ContextRoleConfig contextRoleConfig =
                        configurationExplorer.getConfig(ContextRoleConfig.class, contextRoleName);
                if (contextRoleConfig == null) {
                    throw new ConfigurationException("Context role : " + contextRoleName
                            + " not found in configuration");

                }
                validateRoleContextType(domainObjectType, contextRoleConfig);

                List<ContextRoleRegisterItem> collectors =
                        collectorsByContextRoleNames.get(contextRoleConfig.getName());

                if (collectors != null) {
                    for (ContextRoleRegisterItem collectorItem : collectors) {
                        result.addAll(processAclForCollector(invalidContextId, collectorItem.getCollector(),
                                accessType));
                    }
                }
            } else if (permit.getClass().equals(PermitGroup.class)) {
                String dynamicGroupName = permit.getName();

                DynamicGroupConfig dynamicGroupConfig =
                        configurationExplorer.getConfig(DynamicGroupConfig.class, dynamicGroupName);

                if (dynamicGroupConfig != null && dynamicGroupConfig.getContext() != null
                        && dynamicGroupConfig.getContext().getDomainObject() != null) {

                    // контекстным объектом является текущий объект (для которого
                    // пересчитываются списки доступа)
                    AclInfo aclInfo = processAclForDynamicGroupWithContext(invalidContextId, accessType, dynamicGroupName, invalidContextId);
                    if (aclInfo != null) {
                        result.add(aclInfo);
                    }
                } else {
                    result.add(processAclForDynamicGroupWithoutContext(invalidContextId, accessType, dynamicGroupName));
                }
            }
        }
        return result;
    }

    private AclInfo processAclForDynamicGroupWithContext(Id objectId, AccessType accessType, String dynamicGroupName,
            Id contextObjectId) {
        Id dynamicGroupId = getUserGroupByGroupNameAndObjectId(dynamicGroupName, contextObjectId);
        if (dynamicGroupId != null) {
            return new AclInfo(accessType, dynamicGroupId);
        } else {
            return null;
        }
    }

    private List<AclInfo> processAclForCollector(Id invalidContextId,
            ContextRoleCollector collector, AccessType accessType) {
        List<AclInfo> result = new ArrayList<AclInfo>();
        for (Id groupId : collector.getMembers(invalidContextId)) {
            result.add(new AclInfo(accessType, groupId));
        }
        return result;
    }

    private AclInfo
            processAclForDynamicGroupWithoutContext(Id objectId, AccessType accessType, String dynamicGroupName) {
        Id dynamicGroupId = getUserGroupByGroupName(dynamicGroupName);
        if (dynamicGroupId == null) {
            dynamicGroupId = createUserGroup(dynamicGroupName, null);
        }
        //insertAclRecord(accessType, objectId, dynamicGroupId);
        return new AclInfo(accessType, dynamicGroupId);
    }

    private void insertAclRecords(Id objectId, Set<AclInfo> addAclInfo) {
        RdbmsId rdbmsObjectId = (RdbmsId) objectId;

        List<AclInfo> aclInfoRead = new ArrayList<>();
        List<AclInfo> aclInfoNoRead = new ArrayList<>();
        
        for (AclInfo aclInfo : addAclInfo) {
            if (aclInfo.getAccessType() == DomainObjectAccessType.READ) {
                aclInfoRead.add(aclInfo);
            } else {
                aclInfoNoRead.add(aclInfo);
            }
        }
        
        insertAclRecordsInBatch(aclInfoRead, new RdbmsId[]{rdbmsObjectId}, true);
        insertAclRecordsInBatch(aclInfoNoRead, new RdbmsId[]{rdbmsObjectId}, false);
        

    }

    /**
     * Добавлляет ACL записи в пакетном режиме. Идентификаторы объектов должны быть одного типа.
     * @param addAclInfo
     * @param rdbmsObjectIds
     * @param isReadAcl
     */
    private void insertAclRecordsInBatch(List<AclInfo> addAclInfo, RdbmsId[] rdbmsObjectIds, Boolean isReadAcl) {
        if (addAclInfo == null || addAclInfo.isEmpty() || rdbmsObjectIds == null || rdbmsObjectIds.length == 0) {
            return;
        }
        
        String query = null;
        RdbmsId etalonRdbmsId = rdbmsObjectIds[0];
        if (isReadAcl) {
            query = generateInsertAclReadRecordQuery(etalonRdbmsId);
        } else {
            query = generateInsertAclRecordQuery(etalonRdbmsId);

        }

        Map<String, Object>[] parameters = new Map[addAclInfo.size() * rdbmsObjectIds.length];

        int index = 0;
        for (RdbmsId rdbmsObjectId : rdbmsObjectIds) {            
            for (AclInfo aclInfo : addAclInfo) {
                RdbmsId rdbmsDynamicGroupId = (RdbmsId) aclInfo.getGroupId();
                parameters[index] = initializeInsertAclRecordParameters(aclInfo.getAccessType(), rdbmsObjectId, rdbmsDynamicGroupId);
                index++;
            }
        }
        masterNamedParameterJdbcTemplate.batchUpdate(query, parameters);
    }
    
    /**
     * Добавляет запись в _ACl (_READ) таблицу.
     * @param accessType
     *            тип доступа
     * @param objectId
     *            идентификатор доменного объекта
     * @param dynamicGroupId
     *            идентификатор группы пользователей
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
        masterNamedParameterJdbcTemplate.update(query, parameters);

    }

    private void deleteAclRecords(Id objectId, Set<AclInfo> addAclInfo) {
        RdbmsId rdbmsObjectId = (RdbmsId) objectId;
        List<AclInfo> aclInfoRead = new ArrayList<>();
        List<AclInfo> aclInfoNoRead = new ArrayList<>();
        
        for (AclInfo aclInfo : addAclInfo) {
            if (aclInfo.getAccessType() == DomainObjectAccessType.READ) {
                aclInfoRead.add(aclInfo);
            } else {
                aclInfoNoRead.add(aclInfo);
            }
        }
        
        deleteAclRecordsInBatch(aclInfoRead, rdbmsObjectId, true);
        deleteAclRecordsInBatch(aclInfoNoRead, rdbmsObjectId, false);

    }

    private void deleteAclRecordsInBatch(List<AclInfo> addAclInfo, RdbmsId rdbmsObjectId, boolean isReadAcl) {
        if (addAclInfo == null || addAclInfo.isEmpty()) {
            return;
        }
        String query = null;
        if (isReadAcl) {
            query = generateDeleteAclReadRecordQuery(rdbmsObjectId);
        } else {
            query = generateDeleteAclRecordQuery(rdbmsObjectId);

        }

        Map<String, Object>[] parameters = new Map[addAclInfo.size()];

        int index = 0;
        for (AclInfo aclInfo : addAclInfo) {
            RdbmsId rdbmsDynamicGroupId = (RdbmsId) aclInfo.getGroupId();
            parameters[index] = initializeDeleteAclRecordParameters(aclInfo.getAccessType(), rdbmsObjectId, rdbmsDynamicGroupId);
            ;
            index++;
        }
        masterNamedParameterJdbcTemplate.batchUpdate(query, parameters);
    }

    private String generateDeleteAclReadRecordQuery(RdbmsId objectId) {
        String tableName = null;
        tableName = AccessControlUtility.getAclReadTableName(configurationExplorer,
                domainObjectTypeIdCache.getName(objectId.getTypeId()));

        StringBuilder query = new StringBuilder();
        query.append("delete from ");
        query.append(DaoUtils.wrap(tableName)).append(" where ").append(DaoUtils.wrap("object_id"))
                .append(" = :object_id ");
        query.append("and ").append(DaoUtils.wrap("group_id")).append(" = :group_id");

        return query.toString();
    }

    private String generateInsertAclReadRecordQuery(RdbmsId objectId) {
        String tableName = null;

        tableName = AccessControlUtility.getAclReadTableName(configurationExplorer,
                domainObjectTypeIdCache.getName(objectId.getTypeId()));

        StringBuilder query = new StringBuilder();
        query.append("insert  into ");
        query.append(DaoUtils.wrap(tableName)).append(" (").append(DaoUtils.wrap("object_id")).append(", ")
                .append(DaoUtils.wrap("group_id")).
                append(") values (:object_id, :group_id)");

        return query.toString();
    }

    private String generateInsertAclRecordQuery(RdbmsId objectId) {
        String tableName = AccessControlUtility.getAclTableName(domainObjectTypeIdCache.getName(objectId.getTypeId()));

        StringBuilder query = new StringBuilder();
        query.append("insert  into ");
        query.append(DaoUtils.wrap(tableName)).append(" (").append(DaoUtils.wrap("operation")).append(", ").
                append(DaoUtils.wrap("object_id")).append(", ").append(DaoUtils.wrap("group_id")).append(")").
                append(" values (:operation, :object_id, :group_id)");

        return query.toString();
    }

    private String getTopLevelParentType(RdbmsId objectId) {
        String objectType = domainObjectTypeIdCache.getName(objectId.getTypeId());
        String topLevelParentType = ConfigurationExplorerUtils.getTopLevelParentType(configurationExplorer, objectType);
        return topLevelParentType;
    }

    private String generateDeleteAclRecordQuery(RdbmsId objectId) {
        String tableName = null;
        tableName = AccessControlUtility.getAclTableName(domainObjectTypeIdCache.getName(objectId.getTypeId()));

        StringBuilder query = new StringBuilder();
        query.append("delete from ");
        query.append(DaoUtils.wrap(tableName)).append(" where ").append(DaoUtils.wrap("operation"))
                .append("=:operation and ").
                append(DaoUtils.wrap("object_id")).append("=:object_id and ").append(DaoUtils.wrap("group_id"))
                .append("=:group_id");

        return query.toString();
    }

    private Map<String, Object> initializeDeleteAclRecordParameters(AccessType accessType, RdbmsId rdbmsObjectId,
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

    private void validateRoleContextType(String domainObjectType, ContextRoleConfig contextRoleConfig) {
        //Переделать проверку чтобы учитывалось наследование типов
        String roleContextType = null;
        if (contextRoleConfig.getContext() != null && contextRoleConfig.getContext().getDomainObject() != null) {
            roleContextType = contextRoleConfig.getContext().getDomainObject().getType().toLowerCase();
        }

        //Получение всех вышестоящих типов
        List<String> parentTypeList = new ArrayList<String>();
        String parentType = domainObjectType;
        while (parentType != null) {
            parentTypeList.add(parentType.toLowerCase());
            parentType = configurationExplorer.getDomainObjectParentType(parentType);
        }

        //Проверка наличие типа в списке родительских типов
        if (!parentTypeList.contains(roleContextType)) {
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
        masterNamedParameterJdbcTemplate.update(query, parameters);

    }

    private String generateDeleteAclQuery(RdbmsId objectId, boolean isAclReadTable) {
        String tableName = null;
        String typeName = domainObjectTypeIdCache.getName(objectId.getTypeId());
        if (isAclReadTable) {
            tableName = AccessControlUtility.getAclReadTableName(configurationExplorer, typeName);
        } else {
            tableName = AccessControlUtility.getAclTableName(typeName);
        }

        StringBuilder query = new StringBuilder();
        query.append("delete from ");
        query.append(DaoUtils.wrap(tableName)).append(" o ");
        query.append("where o.").append(DaoUtils.wrap("object_id")).append(" = :object_id");

        return query.toString();
    }

    private Map<String, Object> initializeDeleteAclParameters(RdbmsId objectId) {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("object_id", objectId.getId());

        return parameters;
    }

    @Override
    public void onLoad() {
        try {
            // Поиск конфигураций динамических групп
            Configuration configuration = configurationExplorer.getConfiguration();
            List<TopLevelConfig> configurationList = configuration.getConfigurationList();
            for (TopLevelConfig topConfig : configurationList) {

                if (topConfig instanceof ContextRoleConfig) {
                    ContextRoleConfig contextRoleConfig = (ContextRoleConfig) topConfig;
                    // Если контекстная роль настраивается классом коллектором,
                    // то создаем его экземпляр, и добавляем в реестр

                    if (contextRoleConfig.getGroups() != null && contextRoleConfig.getGroups().getGroups() != null) {

                        for (Object collectorConfig : contextRoleConfig.getGroups().getGroups()) {
                            if (collectorConfig instanceof CollectorConfig) {
                                CollectorConfig classCollectorConfig = (CollectorConfig) collectorConfig;
                                Class<?> collectorClass = Class.forName(classCollectorConfig.getClassName());
                                ContextRoleCollector collector = (ContextRoleCollector) applicationContext
                                        .getAutowireCapableBeanFactory()
                                        .createBean(
                                                collectorClass,
                                                AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE,
                                                false);
                                collector.init(contextRoleConfig, classCollectorConfig.getSettings());
                                registerCollector(collector, contextRoleConfig, configuration);
                            } else if (collectorConfig instanceof TrackDomainObjectsConfig) {
                                // Специфичный коллектор не указан используем коллектор
                                // по умолчанию
                                TrackDomainObjectsConfig trackDomainObjectsConfig =
                                        (TrackDomainObjectsConfig) collectorConfig;
                                ContextRoleCollector collector = (ContextRoleCollector) applicationContext
                                        .getAutowireCapableBeanFactory()
                                        .createBean(
                                                ContextRoleTrackDomainObjectCollector.class,
                                                AutowireCapableBeanFactory.AUTOWIRE_BY_NAME,
                                                false);
                                collector.init(contextRoleConfig, trackDomainObjectsConfig);
                                registerCollector(collector, contextRoleConfig, configuration);
                            } else if (collectorConfig instanceof StaticGroupCollectorConfig) {
                                StaticGroupCollectorConfig staticGroupCollectorConfig =
                                        (StaticGroupCollectorConfig) collectorConfig;
                                ContextRoleCollector collector = (ContextRoleCollector) applicationContext
                                        .getAutowireCapableBeanFactory()
                                        .createBean(
                                                ContextRoleStaticGroupCollector.class,
                                                AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE,
                                                false);
                                collector.init(contextRoleConfig, staticGroupCollectorConfig);
                                registerCollector(collector, contextRoleConfig, configuration);
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            throw new PermissionException("Error on init collector classes", ex);
        }
    }

    private void registerCollector(ContextRoleCollector collector, ContextRoleConfig contextRoleConfig,
            Configuration configuration) {
        // Получение типов, которые отслеживает коллектор
        List<String> types = collector.getTrackTypeNames();
        // Регистрируем коллектор в реестре, для обработки
        // только определенных типов
        if (types != null) {
            for (String type : types) {
                registerCollector(type, collector, contextRoleConfig);
                // Ищем всех наследников и так же регистрируем
                // их в
                // реестре с данным коллектором
                List<String> subTypes = getSubTypes(type, configuration);
                for (String subtype : subTypes) {
                    registerCollector(subtype, collector, contextRoleConfig);
                }
            }
        }

        registerCollectorForContextRole(contextRoleConfig.getName(), collector, contextRoleConfig);
    }

    /**
     * Регистрация коллектора в реестре коллекторов
     * 
     * @param type
     * @param collector
     */
    private void registerCollector(String type, ContextRoleCollector collector, ContextRoleConfig config) {
        List<ContextRoleRegisterItem> typeCollectors = collectors.get(type.toLowerCase());
        if (typeCollectors == null) {
            typeCollectors = new ArrayList<ContextRoleRegisterItem>();
            collectors.put(type.toLowerCase(), typeCollectors);
        }
        typeCollectors.add(new ContextRoleRegisterItem(collector));
    }

    /**
     * Получение всех дочерних типов переданного типа
     * 
     * @param type
     * @return
     */
    private List<String> getSubTypes(String type, Configuration configuration) {
        List<String> result = new ArrayList<String>();
        // Получение всех конфигураций доменных оьъектов
        for (TopLevelConfig topConfig : configuration.getConfigurationList()) {
            if (topConfig instanceof DomainObjectTypeConfig) {
                DomainObjectTypeConfig config = (DomainObjectTypeConfig) topConfig;
                // Сравнение родительского типа и переданного парамера
                if (config.getExtendsAttribute() != null
                        && config.getExtendsAttribute().equals(type)) {
                    // Если нашли наследника добавляем в результат
                    result.add(config.getName());
                    // Рекурсивно вызываем для получения всех наследников
                    // найденного наследника
                    result.addAll(getSubTypes(config.getName(), configuration));
                }
            }
        }
        return result;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    private void registerCollectorForContextRole(String roleName, ContextRoleCollector collector,
            ContextRoleConfig config) {
        List<ContextRoleRegisterItem> groupCollectors = collectorsByContextRoleNames.get(roleName);
        if (groupCollectors == null) {
            groupCollectors = new ArrayList<ContextRoleRegisterItem>();
            collectorsByContextRoleNames.put(roleName, groupCollectors);
        }
        groupCollectors.add(new ContextRoleRegisterItem(collector));
    }

    /**
     * Клаксс для описания элемента реестра контекстных ролей
     * @author larin
     * 
     */
    private class ContextRoleRegisterItem {
        private ContextRoleCollector collector;

        private ContextRoleRegisterItem(ContextRoleCollector collector) {
            this.collector = collector;
        }

        public ContextRoleCollector getCollector() {
            return collector;
        }
    }

    @Override
    public DomainObjectPermission getObjectPermission(Id domainObjectId, Id userId) {
        List<DomainObjectPermission> result = getObjectPermissions(domainObjectId, userId);
        if (result.size() > 0) {
            return result.get(0);
        }
        return null;
    }

    @Override
    public List<DomainObjectPermission> getObjectPermissions(Id domainObjectId) {
        return getObjectPermissions(domainObjectId, null);
    }

    private List<DomainObjectPermission> getObjectPermissions(Id domainObjectId, Id personId) {
        RdbmsId rdbmsObjectId = (RdbmsId) domainObjectId;

        String objectType = domainObjectTypeIdCache.getName(rdbmsObjectId.getTypeId());
        String permissionType = objectType;
        String matrixRefType = configurationExplorer.getMatrixReferenceTypeName(permissionType);
        if (matrixRefType != null) {
            permissionType = getMatrixRefType(objectType, matrixRefType, rdbmsObjectId);
        }
        
        final AccessMatrixConfig accessMatrix = configurationExplorer.getAccessMatrixByObjectTypeUsingExtension(objectType);

        String domainObjectBaseTable =
                DataStructureNamingHelper.getSqlName(ConfigurationExplorerUtils.getTopLevelParentType(configurationExplorer, objectType));
        String tableNameRead =
                AccessControlUtility.getAclReadTableName(configurationExplorer, permissionType);
        String tableNameAcl =
                AccessControlUtility.getAclTableName(permissionType);

        StringBuilder query = new StringBuilder();
        query.append("select 'R' as operation, gm.").append(DaoUtils.wrap("person_id")).append(", gm.").
                append(DaoUtils.wrap("person_id_type")).append(" from ").append(DaoUtils.wrap(tableNameRead))
                .append(" r ").
                append("inner join ").append(DaoUtils.wrap("group_group")).append(" gg on (r.")
                .append(DaoUtils.wrap("group_id")).
                append(" = gg.").append(DaoUtils.wrap("child_group_id")).append(") inner join ")
                .append(DaoUtils.wrap("group_member")).
                append(" gm on gg.").append(DaoUtils.wrap("parent_group_id")).append(" = gm.")
                .append(DaoUtils.wrap("usergroup")).
                //обавляем в связи с появлением функциональности замещения прав
                append("inner join ").append(DaoUtils.wrap(domainObjectBaseTable)).append(" o on (o.")
                .append(DaoUtils.wrap("access_object_id")).
                append(" = r.").append(DaoUtils.wrap("object_id")).
                append(") where o.").append(DaoUtils.wrap("id")).append(" = :object_id ");
        if (personId != null) {
            query.append("and gm.").append(DaoUtils.wrap("person_id")).append(" = :person_id ");
        }
        query.append("union ");
        query.append("select a.").append(DaoUtils.wrap("operation")).append(", gm.").append(DaoUtils.wrap("person_id"))
                .append(", ").
                append("gm.").append(DaoUtils.wrap("person_id_type")).append(" from ")
                .append(DaoUtils.wrap(tableNameAcl)).append(" a ").
                append("inner join ").append(DaoUtils.wrap("group_group")).append(" gg on (a.")
                .append(DaoUtils.wrap("group_id")).
                append(" = gg.").append(DaoUtils.wrap("child_group_id")).append(") inner join ")
                .append(DaoUtils.wrap("group_member")).
                append(" gm on gg.").append(DaoUtils.wrap("parent_group_id")).append(" = gm.")
                .append(DaoUtils.wrap("usergroup")).
                //обавляем в связи с появлением функциональности замещения прав
                append("inner join ").append(DaoUtils.wrap(domainObjectBaseTable)).append(" o on (o.")
                .append(DaoUtils.wrap("access_object_id")).
                append(" = a.").append(DaoUtils.wrap("object_id")).
                append(") where o.").append(DaoUtils.wrap("id")).append(" = :object_id ");
        if (personId != null) {
            query.append("and gm.").append(DaoUtils.wrap("person_id")).append(" = :person_id");
        }

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("object_id", rdbmsObjectId.getId());
        if (personId != null) {
            parameters.put("person_id", ((RdbmsId) personId).getId());
        }

        return switchableNamedParameterJdbcTemplate.query(query.toString(), parameters, new ResultSetExtractor<List<DomainObjectPermission>>() {

            @Override
            public List<DomainObjectPermission> extractData(ResultSet rs) throws SQLException, DataAccessException {
                Map<Id, DomainObjectPermission> personPermissions = new HashMap<Id, DomainObjectPermission>();
                while (rs.next()) {
                    long personIdLong = rs.getLong("person_id");
                    int personType = rs.getInt("person_id_type");
                    Id personId = new RdbmsId(personType, personIdLong);
                    String operation = rs.getString("operation");

                    DomainObjectPermission personPermission = personPermissions.get(personId);
                    if (personPermission == null) {
                        personPermission = new DomainObjectPermission();
                        personPermission.setPersonId(personId);
                        personPermissions.put(personId, personPermission);
                    }

                    if (operation.equals("R")) {
                        if (accessMatrix.getMatrixReference() != null){
                            setMappedPermission(personPermission, Permission.Read);
                        }else{
                            personPermission.getPermission().add(Permission.Read);
                        }
                    } else if (operation.equals("W")) {
                        if (accessMatrix.getMatrixReference() != null){
                            setMappedPermission(personPermission, Permission.Write);
                        }else{
                            personPermission.getPermission().add(Permission.Write);
                        }
                    } else if (operation.equals("D")) {
                        if (accessMatrix.getMatrixReference() != null){
                            setMappedPermission(personPermission, Permission.Delete);
                        }else{
                            personPermission.getPermission().add(Permission.Delete);
                        }
                    } else if (operation.startsWith("E_")) {
                        String action = operation.substring(2);
                        if (accessMatrix.getMatrixReference() != null){
                            setMappedActions(personPermission, action);
                        }else{
                            personPermission.getActions().add(action);
                        }
                    } else if (operation.startsWith("C_")) {
                        String childType = operation.substring(2);
                        if (accessMatrix.getMatrixReference() != null){
                            setMappedCreateTypes(personPermission, childType);
                        }else{
                            personPermission.getCreateChildTypes().add(childType);
                        }
                    }
                }
                List<DomainObjectPermission> result = new ArrayList<DomainObjectPermission>();

                result.addAll(personPermissions.values());
                return result;
            }

            private void setMappedActions(DomainObjectPermission personPermission, String action) {
                if (accessMatrix.getMatrixReferenceMappingConfig() != null){
                    if (accessMatrix.getMatrixReferenceMappingConfig().getPermission() != null){
                        for (MatrixReferenceMappingPermissionConfig matrixMapping : accessMatrix.getMatrixReferenceMappingConfig().getPermission()) {
                            if (matrixMapping.getMapFrom().equals(MatrixReferenceMappingPermissionConfig.EXECUTE + ":" + action)){
                                personPermission.getPermission().addAll(getPermissionFromMatrixRef(matrixMapping.getMapTo()));
                                personPermission.getActions().addAll(getActionsFromMatrixRef(matrixMapping.getMapTo()));
                                personPermission.getCreateChildTypes().addAll(getCreateChildFromMatrixRef(matrixMapping.getMapTo()));
                            }
                        }
                    }
                }
            }

            private void setMappedCreateTypes(DomainObjectPermission personPermission, String childType) {
                if (accessMatrix.getMatrixReferenceMappingConfig() != null){
                    if (accessMatrix.getMatrixReferenceMappingConfig().getPermission() != null){
                        for (MatrixReferenceMappingPermissionConfig matrixMapping : accessMatrix.getMatrixReferenceMappingConfig().getPermission()) {
                            if (matrixMapping.getMapFrom().equals(MatrixReferenceMappingPermissionConfig.CREATE_CHILD + ":" + childType)){
                                personPermission.getPermission().addAll(getPermissionFromMatrixRef(matrixMapping.getMapTo()));
                                personPermission.getActions().addAll(getActionsFromMatrixRef(matrixMapping.getMapTo()));
                                personPermission.getCreateChildTypes().addAll(getCreateChildFromMatrixRef(matrixMapping.getMapTo()));
                            }
                        }
                    }
                }
            }
            
            private void setMappedPermission(DomainObjectPermission personPermission, Permission permission) {
                if (accessMatrix.getMatrixReferenceMappingConfig() != null){
                    if (accessMatrix.getMatrixReferenceMappingConfig().getPermission() != null){
                        for (MatrixReferenceMappingPermissionConfig matrixMapping : accessMatrix.getMatrixReferenceMappingConfig().getPermission()) {
                            if (permission.equals(Permission.Read) && matrixMapping.getMapFrom().equals(MatrixReferenceMappingPermissionConfig.READ)){
                                personPermission.getPermission().addAll(getPermissionFromMatrixRef(matrixMapping.getMapTo()));
                                personPermission.getActions().addAll(getActionsFromMatrixRef(matrixMapping.getMapTo()));
                                personPermission.getCreateChildTypes().addAll(getCreateChildFromMatrixRef(matrixMapping.getMapTo()));
                            }else if (permission.equals(Permission.Write) && matrixMapping.getMapFrom().equals(MatrixReferenceMappingPermissionConfig.WRITE)){
                                personPermission.getPermission().addAll(getPermissionFromMatrixRef(matrixMapping.getMapTo()));
                                personPermission.getActions().addAll(getActionsFromMatrixRef(matrixMapping.getMapTo()));
                                personPermission.getCreateChildTypes().addAll(getCreateChildFromMatrixRef(matrixMapping.getMapTo()));
                            }else if (permission.equals(Permission.Delete) && matrixMapping.getMapFrom().equals(MatrixReferenceMappingPermissionConfig.DELETE)){
                                personPermission.getPermission().addAll(getPermissionFromMatrixRef(matrixMapping.getMapTo()));
                                personPermission.getActions().addAll(getActionsFromMatrixRef(matrixMapping.getMapTo()));
                                personPermission.getCreateChildTypes().addAll(getCreateChildFromMatrixRef(matrixMapping.getMapTo()));
                            }
                        }
                    }
                }else{
                    //Используем дефалтовый мапинг
                    personPermission.getPermission().add(permission);
                    if (permission.equals(Permission.Write)){
                        personPermission.getPermission().add(Permission.Delete);
                    }
                }
            }

            private List<Permission> getPermissionFromMatrixRef(String mapTo) {
                List<Permission> result = new ArrayList<Permission>();
                if (mapTo.equals(MatrixReferenceMappingPermissionConfig.READ)){
                    result.add(Permission.Read);
                }else if (mapTo.equals(MatrixReferenceMappingPermissionConfig.WRITE)){
                    result.add(Permission.Write);
                }else if (mapTo.equals(MatrixReferenceMappingPermissionConfig.DELETE)){
                    result.add(Permission.Delete);
                }
                return result;
            }

            private List<String> getActionsFromMatrixRef(String mapTo) {
                List<String> result = new ArrayList<String>();
                if (mapTo.startsWith(MatrixReferenceMappingPermissionConfig.EXECUTE)){
                    result.add(mapTo.split(":")[1]);
                }
                return result;
            }

            private List<String> getCreateChildFromMatrixRef(String mapTo) {
                List<String> result = new ArrayList<String>();
                if (mapTo.startsWith(MatrixReferenceMappingPermissionConfig.CREATE_CHILD)){
                    result.add(mapTo.split(":")[1]);
                }
                return result;
            }
        });
    }

    /**
     * Получение имени типа у которого заимствуются права. При этом учитывается
     * то что в матрице при заимствование может быть указан атрибут ссылающийся
     * на родительский тип того объекта, у которого реально надо взять матрицу
     * прав
     * @param childType
     * @param parentType
     * @param id
     * @return
     */
    private String getMatrixRefType(String childType, String parentType, RdbmsId id) {
        String rootForChildType = ConfigurationExplorerUtils.getTopLevelParentType(configurationExplorer, childType);
        String rootForParentType = ConfigurationExplorerUtils.getTopLevelParentType(configurationExplorer, parentType);
        String query =
                "select p.id_type from " + rootForChildType + " c inner join " + rootForParentType + " p on (c.access_object_id = p.id) where c.id = :id";

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("id", id.getId());

        int typeId = switchableNamedParameterJdbcTemplate.query(query, parameters, new ResultSetExtractor<Integer>() {

            @Override
            public Integer extractData(ResultSet rs) throws SQLException,
                    DataAccessException {
                rs.next();
                return rs.getInt("id_type");
            }
        });

        return domainObjectTypeIdCache.getName(typeId);
    }

    private void regRecalcInvalidAcl(Set<Id> invalidContext) {
        //не обрабатываем вне транзакции
        if (getTxReg().getTransactionKey() == null) {
            return;
        }
        RecalcAclSynchronization recalcGroupSynchronization =
                (RecalcAclSynchronization) getTxReg().getResource(RecalcAclSynchronization.class);
        if (recalcGroupSynchronization == null) {
            recalcGroupSynchronization = new RecalcAclSynchronization();
            getTxReg().putResource(RecalcAclSynchronization.class, recalcGroupSynchronization);
            getTxReg().registerInterposedSynchronization(recalcGroupSynchronization);
        }
        recalcGroupSynchronization.addContext(invalidContext);
    }

    private TransactionSynchronizationRegistry getTxReg() {
        if (txReg == null) {
            try {
                txReg =
                        (TransactionSynchronizationRegistry) new InitialContext()
                                .lookup("java:comp/TransactionSynchronizationRegistry");
            } catch (NamingException e) {
                throw new DaoException(e);
            }
        }
        return txReg;
    }

    private class RecalcAclSynchronization implements Synchronization {
        private Set<Id> contextIds = new HashSet<>();

        public RecalcAclSynchronization() {
        }

        public void addContext(Set<Id> invalidContexts) {
            addAllWithoutDuplicate(contextIds, invalidContexts);
        }

        @Override
        public void beforeCompletion() {
            for (Id contextId : contextIds) {
                refreshAclFor(contextId);
            }
        }

        @Override
        public void afterCompletion(int status) {
        }
        
        public Set<Id> getInvalidContexts(){
            return contextIds;
        }
    }

    private class AclInfo {
        private AccessType accessType;
        private Id groupId;

        public AclInfo(AccessType accessType, Id groupId) {
            this.accessType = accessType;
            this.groupId = groupId;
        }

        public AccessType getAccessType() {
            return accessType;
        }

        public Id getGroupId() {
            return groupId;
        }

        private PermissionServiceDaoImpl getOuterType() {
            return PermissionServiceDaoImpl.this;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getOuterType().hashCode();
            result = prime * result + ((accessType == null) ? 0 : accessType.hashCode());
            result = prime * result + ((groupId == null) ? 0 : groupId.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            AclInfo other = (AclInfo) obj;
            if (!getOuterType().equals(other.getOuterType()))
                return false;
            if (accessType == null) {
                if (other.accessType != null)
                    return false;
            } else if (!accessType.equals(other.accessType))
                return false;
            if (groupId == null) {
                if (other.groupId != null)
                    return false;
            } else if (!groupId.equals(other.groupId))
                return false;
            return true;
        }

    }

    @Override
    public List<Id> getPersons(Id contextId, String roleName) {

        List<ContextRoleRegisterItem> collectors = collectorsByContextRoleNames.get(roleName);

        List<Id> result = new ArrayList<Id>();
        if (collectors != null) {
            for (ContextRoleRegisterItem collectorItem : collectors) {
                List<Id> groups = collectorItem.getCollector().getMembers(contextId);
                for (Id groupId : groups) {
                    List<DomainObject> persons = personManagementService.getAllPersonsInGroup(groupId);
                    addUniquePerson(result, persons);
                }
            }
        }

        return result;
    }

    @Override
    public List<Id> getGroups(Id contextId, String roleName) {

        //Получаем все коллекторы
        List<ContextRoleRegisterItem> collectors = collectorsByContextRoleNames.get(roleName);

        List<Id> result = new ArrayList<Id>();
        //Цикл по коллекторам
        if (collectors != null) {
            for (ContextRoleRegisterItem collectorItem : collectors) {
                //Получаем состав контекстной роли
                List<Id> groups = collectorItem.getCollector().getMembers(contextId);
                for (Id group : groups) {
                    //Формируем результат с уникальными значениями
                    if (!result.contains(group)) {
                        result.add(group);
                    }
                }
            }
        }

        return result;
    }    
    
    /**
     * Добавление уникальных записей в результат
     * @param result
     * @param persons
     */
    private void addUniquePerson(List<Id> result, List<DomainObject> persons) {
        for (DomainObject domainObject : persons) {
            if (!result.contains(domainObject.getId())) {
                result.add(domainObject.getId());
            }
        }
    }

    @Override
    public void grantNewObjectPermissions(List<Id> domainObjectIds) {
        Id currentPersonId = currentUserAccessor.getCurrentUserId();

        if (currentPersonId != null && !userGroupGlobalCache.isPersonSuperUser(currentPersonId)) {
            // Получение динамической группы текущего пользователя.
            Id currentPersonGroup = getUserGroupByGroupNameAndObjectId("Person", currentPersonId);

            List<AclInfo> aclInfoRead = new ArrayList<>();
            List<AclInfo> aclInfoNoRead = new ArrayList<>();

            aclInfoRead.add(new AclInfo(DomainObjectAccessType.READ, currentPersonGroup));
            aclInfoNoRead.add(new AclInfo(DomainObjectAccessType.WRITE, currentPersonGroup));
            aclInfoNoRead.add(new AclInfo(DomainObjectAccessType.DELETE, currentPersonGroup));

            RdbmsId[] idsArray = domainObjectIds.toArray(new RdbmsId[domainObjectIds.size()]);

            insertAclRecordsInBatch(aclInfoRead, idsArray, true);
            insertAclRecordsInBatch(aclInfoNoRead, idsArray, false);

        }
    }

    /**
     * Предоставление прав на новые доменные обьекты
     */
    @Override
    public void grantNewObjectPermissions(Id domainObject) {
        //Получение текущего пользователя
        Id currentPersonId = currentUserAccessor.getCurrentUserId();

        //Проверка наличия контекста пользователя и проверка что пользователь не суперпользователь
        if (currentPersonId != null && !userGroupGlobalCache.isPersonSuperUser(currentPersonId)) {
            //Получение динамической группы текущего пользователя.
            Id currentPersonGroup = getUserGroupByGroupNameAndObjectId("Person", currentPersonId);

            //Добавляем права группе
            insertAclRecord(DomainObjectAccessType.READ, domainObject, currentPersonGroup);
            insertAclRecord(DomainObjectAccessType.WRITE, domainObject, currentPersonGroup);
            insertAclRecord(DomainObjectAccessType.DELETE, domainObject, currentPersonGroup);
        }
    }

    @Override
    public void refreshAcls() {
        //Не работаем вне транзакции
        if (getTxReg().getTransactionKey() == null) {
            return;
        }
        //Получаем все невалидные контексты и вызываем для них перерасчет ACL
        RecalcAclSynchronization recalcGroupSynchronization =
                (RecalcAclSynchronization) getTxReg().getResource(RecalcAclSynchronization.class);
        if (recalcGroupSynchronization != null) {
            for (Id contextId : recalcGroupSynchronization.getInvalidContexts()) {
                refreshAclFor(contextId);
            }
        }
    }

    @Override
    public void notifyDomainObjectChangeStatus(DomainObject domainObject) {
        notifyDomainObjectChangedInternal(domainObject, null, true);        
    }

}
