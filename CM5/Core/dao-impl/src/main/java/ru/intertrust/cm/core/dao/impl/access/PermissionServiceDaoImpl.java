package ru.intertrust.cm.core.dao.impl.access;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

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
import ru.intertrust.cm.core.business.api.dto.RdbmsId;
import ru.intertrust.cm.core.config.ConfigurationException;
import ru.intertrust.cm.core.config.model.AccessMatrixStatusConfig;
import ru.intertrust.cm.core.config.model.BaseOperationPermitConfig;
import ru.intertrust.cm.core.config.model.BasePermit;
import ru.intertrust.cm.core.config.model.CollectorConfig;
import ru.intertrust.cm.core.config.model.ContextRoleConfig;
import ru.intertrust.cm.core.config.model.CreateChildConfig;
import ru.intertrust.cm.core.config.model.DeleteConfig;
import ru.intertrust.cm.core.config.model.DomainObjectTypeConfig;
import ru.intertrust.cm.core.config.model.DynamicGroupConfig;
import ru.intertrust.cm.core.config.model.ExecuteActionConfig;
import ru.intertrust.cm.core.config.model.PermitGroup;
import ru.intertrust.cm.core.config.model.PermitRole;
import ru.intertrust.cm.core.config.model.ReadConfig;
import ru.intertrust.cm.core.config.model.TrackDomainObjectsConfig;
import ru.intertrust.cm.core.config.model.WriteConfig;
import ru.intertrust.cm.core.config.model.base.TopLevelConfig;
import ru.intertrust.cm.core.config.model.doel.DoelExpression;
import ru.intertrust.cm.core.dao.access.AccessType;
import ru.intertrust.cm.core.dao.access.ContextRoleCollector;
import ru.intertrust.cm.core.dao.access.CreateChildAccessType;
import ru.intertrust.cm.core.dao.access.DomainObjectAccessType;
import ru.intertrust.cm.core.dao.access.ExecuteActionAccessType;
import ru.intertrust.cm.core.dao.access.PermissionServiceDao;
import ru.intertrust.cm.core.dao.api.extension.ExtensionPoint;
import ru.intertrust.cm.core.dao.api.extension.OnLoadConfigurationExtensionHandler;
import ru.intertrust.cm.core.model.PermissionException;

/**
 * Реализация сервиса обновления списков доступа.
 * @author atsvetkov
 */
