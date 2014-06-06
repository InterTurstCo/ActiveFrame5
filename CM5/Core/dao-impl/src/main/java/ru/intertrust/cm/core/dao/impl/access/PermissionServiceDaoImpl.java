package ru.intertrust.cm.core.dao.impl.access;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.Synchronization;
import javax.transaction.TransactionSynchronizationRegistry;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.DomainObjectPermission;
import ru.intertrust.cm.core.business.api.dto.DomainObjectPermission.Permission;
import ru.intertrust.cm.core.business.api.dto.FieldModification;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.config.AccessMatrixStatusConfig;
import ru.intertrust.cm.core.config.BaseOperationPermitConfig;
import ru.intertrust.cm.core.config.BasePermit;
import ru.intertrust.cm.core.config.CollectorConfig;
import ru.intertrust.cm.core.config.ConfigurationException;
import ru.intertrust.cm.core.config.ContextRoleConfig;
import ru.intertrust.cm.core.config.CreateChildConfig;
import ru.intertrust.cm.core.config.DeleteConfig;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.core.config.DynamicGroupConfig;
import ru.intertrust.cm.core.config.ExecuteActionConfig;
import ru.intertrust.cm.core.config.PermitGroup;
import ru.intertrust.cm.core.config.PermitRole;
import ru.intertrust.cm.core.config.ReadConfig;
import ru.intertrust.cm.core.config.StaticGroupCollectorConfig;
import ru.intertrust.cm.core.config.TrackDomainObjectsConfig;
import ru.intertrust.cm.core.config.WriteConfig;
import ru.intertrust.cm.core.config.base.Configuration;
import ru.intertrust.cm.core.config.base.TopLevelConfig;
import ru.intertrust.cm.core.dao.access.AccessType;
import ru.intertrust.cm.core.dao.access.ContextRoleCollector;
import ru.intertrust.cm.core.dao.access.CreateChildAccessType;
import ru.intertrust.cm.core.dao.access.DomainObjectAccessType;
import ru.intertrust.cm.core.dao.access.ExecuteActionAccessType;
import ru.intertrust.cm.core.dao.access.PermissionServiceDao;
import ru.intertrust.cm.core.dao.api.extension.ExtensionPoint;
import ru.intertrust.cm.core.dao.api.extension.OnLoadConfigurationExtensionHandler;
import ru.intertrust.cm.core.dao.exception.DaoException;
import ru.intertrust.cm.core.dao.impl.DataStructureNamingHelper;
import ru.intertrust.cm.core.dao.impl.utils.ConfigurationExplorerUtils;
import ru.intertrust.cm.core.dao.impl.utils.DaoUtils;
import ru.intertrust.cm.core.model.PermissionException;

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
                    boolean isCreate) {
        String typeName = domainObject.getTypeName().toLowerCase();

        List<ContextRoleRegisterItem> typeCollectors = collectors.get(typeName);
        // Формируем мапу динамических групп, требующих пересчета и их
        // коллекторов, исключая дублирование
        List<Id> invalidContexts = new ArrayList<Id>();

        //Для нового объекта всегда добавляем в не валидный контекст сам создаваемый объект, 
        //чтобы рассчитались права со статичными или без контекстными группами
        if (isCreate) {
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

    private void refreshAclFor(Id invalidContextId) {
        RdbmsId rdbmsId = (RdbmsId) invalidContextId;
        String domainObjectType = domainObjectTypeIdCache.getName(rdbmsId.getTypeId());
        String status = getStatusFor(invalidContextId);
        AccessMatrixStatusConfig accessMatrixConfig =
                configurationExplorer.getAccessMatrixByObjectTypeAndStatus(domainObjectType, status);

        if (accessMatrixConfig == null || accessMatrixConfig.getName() == null
                || accessMatrixConfig.getPermissions() == null) {
            return;
        }

        //Переделываем формирование acl. Вместо полного удаления и создания формируем точечные изменения, приводящие acl в актуальное состояниеы
        /*cleanAclFor(invalidContextId);

        for (BaseOperationPermitConfig operationPermitConfig : accessMatrixConfig.getPermissions()) {
            AccessType accessType = getAccessType(operationPermitConfig);
            processOperationPermissions(invalidContextId, operationPermitConfig, accessType);
        }
        */

        //Получение необходимого состава acl
        List<AclInfo> newAclInfos = new ArrayList<AclInfo>();
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
        List<AclInfo> addAclInfo = new ArrayList<AclInfo>();
        for (AclInfo aclInfo : newAclInfos) {
            if (!oldAclInfos.contains(aclInfo)) {
                addAclInfo.add(aclInfo);
            }
        }

        //Получаем те элементы acl которые надо удалить
        List<AclInfo> deleteAclInfo = new ArrayList<AclInfo>();
        for (AclInfo aclInfo : oldAclInfos) {
            if (!newAclInfos.contains(aclInfo)) {
                deleteAclInfo.add(aclInfo);
            }
        }

        //Непосредственно удаление или добавление в базу
        for (AclInfo aclInfo : deleteAclInfo) {
            deleteAclRecord(aclInfo.getAccessType(), invalidContextId, aclInfo.getGroupId());
        }
        for (AclInfo aclInfo : addAclInfo) {
            insertAclRecord(aclInfo.getAccessType(), invalidContextId, aclInfo.getGroupId());
        }
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

        return jdbcTemplate.query(query.toString(), parameters, new ResultSetExtractor<List<AclInfo>>() {

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
     *            идентификатор доменного объекта, для которого расчитывается список доступа
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
                    result.add(processAclForDynamicGroupWithContext(invalidContextId, accessType, dynamicGroupName, invalidContextId));

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
        return new AclInfo(accessType, dynamicGroupId);
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
        jdbcTemplate.update(query, parameters);

    }

    private void deleteAclRecord(AccessType accessType, Id objectId, Id dynamicGroupId) {
        RdbmsId rdbmsObjectId = (RdbmsId) objectId;
        RdbmsId rdbmsDynamicGroupId = (RdbmsId) dynamicGroupId;

        String query = null;
        if (accessType == DomainObjectAccessType.READ) {
            query = generateDeleteAclReadRecordQuery(rdbmsObjectId);
        } else {
            query = generateDeleteAclRecordQuery(rdbmsObjectId);

        }

        Map<String, Object> parameters =
                initializeDeleteAclRecordParameters(accessType, rdbmsObjectId, rdbmsDynamicGroupId);
        jdbcTemplate.update(query, parameters);

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
        String roleContextType = null;
        if (contextRoleConfig.getContext() != null && contextRoleConfig.getContext().getDomainObject() != null) {
            roleContextType = contextRoleConfig.getContext().getDomainObject().getType();
        }

        if (!domainObjectType.equalsIgnoreCase(roleContextType)) {
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
                                                AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE,
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
        if (matrixRefType != null){
            permissionType = getMatrixRefType(objectType, matrixRefType, rdbmsObjectId); 
        }
        
        String domainObjectBaseTable = DataStructureNamingHelper.getSqlName(ConfigurationExplorerUtils.getTopLevelParentType(configurationExplorer, objectType));
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
            query.append("and gm.").append(DaoUtils.wrap("person_id")).append(" = :person_id");
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

        return jdbcTemplate.query(query.toString(), parameters, new ResultSetExtractor<List<DomainObjectPermission>>() {

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

                    if (operation.equals("R") && personPermission.getPermission().equals(Permission.None)) {
                        personPermission.setPermission(Permission.Read);
                    } else if (operation.equals("W")
                            &&
                            (personPermission.getPermission().equals(Permission.None) || personPermission
                                    .getPermission().equals(Permission.Read))) {
                        personPermission.setPermission(Permission.Write);
                    } else if (operation.equals("D")
                            &&
                            (personPermission.getPermission().equals(Permission.None)
                                    || personPermission.getPermission().equals(Permission.Read) || personPermission
                                    .getPermission().equals(Permission.Write))) {
                        personPermission.setPermission(Permission.Delete);
                    } else if (operation.startsWith("E_")) {
                        String action = operation.substring(2);
                        personPermission.getActions().add(action);
                    }
                }
                List<DomainObjectPermission> result = new ArrayList<DomainObjectPermission>();

                result.addAll(personPermissions.values());
                return result;
            }
        });
    }

    /**
     * Получение имени типа у которого заимствуются права. При этом учитывается то что в матрице при заимствование 
     * может быть указан атрибут ссылающийся на родительский тип того объекта, у которого реально надо взять матрицу прав
     * @param childType
     * @param parentType
     * @param id
     * @return
     */
    private String getMatrixRefType(String childType, String parentType, RdbmsId id){
    	String rootForChildType = ConfigurationExplorerUtils.getTopLevelParentType(configurationExplorer, childType);
    	String query = "select p.id_type from " + rootForChildType + " c inner join " + parentType + " p on (c.access_object_id = p.id) where c.id = :id";
    	
    	Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("id", id.getId());

        int typeId = jdbcTemplate.query(query, parameters, new ResultSetExtractor<Integer>(){

			@Override
			public Integer extractData(ResultSet rs) throws SQLException,
					DataAccessException {
				rs.next();
				return rs.getInt("id_type");
			}});
        
        return domainObjectTypeIdCache.getName(typeId);
    }    
    
    private void regRecalcInvalidAcl(List<Id> invalidContext) {
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
        private List<Id> contextIds = new ArrayList<Id>();

        public RecalcAclSynchronization() {
        }

        public void addContext(List<Id> invalidContexts) {
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
}
