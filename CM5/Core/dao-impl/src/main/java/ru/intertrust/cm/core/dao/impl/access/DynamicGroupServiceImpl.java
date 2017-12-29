package ru.intertrust.cm.core.dao.impl.access;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.Synchronization;
import javax.transaction.TransactionSynchronizationRegistry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import ru.intertrust.cm.core.business.api.dto.Case;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.FieldModification;
import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObject;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.config.CollectorConfig;
import ru.intertrust.cm.core.config.DynamicGroupConfig;
import ru.intertrust.cm.core.config.DynamicGroupTrackDomainObjectsConfig;
import ru.intertrust.cm.core.config.base.Configuration;
import ru.intertrust.cm.core.config.base.TopLevelConfig;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.access.DynamicGroupCollector;
import ru.intertrust.cm.core.dao.access.DynamicGroupService;
import ru.intertrust.cm.core.dao.access.DynamicGroupSettings;
import ru.intertrust.cm.core.dao.api.extension.BeforeDeleteExtensionHandler;
import ru.intertrust.cm.core.dao.api.extension.ExtensionPoint;
import ru.intertrust.cm.core.dao.api.extension.OnLoadConfigurationExtensionHandler;
import ru.intertrust.cm.core.dao.exception.DaoException;
import ru.intertrust.cm.core.model.AccessException;
import ru.intertrust.cm.core.model.CrudException;
import ru.intertrust.cm.core.model.ObjectNotFoundException;
import ru.intertrust.cm.core.model.PermissionException;
import ru.intertrust.cm.core.model.UnexpectedException;

/**
 * Реализация сервиса по работе с динамическими группами пользователей
 * 
 * @author atsvetkov
 */