@ExtensionPoint
public class PermissionServiceDaoImpl extends BaseDynamicGroupServiceImpl implements PermissionServiceDao,
        ApplicationContextAware,
        OnLoadConfigurationExtensionHandler {

    private ApplicationContext applicationContext;

    //Реестр коллекторов по отслеживаемому типу
    private Hashtable<String, List<ContextRoleRegisterItem>> collectors =
            new Hashtable<String, List<ContextRoleRegisterItem>>();

    //Реестр коллекторов по имени контекстной роли
    private Hashtable<String, List<ContextRoleRegisterItem>> collectorsByContextRoleNames =
            new Hashtable<String, List<ContextRoleRegisterItem>>();

    /*public void setConfigurationExplorer(ConfigurationExplorer configurationExplorer) {
        this.configurationExplorer = configurationExplorer;
        doelResolver.setConfigurationExplorer(configurationExplorer);
    }*/

    @Override
    public void notifyDomainObjectDeleted(DomainObject domainObject) {
        notifyDomainObjectChangedInternal(domainObject, getDeletedModificationList(domainObject));
        cleanAclFor(domainObject.getId());
    }

    @Override
    public void notifyDomainObjectChanged(DomainObject domainObject, List<FieldModification> modifiedFieldNames) {
        notifyDomainObjectChangedInternal(domainObject, modifiedFieldNames);
    }

    @Override
    public void notifyDomainObjectCreated(DomainObject domainObject) {
        notifyDomainObjectChangedInternal(domainObject, getNewObjectModificationList(domainObject));
    }

    private void
            notifyDomainObjectChangedInternal(DomainObject domainObject, List<FieldModification> modifiedFieldNames) {
        String typeName = domainObject.getTypeName();

        List<ContextRoleRegisterItem> typeCollectors = collectors.get(typeName);
        // Формируем мапу динамических групп, требующих пересчета и их
        // коллекторов, исключая дублирование
        List<Id> invalidContexts = new ArrayList<Id>();
        if (typeCollectors != null) {
            for (ContextRoleRegisterItem dynamicGroupCollector : typeCollectors) {
                // Поучаем невалидные контексты и добавляем их в итоговый массив без дублирования                
                addAllWithoutDuplicate(invalidContexts,
                        dynamicGroupCollector.getCollector().getInvalidContexts(domainObject,
                                modifiedFieldNames));
            }
        }

        // Непосредственно формирование состава, должно вызываться в конце
        // транзакции
        // TODO надо перенести на конец транзакции
        if (invalidContexts != null) {
            for (Id contextId : invalidContexts) {
                refreshAclFor(domainObject, contextId);
            }
        }
    }

    private void refreshAclFor(DomainObject changedObject, Id invalidContextId) {
        RdbmsId rdbmsId = (RdbmsId) invalidContextId;
        String domainObjectType = domainObjectTypeIdCache.getName(rdbmsId.getTypeId());
        String status = getStatusFor(invalidContextId);
        AccessMatrixStatusConfig accessMatrixConfig =
                configurationExplorer.getAccessMatrixByObjectTypeAndStatus(domainObjectType, status);

        if (accessMatrixConfig == null || accessMatrixConfig.getName() == null
                || accessMatrixConfig.getPermissions() == null) {
            return;
        }

        cleanAclFor(invalidContextId);

        for (BaseOperationPermitConfig operationPermitConfig : accessMatrixConfig.getPermissions()) {
            AccessType accessType = getAccessType(operationPermitConfig);
            processOperationPermissions(changedObject, invalidContextId, operationPermitConfig, accessType);
        }

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
     * @param objectId
     *            идентификатор доменного объекта, для которого расчитывается список доступа
     * @param operationPermitConfig
     *            конфигурация разрешений для операции
     * @param accessType
     *            тип операции
     */
    private void processOperationPermissions(DomainObject changedObject, Id invalidContextId,
            BaseOperationPermitConfig operationPermitConfig,
            AccessType accessType) {
        RdbmsId rdbmsId = (RdbmsId) invalidContextId;
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

                List<ContextRoleRegisterItem> collectors =
                        collectorsByContextRoleNames.get(contextRoleConfig.getName());

                if (collectors != null) {
                    for (ContextRoleRegisterItem collectorItem : collectors) {
                        processAclForCollector(changedObject, invalidContextId, collectorItem.getCollector(),
                                accessType);
                    }
                }
            } else if (permit.getClass().equals(PermitGroup.class)) {
                String dynamicGroupName = permit.getName();

                DynamicGroupConfig dynamicGroupConfig =
                        configurationExplorer.getDynamicGroupByName(dynamicGroupName);

                if (dynamicGroupConfig != null && dynamicGroupConfig.getContext() != null
                        && dynamicGroupConfig.getContext().getDomainObject() != null) {

                    // контекстным объектом является текущий объект (для которого
                    // пересчитываются списки доступа)
                    Long contextObjectId = ((RdbmsId) invalidContextId).getId();
                    processAclForDynamicGroupWithContext(invalidContextId, accessType, dynamicGroupName,
                            contextObjectId);

                } else {
                    processAclForDynamicGroupWithoutContext(invalidContextId, accessType, dynamicGroupName);
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

    private void processAclForCollector(DomainObject changedObject, Id invalidContextId,
            ContextRoleCollector collector, AccessType accessType) {
        for (Id groupId : collector.getMembers(changedObject.getId(), invalidContextId)) {
            insertAclRecord(accessType, invalidContextId, groupId);
        }
    }

    private void processAclForDynamicGroupWithoutContext(Id objectId, AccessType accessType, String dynamicGroupName) {
        Id dynamicGroupId = getUserGroupByGroupName(dynamicGroupName);
        if (dynamicGroupId == null) {
            dynamicGroupId = createUserGroup(dynamicGroupName, null);
        }
        insertAclRecord(accessType, objectId, dynamicGroupId);
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
     * @param objectId
     *            отслеживаемый объект матрицы
     * @param doel
     *            выражение внутри тега <bind-context>
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

    @Override
    public void onLoad() {
        try {
            // Поиск конфигураций динамических групп
            for (TopLevelConfig topConfig : configurationExplorer
                    .getConfiguration().getConfigurationList()) {

                if (topConfig instanceof ContextRoleConfig) {
                    ContextRoleConfig config = (ContextRoleConfig) topConfig;
                    // Если контекстная роль настраивается классом коллектором,
                    // то создаем его экземпляр, и добавляем в реестр

                    if (config.getGroups() != null && config.getGroups().getGroups() != null) {

                        for (Object collectorConfig : config.getGroups().getGroups()) {
                            if (collectorConfig instanceof CollectorConfig) {
                                CollectorConfig classCollectorConfig = (CollectorConfig) collectorConfig;
                                Class<?> collectorClass = Class.forName(classCollectorConfig.getClassName());
                                ContextRoleCollector collector = (ContextRoleCollector) applicationContext
                                        .getAutowireCapableBeanFactory()
                                        .createBean(
                                                collectorClass,
                                                AutowireCapableBeanFactory.AUTOWIRE_BY_NAME,
                                                false);
                                collector.init(config, classCollectorConfig.getSettings());
                                registerCollector(collector, config);
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
                                collector.init(config, trackDomainObjectsConfig);
                                registerCollector(collector, config);
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            throw new PermissionException("Error on init collector classes", ex);
        }
    }

    private void registerCollector(ContextRoleCollector collector, ContextRoleConfig config) {
        // Получение типов, которые отслеживает коллектор
        List<String> types = collector.getTrackTypeNames();
        // Регистрируем коллектор в реестре, для обработки
        // только определенных типов
        if (types != null) {
            for (String type : types) {
                registerCollector(type, collector, config);
                // Ищем всех наследников и так же регистрируем
                // их в
                // реестре с данным коллектором
                List<String> subTypes = getSubTypes(type);
                for (String subtype : subTypes) {
                    registerCollector(subtype, collector, config);
                }
            }
        }

        registerCollectorForContextRole(config.getName(), collector, config);
    }

    /**
     * Регистрация коллектора в реестре коллекторов
     * 
     * @param type
     * @param collector
     */
    private void registerCollector(String type, ContextRoleCollector collector, ContextRoleConfig config) {
        List<ContextRoleRegisterItem> typeCollectors = collectors.get(type);
        if (typeCollectors == null) {
            typeCollectors = new ArrayList<ContextRoleRegisterItem>();
            collectors.put(type, typeCollectors);
        }
        typeCollectors.add(new ContextRoleRegisterItem(config, collector));
    }

    /**
     * Получение всех дочерних типов переданного типа
     * 
     * @param type
     * @return
     */
    private List<String> getSubTypes(String type) {
        List<String> result = new ArrayList<String>();
        // Получение всех конфигураций доменных оьъектов
        for (TopLevelConfig topConfig : configurationExplorer
                .getConfiguration().getConfigurationList()) {
            if (topConfig instanceof DomainObjectTypeConfig) {
                DomainObjectTypeConfig config = (DomainObjectTypeConfig) topConfig;
                // Сравнение родительского типа и переданного парамера
                if (config.getExtendsAttribute() != null
                        && config.getExtendsAttribute().equals(type)) {
                    // Если нашли наследника добавляем в результат
                    result.add(config.getName());
                    // Рекурсивно вызываем для получения всех наследников
                    // найденного наследника
                    result.addAll(getSubTypes(config.getName()));
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
        groupCollectors.add(new ContextRoleRegisterItem(config, collector));
    }

    /**
     * Клаксс для описания элемента реестра контекстных ролей
     * @author larin
     * 
     */
    private class ContextRoleRegisterItem {
        private ContextRoleCollector collector;
        private ContextRoleConfig config;

        private ContextRoleRegisterItem(ContextRoleConfig config, ContextRoleCollector collector) {
            this.collector = collector;
            this.config = config;
        }

        public ContextRoleCollector getCollector() {
            return collector;
        }

        public ContextRoleConfig getConfig() {
            return config;
        }
    }

    @Override
    public DomainObjectPermission getObjectPermission(Id domainObjectId, Id userId) {
        List<DomainObjectPermission> result = getObjectPermissions(domainObjectId, userId);
        if (result.size() > 0){
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

        String tableNameRead =
                AccessControlUtility.getAclReadTableName(domainObjectTypeIdCache.getName(rdbmsObjectId.getTypeId()));
        String tableNameAcl =
                AccessControlUtility.getAclTableName(domainObjectTypeIdCache.getName(rdbmsObjectId.getTypeId()));

        String query = "select 'R' as operation, gm.person_id, gm.person_id_type from " + tableNameRead + " r ";
        query += "inner join group_group gg on (r.group_id = gg.child_group_id) ";
        query += "inner join group_member gm on (gg.parent_group_id = gm.usergroup) ";
        query += "where r.object_id = :object_id ";
        if (personId != null){
            query += "and gm.person_id = :person_id";
        }
        query += "union ";
        query += "select a.operation, gm.person_id, gm.person_id_type from " + tableNameAcl + " a ";
        query += "inner join group_group gg on (a.group_id = gg.child_group_id) ";
        query += "inner join group_member gm on (gg.parent_group_id = gm.usergroup) ";
        query += "where a.object_id = :object_id ";
        if (personId != null){
            query += "and gm.person_id = :person_id";
        }

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("object_id", rdbmsObjectId.getId());
        if (personId != null){
            parameters.put("person_id", ((RdbmsId)personId).getId());
        }
        
        return jdbcTemplate.query(query, parameters, new ResultSetExtractor<List<DomainObjectPermission>>() {

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
                        personPermission.setPermission(Permission.Delate);
                    }else if (operation.startsWith("E_")) {
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

}