@ExtensionPoint(filter = "Person")
public class DynamicGroupServiceImpl extends BaseDynamicGroupServiceImpl
        implements DynamicGroupService, ApplicationContextAware,
        OnLoadConfigurationExtensionHandler, BeforeDeleteExtensionHandler {

    final static Logger logger = LoggerFactory
            .getLogger(DynamicGroupServiceImpl.class);

    @Autowired
    private DynamicGroupSettings dynamicGroupSettings;
    
    @Resource
    private TransactionSynchronizationRegistry txReg;

    private Hashtable<String, List<DynamicGroupRegisterItem>> collectorsByTrackingType =
            new Hashtable<String, List<DynamicGroupRegisterItem>>();
    private Hashtable<String, List<DynamicGroupRegisterItem>> collectorsByGroupName =
            new Hashtable<String, List<DynamicGroupRegisterItem>>();
    private Hashtable<String, List<DynamicGroupConfig>> configsByContextType =
            new Hashtable<String, List<DynamicGroupConfig>>();

    private ApplicationContext applicationContext;

    @Override
    public void notifyDomainObjectChanged(DomainObject domainObject,
            List<FieldModification> modifiedFieldNames, Set<Id> beforeSaveInvalidGroups) {
        String typeName = domainObject.getTypeName();

        if (!dynamicGroupSettings.isDisableGroupCalculation()) {

            List<DynamicGroupRegisterItem> typeCollectors = collectorsByTrackingType.get(Case.toLower(typeName));
            // Формируем список динамических групп, требующих пересчета и их
            // коллекторов, исключая дублирование
            Set<Id> invalidGroups = new HashSet<Id>();
            if (typeCollectors != null) {
                for (DynamicGroupRegisterItem dynamicGroupCollector : typeCollectors) {
                    // Получаем невалидные контексты для
                    List<Id> invalidContexts =
                            dynamicGroupCollector.getCollector().getInvalidContexts(domainObject, modifiedFieldNames);

                    for (Id invalidContext : invalidContexts) {
                        Id dynamicGroupId = refreshUserGroup(dynamicGroupCollector.getConfig().getName(), invalidContext);
                        if (dynamicGroupId != null) {
                            invalidGroups.add(dynamicGroupId);
                        }
                    }
                }
            }

            if (beforeSaveInvalidGroups != null) {
                invalidGroups.addAll(beforeSaveInvalidGroups);
            }

            // Непосредственно формирование состава, должно вызываться в конце транзакции
            regRecalcInvalidGroups(invalidGroups);
        }
    }

    /**
     * Пересчет состава динамической группы
     * @param domainObject
     * @param groupId
     */
    @Override
    public void recalcGroup(Id groupId) {
        // Получаем группу
        AccessToken accessToken = accessControlService
                .createSystemAccessToken(this.getClass().getName());
        DomainObject dynGroup = domainObjectDao.find(groupId, accessToken);

        DynamicGroupConfig config =
                configurationExplorer.getConfig(DynamicGroupConfig.class, dynGroup.getString("group_name"));

        if (config != null){
            //Получаем состав
            Set<Id> groupMembres = new HashSet<>();
            Set<Id> groupMembresGroups = new HashSet<Id>();
    
            List<DynamicGroupRegisterItem> collectors = collectorsByGroupName.get(config.getName());
            if (collectors != null) {
                for (DynamicGroupRegisterItem dynamicGroupRegisterItem : collectors) {
                    addAllWithoutDuplicate(groupMembres,
                            dynamicGroupRegisterItem.getCollector().getPersons(dynGroup.getReference("object_id")));
                    addAllWithoutDuplicate(groupMembresGroups,
                            dynamicGroupRegisterItem.getCollector().getGroups(dynGroup.getReference("object_id")));
                }
            }
    
            // Выполняю пересчет
            refreshGroupMembers(groupId, groupMembres, groupMembresGroups);
        }
    }

    /**
     * Выполняет пересчет всех динамических групп, где созданный объект является
     * отслеживаемым (указан в теге <track-domain-objects>).
     */
    @Override
    public void notifyDomainObjectCreated(DomainObject domainObject) {

        String typeName = domainObject.getTypeName();

        //При создание объекта получаем все его динамические группы и создаем их, пока пустыми
        createAllDynamicGroups(domainObject);

        if (!dynamicGroupSettings.isDisableGroupCalculation()) {

            List<DynamicGroupRegisterItem> typeCollectors = collectorsByTrackingType.get(Case.toLower(typeName));
            // Формируем мап динамических групп, требующих пересчета и их
            // коллекторов, исключая дублирование
            Set<Id> invalidGroups = new HashSet<Id>();
            if (typeCollectors != null) {
                for (DynamicGroupRegisterItem dynamicGroupCollector : typeCollectors) {
                    // Получаем невалидные контексты для
                    List<Id> invalidContexts =
                            dynamicGroupCollector.getCollector().getInvalidContexts(domainObject,
                                    getNewObjectModificationList(domainObject));

                    for (Id invalidContext : invalidContexts) {
                        Id dynamicGroupId = refreshUserGroup(dynamicGroupCollector.getConfig().getName(), invalidContext);
                        if (dynamicGroupId != null) {
                            invalidGroups.add(dynamicGroupId);
                        }
                    }
                }
            }

            // Непосредственно формирование состава, должно вызываться в конце
            // транзакции
            regRecalcInvalidGroups(invalidGroups);
        }
    }

    private void createAllDynamicGroups(DomainObject domainObject) {
        List<DynamicGroupConfig> configs = configsByContextType.get(Case.toLower(domainObject.getTypeName()));
        if (configs != null) {
            createUserGroups(domainObject, configs);
        }
    }

    /**
     * Пересчитывает список персон динамической группы.
     * 
     * @param dynamicGroupId
     *            идентификатор динамической группы
     * @param personIds
     *            список персон
     */
    private void refreshGroupMembers(Id dynamicGroupId, Set<Id> newPersonIds, Set<Id> newGroupIds) {
        //Оптимизируем метод заполнения динамической группы
        /*personManagementService.removeGroupMembers(dynamicGroupId);
        insertGroupMembers(dynamicGroupId, personIds);
        insertGroupMembersGroups(dynamicGroupId, groupIds);*/

        final List<Id> oldGroupIdsList = getIdListFromDomainObjectList(personManagementService.getChildGroups(dynamicGroupId));
        final List<Id> oldPersonIdsList = getIdListFromDomainObjectList(personManagementService.getPersonsInGroup(dynamicGroupId));
        final Set<Id> oldGroupIds = oldGroupIdsList.isEmpty() ? Collections.<Id> emptySet() : new HashSet<>(oldGroupIdsList);
        final Set<Id> oldPersonIds = oldPersonIdsList.isEmpty() ? Collections.<Id> emptySet() : new HashSet<>(oldPersonIdsList);

        //Сравнение списков и получение дельты
        //Получение добавленных персон
        List<Id> addPerson = new ArrayList<Id>();
        for (Id personId : newPersonIds) {
            if (!oldPersonIds.contains(personId)) {
                addPerson.add(personId);
            }
        }

        //Получить добавленные группы
        List<Id> addGroup = new ArrayList<Id>();
        for (Id groupId : newGroupIds) {
            if (!oldGroupIds.contains(groupId)) {
                addGroup.add(groupId);
            }
        }

        //Получение удаленных персон
        List<Id> deletePerson = new ArrayList<Id>();
        for (Id personId : oldPersonIds) {
            if (!newPersonIds.contains(personId)) {
                deletePerson.add(personId);
            }
        }

        //Получить удаленные группы
        List<Id> deleteGroup = new ArrayList<Id>();
        for (Id groupId : oldGroupIds) {
            if (!newGroupIds.contains(groupId)) {
                deleteGroup.add(groupId);
            }
        }
        deleteGroupMembers(dynamicGroupId, deletePerson);
        deleteGroupMembersGroups(dynamicGroupId, deleteGroup);

        insertGroupMembers(dynamicGroupId, addPerson);
        insertGroupMembersGroups(dynamicGroupId, addGroup);

    }

    private void deleteGroupMembersGroups(Id dynamicGroupId, List<Id> deleteGroup) {
        if (deleteGroup != null) {
           personManagementService.remoteGroupFromGroups(dynamicGroupId, deleteGroup);
        }
    }

    private void deleteGroupMembers(Id dynamicGroupId, List<Id> deletePerson) {
        if (deletePerson != null) {
            personManagementService.remotePersonsFromGroup(dynamicGroupId, deletePerson);
        }
    }

    /**
     * Добавление в динамическую группу другие группы
     * @param dynamicGroupId
     * @param groupIds
     */
    private void insertGroupMembersGroups(Id dynamicGroupId, List<Id> groupIds) {
        if (groupIds != null) {
            personManagementService.addGroupsToGroup(dynamicGroupId, groupIds);
        }
    }

    // TODO Optimize performance
    private void insertGroupMembers(Id dynamicGroupId, List<Id> personIds) {
        if (personIds != null) {
            AccessToken accessToken = accessControlService.createSystemAccessToken(this.getClass().getName());

            List<DomainObject> groupMembers = new ArrayList<DomainObject>();
            for (Id personValue : personIds) {
                GenericDomainObject groupMemeber = new GenericDomainObject();
                groupMemeber.setTypeName(GenericDomainObject.GROUP_MEMBER_DOMAIN_OBJECT);
                groupMemeber.setReference("UserGroup", dynamicGroupId);
                groupMemeber.setReference("person_id", personValue);
                groupMembers.add(groupMemeber);
            }
            domainObjectDao.save(groupMembers, accessToken);
        }
    }

    protected Map<String, Object> initializeDeleteGroupMembersParameters(
            Id groupId) {
        RdbmsId rdbmsId = (RdbmsId) groupId;
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("usergroup", rdbmsId.getId());
        return parameters;
    }

    /**
     * Добавляет группу с данным именем и контекстным объектом, если группы нет
     * в базе данных
     * 
     * @param dynamicGroupName
     *            имя динамической группы
     * @param contextObjectId
     *            контекстный объект динамической группы
     * @return обновленную динамическую группу
     */
    private Id refreshUserGroup(String dynamicGroupName, Id contextObjectId) {
        Id userGroupId = null;
        if (contextObjectId == null) {
            userGroupId = personManagementService.getGroupId(dynamicGroupName);
        } else {
            userGroupId = getUserGroupByGroupNameAndObjectId(dynamicGroupName, contextObjectId);
        }

        if (userGroupId == null) {
            AccessToken accessToken = accessControlService
                    .createSystemAccessToken(this.getClass().getName());

            DynamicGroupConfig config = configurationExplorer.getConfig(DynamicGroupConfig.class, dynamicGroupName);
            if (contextObjectId == null || applyFilter(domainObjectDao.find(contextObjectId, accessToken), config)) {
                userGroupId = createUserGroup(dynamicGroupName, contextObjectId);
            }
        }

        return userGroupId;
    }

    @Override
    public void notifyDomainObjectDeleted(DomainObject domainObject, Set<Id> beforeDeleteInvalidGroups) {
        String typeName = domainObject.getTypeName();

        if (!dynamicGroupSettings.isDisableGroupCalculation()) {

            List<DynamicGroupRegisterItem> typeCollectors = collectorsByTrackingType.get(Case.toLower(typeName));
            // Формируем список динамических групп, требующих пересчета и их
            // коллекторов, исключая дублирование
            Set<Id> invalidGroups = new HashSet<Id>();
            if (typeCollectors != null) {
                for (DynamicGroupRegisterItem dynamicGroupCollector : typeCollectors) {
                    // Получаем невалидные контексты для
                    List<Id> invalidContexts =
                            dynamicGroupCollector.getCollector().getInvalidContexts(domainObject,
                                    getDeletedModificationList(domainObject));

                    for (Id invalidContext : invalidContexts) {
                        Id dynamicGroupId = refreshUserGroup(dynamicGroupCollector.getConfig().getName(), invalidContext);
                        if (dynamicGroupId != null) {
                            invalidGroups.add(dynamicGroupId);
                        }
                    }
                }
            }
            invalidGroups.addAll(beforeDeleteInvalidGroups);

            // Непосредственно формирование состава, должно вызываться в конце
            // транзакции
            regRecalcInvalidGroups(invalidGroups);

            //Удаление всех контекстных групп, которые висят на удаляемом доменном объекте
            regDeleteContextGroups(domainObject);
        }
    }

    /**
     * Удаление всех динамических групп, которые висят на удаляемом доменном
     * объектах
     * @param domainObject
     */
    private void deleteContextGroups(Id domainObjectId) {
        AccessToken accessToken = accessControlService
                .createSystemAccessToken(this.getClass().getName());

        String query = "select t.id from user_group t where object_id = {0}";
        List<Value> params = new ArrayList<>();
        params.add(new ReferenceValue(domainObjectId));
        IdentifiableObjectCollection collection = collectionsService.findCollectionByQuery(query, params, 0, 0, accessToken);

        List<Id> idForDelete = new ArrayList<Id>();
        for (IdentifiableObject identifiableObject : collection) {
            idForDelete.add(identifiableObject.getId());
        }
        if (idForDelete.size() > 0) {
            domainObjectDao.delete(idForDelete, accessToken);
        }
    }

    /**
     * Метод вызывается после загрузки конфигурации. Пробегает по конфигурации,
     * собирает информацию о динамических группах, настраиваемых с помощью
     * классов и создает экземпляры этих классов, добавляет их в реестр
     */
    @Override
    public void onLoad() {
        try {
            // Поиск конфигураций динамических групп
            Configuration configuration = configurationExplorer.getConfiguration();
            List<TopLevelConfig> configurationList = configuration.getConfigurationList();
            for (TopLevelConfig topConfig : configurationList) {

                if (topConfig instanceof DynamicGroupConfig) {
                    DynamicGroupConfig config = (DynamicGroupConfig) topConfig;

                    //Регистрации конфигурации для типов контекстов с учетом иерархии типов
                    if (config.getContext() != null) {
                        registerConfig(config.getContext().getDomainObject().getType(), config, configuration);
                    }

                    if (config.getMembers() != null) {
                        // Если динамическая группа настраивается классом
                        // коллектором,
                        // то создаем его экземпляр, и добавляем в реестр
                        if (config.getMembers().getCollector() != null) {

                            for (CollectorConfig collectorConfig : config.getMembers().getCollector()) {
                                Class<?> collectorClass = Class.forName(collectorConfig.getClassName());
                                DynamicGroupCollector collector = (DynamicGroupCollector) applicationContext
                                        .getAutowireCapableBeanFactory()
                                        .createBean(
                                                collectorClass,
                                                AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE,
                                                false);
                                collector.init(config, collectorConfig.getSettings());
                                registerCollector(collector, config, configuration);

                            }
                        } else {

                            for (DynamicGroupTrackDomainObjectsConfig collectorConfig : config.getMembers()
                                    .getTrackDomainObjects()) {
                                // Специфичный коллектор не указан используем коллектор
                                // по умолчанию
                                DynamicGroupCollector collector = (DynamicGroupCollector) applicationContext
                                        .getAutowireCapableBeanFactory()
                                        .createBean(
                                                DynamicGroupTrackDomainObjectCollector.class,
                                                AutowireCapableBeanFactory.AUTOWIRE_BY_NAME,
                                                false);
                                collector.init(config, collectorConfig);
                                registerCollector(collector, config, configuration);
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            throw new PermissionException("Error on init collector classes", ex);
        }
    }

    /**
     * Создание реестра конфигураций по типам контекстов, с учетом наследования
     * @param contextType
     * @param dynamicGroupConfig
     */
    private void registerConfig(String contextType, DynamicGroupConfig dynamicGroupConfig,
            Configuration configuration) {
        registerOneTypeConfig(contextType, dynamicGroupConfig);
        List<String> subTypes = AccessControlUtility.getSubTypes(contextType, configurationExplorer);
        for (String subtype : subTypes) {
            registerOneTypeConfig(subtype, dynamicGroupConfig);
        }
    }

    /**
     * Регистрация конфигурации для типа контекста в реестре
     * @param type
     * @param config
     */
    private void registerOneTypeConfig(String type, DynamicGroupConfig config) {
        List<DynamicGroupConfig> typeCollectors = configsByContextType.get(Case.toLower(type));
        if (typeCollectors == null) {
            typeCollectors = new ArrayList<DynamicGroupConfig>();
            configsByContextType.put(Case.toLower(type), typeCollectors);
        }
        typeCollectors.add(config);
    }

    /**
     * Регистрация коллектора для отслеживаемого типа доменного объекта
     * @param collector
     * @param dynamicGroupConfig
     */
    private void registerCollector(DynamicGroupCollector collector, DynamicGroupConfig dynamicGroupConfig,
            Configuration configuration) {
        // Получение типов, которые отслеживает коллектор
        List<String> types = collector.getTrackTypeNames();
        // Регистрируем коллектор в реестре, для обработки
        // только определенных типов
        if (types != null) {
            for (String type : types) {
                registerCollector(type, collector, dynamicGroupConfig);
                // Ищем всех наследников и так же регистрируем
                // их в
                // реестре с данным коллектором
                List<String> subTypes = AccessControlUtility.getSubTypes(type, configurationExplorer);
                for (String subtype : subTypes) {
                    registerCollector(subtype, collector, dynamicGroupConfig);
                }
            }
        }
        registerCollectorForDynamicGroup(dynamicGroupConfig.getName(), collector, dynamicGroupConfig);
    }

    /**
     * Регистрация коллектора в реестре колекторов для дмнамической группы
     * 
     * @param type
     * @param collector
     */
    private void registerCollectorForDynamicGroup(String groupName, DynamicGroupCollector collector,
            DynamicGroupConfig config) {
        List<DynamicGroupRegisterItem> groupCollectors = collectorsByGroupName.get(groupName);
        if (groupCollectors == null) {
            groupCollectors = new ArrayList<>();
            collectorsByGroupName.put(groupName, groupCollectors);
        }
        groupCollectors.add(new DynamicGroupRegisterItem(config, collector));
    }

    /**
     * Регистрация коллектора в реестре колекторов
     * 
     * @param type
     * @param collector
     */
    private void registerCollector(String type, DynamicGroupCollector collector, DynamicGroupConfig config) {
        String typeKey = Case.toLower(type);
        List<DynamicGroupRegisterItem> typeCollectors = collectorsByTrackingType.get(typeKey);
        if (typeCollectors == null) {
            typeCollectors = new ArrayList<DynamicGroupRegisterItem>();
            collectorsByTrackingType.put(typeKey, typeCollectors);
        }
        typeCollectors.add(new DynamicGroupRegisterItem(config, collector));
    }

    /**
     * Установка спринг контекста в экземпляр класса
     * 
     * @param applicationContext
     * @throws BeansException
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * Клаксс для описания элемента реестра динамических групп
     * @author larin
     * 
     */
    private class DynamicGroupRegisterItem {
        private DynamicGroupCollector collector;
        private DynamicGroupConfig config;

        private DynamicGroupRegisterItem(DynamicGroupConfig config, DynamicGroupCollector collector) {
            this.collector = collector;
            this.config = config;
        }

        public DynamicGroupCollector getCollector() {
            return collector;
        }

        public DynamicGroupConfig getConfig() {
            return config;
        }
    }

    @Override
    public void onBeforeDelete(DomainObject deletedDomainObject) {
        //Удаляем в цикле, пока ничего не останется
        int deleteRowCount = 1;
        while (deleteRowCount > 0) {
            deleteRowCount = deleteGroupMembers(deletedDomainObject);
        }
    }

    private int deleteGroupMembers(DomainObject deletedDomainObject) {
        //Получение персон, которые числятся членами группы
        AccessToken accessToken = accessControlService
                .createSystemAccessToken(this.getClass().getName());
        String query =
                "select t.id from group_member t where t.person_id = "
                        + ((RdbmsId) deletedDomainObject.getId()).getId();
        IdentifiableObjectCollection collection = collectionsService.findCollectionByQuery(query, 0, 0, accessToken);
        List<Id> ids = new ArrayList<>();
        for (IdentifiableObject identifiableObject : collection) {
            ids.add(identifiableObject.getId());
        }
        return ids.isEmpty() ? 0 : domainObjectDao.delete(ids, accessToken);
    }

    @Override
    public Set<Id> getInvalidGroupsBeforeChange(DomainObject domainObject, List<FieldModification> modifiedFieldNames) {
        String typeName = domainObject.getTypeName();
        Set<Id> invalidGroups = new HashSet<Id>();

        if (!dynamicGroupSettings.isDisableGroupCalculation()) {
            List<DynamicGroupRegisterItem> typeCollectors = collectorsByTrackingType.get(Case.toLower(typeName));
            // Формируем мапу динамических групп, требующих пересчета и их
            // коллекторов, исключая дублирование
            if (typeCollectors != null) {
                for (DynamicGroupRegisterItem dynamicGroupCollector : typeCollectors) {
                    // Поучаем невалидные контексты для
                    List<Id> invalidContexts =
                            dynamicGroupCollector.getCollector().getInvalidContexts(domainObject, modifiedFieldNames);

                    for (Id invalidContext : invalidContexts) {
                        Id dynamicGroupId = refreshUserGroup(dynamicGroupCollector.getConfig().getName(), invalidContext);
                        if (dynamicGroupId != null) {
                            invalidGroups.add(dynamicGroupId);
                        }
                    }
                }
            }
        }
        return invalidGroups;
    }

    @Override
    public Set<Id> getInvalidGroupsBeforeDelete(DomainObject domainObject) {
        String typeName = domainObject.getTypeName();
        Set<Id> invalidGroups = new HashSet<Id>();

        if (!dynamicGroupSettings.isDisableGroupCalculation()) {
            List<DynamicGroupRegisterItem> typeCollectors = collectorsByTrackingType.get(Case.toLower(typeName));
            // Формируем мапу динамических групп, требующих пересчета и их
            // коллекторов, исключая дублирование
            if (typeCollectors != null) {
                for (DynamicGroupRegisterItem dynamicGroupCollector : typeCollectors) {
                    // Поучаем невалидные контексты для
                    List<Id> invalidContexts =
                            dynamicGroupCollector.getCollector().getInvalidContexts(domainObject,
                                    getDeletedModificationList(domainObject));

                    for (Id invalidContext : invalidContexts) {
                        Id dynamicGroupId = refreshUserGroup(dynamicGroupCollector.getConfig().getName(), invalidContext);
                        if (dynamicGroupId != null) {
                            invalidGroups.add(dynamicGroupId);
                        }
                    }
                }
            }
        }
        return invalidGroups;
    }

    private void regDeleteContextGroups(DomainObject domainObject) {
        if (getTxReg().getTransactionKey() == null) {
            return;
        }
        RecalcGroupSynchronization recalcGroupSynchronization =
                (RecalcGroupSynchronization) getTxReg().getResource(RecalcGroupSynchronization.class);
        if (recalcGroupSynchronization == null) {
            recalcGroupSynchronization = new RecalcGroupSynchronization();
            getTxReg().putResource(RecalcGroupSynchronization.class, recalcGroupSynchronization);
            getTxReg().registerInterposedSynchronization(recalcGroupSynchronization);
        }
        recalcGroupSynchronization.addDeleteContextGroups(domainObject.getId());
    }

    private void regRecalcInvalidGroups(Set<Id> invalidGroups) {
        //не обрабатываем вне транзакции
        if (getTxReg().getTransactionKey() == null) {
            return;
        }
        RecalcGroupSynchronization recalcGroupSynchronization =
                (RecalcGroupSynchronization) getTxReg().getResource(RecalcGroupSynchronization.class);
        if (recalcGroupSynchronization == null) {
            recalcGroupSynchronization = new RecalcGroupSynchronization();
            getTxReg().putResource(RecalcGroupSynchronization.class, recalcGroupSynchronization);
            getTxReg().registerInterposedSynchronization(recalcGroupSynchronization);
        }
        recalcGroupSynchronization.addGroups(invalidGroups);
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

    private class RecalcGroupSynchronization implements Synchronization {
        private Set<Id> groupIds = new HashSet<Id>();
        private List<Id> contextsForDelete = new ArrayList<Id>();

        public RecalcGroupSynchronization() {
        }

        public void addDeleteContextGroups(Id contextId) {
            if (!contextsForDelete.contains(contextId)) {
                contextsForDelete.add(contextId);
            }
        }

        public void addGroups(Set<Id> invalidGroups) {
            addAllWithoutDuplicate(groupIds, invalidGroups);
        }

        @Override
        public void beforeCompletion() {
            try {
                for (Id groupId : groupIds) {
                    recalcGroup(groupId);
                }

                for (int i = contextsForDelete.size() - 1; i > -1; i--) {
                    deleteContextGroups(contextsForDelete.get(i));
                }
            } catch (AccessException | ObjectNotFoundException | NullPointerException | CrudException ex) {
                throw ex;
            } catch (Exception ex) {
                logger.error("Unexpected exception caught in beforeCompletion", ex);
                throw new UnexpectedException("RecalcGroupSynchronization.beforeCompletion " + ex.getMessage());
            }
        }

        @Override
        public void afterCompletion(int status) {
        }
    }

    @Override
    public List<Id> getPersons(Id contextId, String groupName) {
        List<DomainObject> persons = null;
        DomainObject group = personManagementService.findDynamicGroup(groupName, contextId);
        if (group != null) {
            persons = personManagementService.getAllPersonsInGroup(group.getId());
        }
        return getIdListFromDomainObjectList(persons);
    }

}
