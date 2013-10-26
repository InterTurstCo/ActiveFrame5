package ru.intertrust.cm.core.dao.impl.access;

import static ru.intertrust.cm.core.dao.impl.DataStructureNamingHelper.getSqlName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.FieldModification;
import ru.intertrust.cm.core.business.api.dto.FieldModificationImpl;
import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.RdbmsId;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.config.ConfigurationException;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.model.BindContextConfig;
import ru.intertrust.cm.core.config.model.DoelAware;
import ru.intertrust.cm.core.config.model.DomainObjectTypeConfig;
import ru.intertrust.cm.core.config.model.DynamicGroupConfig;
import ru.intertrust.cm.core.config.model.GetPersonConfig;
import ru.intertrust.cm.core.config.model.TrackDomainObjectsConfig;
import ru.intertrust.cm.core.config.model.base.TopLevelConfig;
import ru.intertrust.cm.core.config.model.doel.DoelExpression;
import ru.intertrust.cm.core.config.model.doel.DoelExpression.Element;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.access.DynamicGroupCollector;
import ru.intertrust.cm.core.dao.access.DynamicGroupService;
import ru.intertrust.cm.core.dao.api.extension.ExtensionPoint;
import ru.intertrust.cm.core.dao.api.extension.OnLoadConfigurationExtensionHandler;
import ru.intertrust.cm.core.model.PermissionException;

/**
 * Реализация сервиса по работе с динамическими группами пользователей
 * 
 * @author atsvetkov
 */
@ExtensionPoint
public class DynamicGroupServiceImpl extends BaseDynamicGroupServiceImpl
        implements DynamicGroupService, ApplicationContextAware,
        OnLoadConfigurationExtensionHandler {

    final static Logger logger = LoggerFactory
            .getLogger(DynamicGroupServiceImpl.class);

    private Hashtable<String, List<DynamicGroupCollector>> collectors = new Hashtable<String, List<DynamicGroupCollector>>();

    @Autowired
    private ConfigurationExplorer configurationExplorer;

    @Autowired
    private AccessControlService accessControlService;

    private ApplicationContext applicationContext;

    public void setConfigurationExplorer(
            ConfigurationExplorer configurationExplorer) {
        this.configurationExplorer = configurationExplorer;
        doelResolver.setConfigurationExplorer(configurationExplorer);
    }

    @Override
    public void notifyDomainObjectChanged(DomainObject domainObject,
            List<FieldModification> modifiedFieldNames) {
        String typeName = domainObject.getTypeName();

        List<DynamicGroupCollector> typeCollectors = collectors.get(typeName);
        // Формируем мапу динамических грпп, требующих пересчета и их
        // коллекторов, исключая дублирование
        Map<Id, DynamicGroupCollector> invalidGroups = new Hashtable<Id, DynamicGroupCollector>();
        for (DynamicGroupCollector dynamicGroupCollector : typeCollectors) {

            List<Id> collectorInvalidGroups = dynamicGroupCollector
                    .getInvalidDynamicGroups(domainObject,
                            modifiedFieldNames);
            // Добавляем в массив групп, исключая дублирование
            for (Id id : collectorInvalidGroups) {
                invalidGroups.put(id, dynamicGroupCollector);
            }
        }

        // Непосредственно формирование состава, должно выхыватся в конце
        // транзакции
        // TODO надо перенести на конец транзакции
        for (Id droupId : invalidGroups.keySet()) {
            DynamicGroupCollector collector = invalidGroups.get(droupId);
            // Получаем группу
            AccessToken accessToken = accessControlService
                    .createSystemAccessToken(this.getClass().getName());
            DomainObject dynGroup = domainObjectDao.find(droupId, accessToken);

            // Выполняю пересчет
            List<Id> groupMembres = collector.getPersons(dynGroup
                    .getReference("object_id"));
            refreshGroupMembers(droupId, groupMembres);
        }

        /*
         * String status = getStatusFor(domainObject.getId());
         * List<DynamicGroupConfig> dynamicGroups =
         * getDynamicGroupsToRecalculateForUpdate( domainObject.getId(), status,
         * modifiedFieldNames); logger.info("Found dynamic groups by id " +
         * domainObject.getId() + " :" + dynamicGroups); for (DynamicGroupConfig
         * dynamicGroupConfig : dynamicGroups) { Id contextObjectid =
         * getContextObjectId(dynamicGroupConfig, domainObject.getId()); Id
         * dynamicGroupId = refreshUserGroup(dynamicGroupConfig.getName(),
         * contextObjectid); List<Value> groupMembres = getAllGroupMembersFor(
         * dynamicGroupConfig, domainObject.getId(), contextObjectid);
         * 
         * refreshGroupMembers(dynamicGroupId, groupMembres); }
         */
    }

    /**
     * Выполняет пересчет всех динамических групп, где созданный объект является
     * отслеживаемым (указан в теге <track-domain-objects>).
     */
    @Override
    public void notifyDomainObjectCreated(DomainObject domainObject) {

        String typeName = domainObject.getTypeName();

        List<DynamicGroupCollector> typeCollectors = collectors.get(typeName);
        // Формируем мапу динамических грпп, требующих пересчета и их
        // коллекторов, исключая дублирование
        Map<Id, DynamicGroupCollector> invalidGroups = new Hashtable<Id, DynamicGroupCollector>();
        for (DynamicGroupCollector dynamicGroupCollector : typeCollectors) {

            List<Id> collectorInvalidGroups = dynamicGroupCollector
                    .getInvalidDynamicGroups(domainObject,
                            getNewObjectModificationList(domainObject));
            // Добавляем в массив групп, исключая дублирование
            for (Id id : collectorInvalidGroups) {
                invalidGroups.put(id, dynamicGroupCollector);
            }
        }

        // Непосредственно формирование состава, должно выхыватся в конце
        // транзакции
        // TODO надо перенести на конец транзакции
        for (Id droupId : invalidGroups.keySet()) {
            DynamicGroupCollector collector = invalidGroups.get(droupId);
            // Получаем группу
            AccessToken accessToken = accessControlService
                    .createSystemAccessToken(this.getClass().getName());
            DomainObject dynGroup = domainObjectDao.find(droupId, accessToken);

            // Выполняю пересчет
            List<Id> groupMembres = collector.getPersons(dynGroup
                    .getReference("object_id"));
            refreshGroupMembers(droupId, groupMembres);
        }

        /*
         * String status = getStatusFor(domainObject.getId());
         * 
         * List<DynamicGroupConfig> dynamicGroups =
         * getDynamicGroupsToRecalculate( domainObject.getId(), status); for
         * (DynamicGroupConfig dynamicGroupConfig : dynamicGroups) { Id
         * contextObjectid = getContextObjectId(dynamicGroupConfig,
         * domainObject.getId()); Id dynamicGroupId =
         * refreshUserGroup(dynamicGroupConfig.getName(), contextObjectid);
         * List<Value> groupMembres = getAllGroupMembersFor( dynamicGroupConfig,
         * domainObject.getId(), contextObjectid);
         * 
         * refreshGroupMembers(dynamicGroupId, groupMembres); }
         */
    }

    private List<FieldModification> getNewObjectModificationList(
            DomainObject domainObject) {
        List<FieldModification> result = new ArrayList<FieldModification>();

        for (String fieldName : domainObject.getFields()) {
            result.add(new FieldModificationImpl(fieldName, null, domainObject
                    .getValue(fieldName)));
        }

        return result;
    }

    private List<FieldModification> getDeletedModificationList(
            DomainObject domainObject) {
        List<FieldModification> result = new ArrayList<FieldModification>();

        for (String fieldName : domainObject.getFields()) {
            result.add(new FieldModificationImpl(fieldName, domainObject
                    .getValue(fieldName), null));
        }

        return result;
    }

    /**
     * Получает список персон динамической группы по дескриптору группы и
     * контекстному объекту.
     * 
     * @param dynamicGroupConfig
     *            дескриптор динамической группы
     * @param objectId
     *            отслеживаемй объект динамической группы. Используется для
     *            расчета обратного Doel выражения.
     * @param contextObjectid
     *            контекстному объекту динамической группы
     * @return список персон группы
     */
    private List<Value> getAllGroupMembersFor(
            DynamicGroupConfig dynamicGroupConfig, Id objectId,
            Id contextObjectid) {
        List<Value> result = null;
        TrackDomainObjectsConfig trackDomainObjects = dynamicGroupConfig
                .getMembers().getTrackDomainObjects();
        if (trackDomainObjects != null
                && trackDomainObjects.getBindContext() != null) {
            String bindContextDoel = trackDomainObjects.getBindContext()
                    .getDoel();
            DoelExpression bindContextExpr = DoelExpression
                    .parse(bindContextDoel);
            DoelExpression reverseBindContextExpr = createReverseExpression(
                    objectId, bindContextExpr);

            String getPersonDoel = null;
            if (trackDomainObjects.getGetPerson() != null
                    && trackDomainObjects.getGetPerson().getDoel() != null) {
                getPersonDoel = trackDomainObjects.getGetPerson().getDoel();
            }

            String getGroupPersonsDoel = createRetrieveGroupPersonsDoel(
                    reverseBindContextExpr, getPersonDoel);
            DoelExpression reverseGetPersonExpr = DoelExpression
                    .parse(getGroupPersonsDoel);
            result = doelResolver.evaluate(reverseGetPersonExpr,
                    contextObjectid);

        }
        return result;
    }

    /**
     * Создает обратное Doel выражение.
     * 
     * @param objectId
     *            объект, относительно которого вычисляется переданное прямое
     *            выражение
     * @param bindContextExpr
     *            прямое Doel выражение.
     * @return обратное Doel выражение
     */
    private DoelExpression createReverseExpression(Id objectId,
            DoelExpression bindContextExpr) {
        String domainObjectTypeName = domainObjectTypeIdCache
                .getName(((RdbmsId) objectId).getTypeId());
        DoelExpression reverseBindContextExpr = doelResolver
                .createReverseExpression(bindContextExpr, domainObjectTypeName);
        return reverseBindContextExpr;
    }

    private String createRetrieveGroupPersonsDoel(
            DoelExpression reverseBindContextExpr, String getPersonDoel) {
        String getGroupPersonsDoel = null;
        if (getPersonDoel != null) {
            getGroupPersonsDoel = reverseBindContextExpr.toString() + "."
                    + getPersonDoel;

        } else {
            getGroupPersonsDoel = reverseBindContextExpr.toString() + "."
                    + "id";
        }
        return getGroupPersonsDoel;
    }

    /**
     * Возвращает динамические группы для изменяемого объекта, которые нужно
     * пересчитывать. Поиск динамических группп выполняется по типу и статусу
     * отслеживаемого объекта, а также по измененным полям.
     * 
     * @param objectId
     *            изменяемый доменный объект
     * @param status
     *            статус изменяемого доменног объекта
     * @param modifiedFieldNames
     *            списке измененных полей доменного объекта
     * @return список конфигураций динамических групп
     */
    private List<DynamicGroupConfig> getDynamicGroupsToRecalculateForUpdate(
            Id objectId, String status,
            List<FieldModification> modifiedFieldNames) {
        String objectTypeName = domainObjectTypeIdCache
                .getName(((RdbmsId) objectId).getTypeId());
        List<DynamicGroupConfig> dynamicGroups = configurationExplorer
                .getDynamicGroupConfigsByTrackDO(
                        domainObjectTypeIdCache.getName(objectId), status);

        Set<DynamicGroupConfig> filteredDynamicGroups = new HashSet<DynamicGroupConfig>();

        for (DynamicGroupConfig dynamicGroup : dynamicGroups) {

            if (dynamicGroup.getMembers() != null
                    && dynamicGroup.getMembers().getTrackDomainObjects() != null) {
                TrackDomainObjectsConfig trackDomainObjectsConfig = dynamicGroup
                        .getMembers().getTrackDomainObjects();

                BindContextConfig bindContext = trackDomainObjectsConfig
                        .getBindContext();

                if (containsFieldNameInDoel(modifiedFieldNames, bindContext)) {
                    filteredDynamicGroups.add(dynamicGroup);
                }

                GetPersonConfig getPerson = trackDomainObjectsConfig
                        .getGetPerson();

                if (containsFieldNameInDoel(modifiedFieldNames, getPerson)) {
                    filteredDynamicGroups.add(dynamicGroup);
                }
            }
        }

        return new ArrayList<DynamicGroupConfig>(filteredDynamicGroups);
    }

    /**
     * Возвращает динамические группы для изменяемого объекта, которые нужно
     * пересчитывать. Поиск динамических группп выполняется по типу и статусу
     * отслеживаемого объекта
     * 
     * @param objectId
     *            изменяемый доменный объект
     * @param status
     *            статус изменяемого доменног объекта
     * @return список конфигураций динамических групп
     */
    private List<DynamicGroupConfig> getDynamicGroupsToRecalculate(Id objectId,
            String status) {
        List<DynamicGroupConfig> dynamicGroups = configurationExplorer
                .getDynamicGroupConfigsByTrackDO(
                        domainObjectTypeIdCache.getName(objectId), status);
        return dynamicGroups;
    }

    /**
     * Проверяет, содержит ли Doel выражение в первом элементе название поля,
     * которое было указано в списке переданных измененных полей.
     * 
     * @param modifiedFieldNames
     *            списке измененных полей.
     * @param doelAware
     *            Doel выражение
     * @return true, если содержит, иначе false
     */
    private boolean containsFieldNameInDoel(
            List<FieldModification> modifiedFieldNames, DoelAware doelAware) {
        if (doelAware != null && doelAware.getDoel() != null) {
            String bindContextDoel = doelAware.getDoel();

            DoelExpression expr = DoelExpression.parse(bindContextDoel);

            if (expr.getElements().length > 0) {
                Element firstDoelElement = expr.getElements()[0];
                if (firstDoelElement.getClass().equals(
                        DoelExpression.Field.class)) {
                    String firstDoelElementName = ((DoelExpression.Field) firstDoelElement)
                            .getName();
                    if (containsModifiedField(modifiedFieldNames,
                            firstDoelElementName)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Проверяет содержится ли поле в списке измененных полей
     * 
     * @param modifiedFieldNames
     * @param fieldName
     * @return
     */
    private boolean containsModifiedField(
            List<FieldModification> modifiedFieldNames, String fieldName) {
        for (FieldModification fieldModification : modifiedFieldNames) {
            if (fieldModification.getName().equals(fieldName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Возвращает контекстный объект для динамической группы и отслеживаемого
     * (изменяемого) доменного объекта
     * 
     * @param dynamicGroupConfig
     *            конфигурация динамической группы
     * @param objectId
     *            идентификатор отслеживаемого доменного объекта
     * @return идентификатор контекстного объекта
     */
    private Id getContextObjectId(DynamicGroupConfig dynamicGroupConfig,
            Id objectId) {
        TrackDomainObjectsConfig trackDomainObjects = dynamicGroupConfig
                .getMembers().getTrackDomainObjects();
        Id contextObjectid = null;

        if (trackDomainObjects != null
                && trackDomainObjects.getBindContext() != null) {
            String bindContextDoel = trackDomainObjects.getBindContext()
                    .getDoel();
            DoelExpression expr = DoelExpression.parse(bindContextDoel);
            List<Value> result = doelResolver.evaluate(expr, objectId);
            contextObjectid = convertToId(result);

        } else {
            contextObjectid = objectId;
        }

        return contextObjectid;
    }

    /**
     * Конвертирует результат вычисления doel выражения поиска контекстного
     * объекта в идентификатор контекстного объекта.
     * 
     * @param result
     *            вычисления doel выражения поиска контекстного объекта
     * @return идентификатор контекстного объекта
     */
    private Id convertToId(List<Value> result) {
        Id contextObjectId = null;
        if (result != null && result.size() > 0) {
            Value value = ((List<Value>) result).get(0);

            if (value.getClass().equals(ReferenceValue.class)) {
                contextObjectId = ((ReferenceValue) value).get();
            } else {
                throw new ConfigurationException(
                        "Doel expression in bind context should result in ReferenceValue");
            }
        }
        return contextObjectId;
    }

    /**
     * Пересчитывает список персон динамической группы.
     * 
     * @param dynamicGroupId
     *            идентификатор динамической группы
     * @param personIds
     *            список персон
     */
    private void refreshGroupMembers(Id dynamicGroupId, List<Id> personIds) {
        cleanGroupMembers(dynamicGroupId);

        insertGroupMembers(dynamicGroupId, personIds);
    }

    // TODO Optimize performance
    private void insertGroupMembers(Id dynamicGroupId, List<Id> personIds) {
        List<DomainObject> groupMembers = new ArrayList<DomainObject>();
        for (Id personValue : personIds) {
            if (personValue.getClass().equals(ReferenceValue.class)) {
                GenericDomainObject groupMemeber = new GenericDomainObject();
                groupMemeber.setTypeName(GROUP_MEMBER_DOMAIN_OBJECT);
                groupMemeber.setReference("UserGroup", dynamicGroupId);

                groupMemeber.setReference("person_id", personValue);
                groupMembers.add(groupMemeber);
            }

        }
        List<DomainObject> savedGroupMembers = domainObjectDao
                .save(groupMembers);
    }

    private void cleanGroupMembers(Id dynamicGroupId) {
        String query = generateDeleteGroupMembersQuery();

        Map<String, Object> parameters = initializeDeleteGroupMembersParameters(dynamicGroupId);
        int count = jdbcTemplate.update(query, parameters);
    }

    private String generateDeleteGroupMembersQuery() {
        String tableName = getSqlName(GROUP_MEMBER_DOMAIN_OBJECT);
        StringBuilder query = new StringBuilder();
        query.append("delete from ");
        query.append(tableName);
        query.append(" where usergroup=:usergroup");

        return query.toString();

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
        Id userGroupId = getUserGroupByGroupNameAndObjectId(dynamicGroupName,
                ((RdbmsId) contextObjectId).getId());

        if (userGroupId == null) {
            userGroupId = createUserGroup(dynamicGroupName, contextObjectId);
        }
        return userGroupId;
    }

    @Override
    public void notifyDomainObjectDeleted(DomainObject domainObject) {
        List<DynamicGroupCollector> typeCollectors = collectors.get(domainObject.getTypeName());
        // Формируем мапу динамических грпп, требующих пересчета и их
        // коллекторов, исключая дублирование
        Map<Id, DynamicGroupCollector> invalidGroups = new Hashtable<Id, DynamicGroupCollector>();
        for (DynamicGroupCollector dynamicGroupCollector : typeCollectors) {

            List<Id> collectorInvalidGroups = dynamicGroupCollector
                    .getInvalidDynamicGroups(domainObject,
                            getDeletedModificationList(domainObject));
            // Добавляем в массив групп, исключая дублирование
            for (Id id : collectorInvalidGroups) {
                invalidGroups.put(id, dynamicGroupCollector);
            }
        }

        // Непосредственно формирование состава, должно выхыватся в конце
        // транзакции
        // TODO надо перенести на конец транзакции
        for (Id droupId : invalidGroups.keySet()) {
            DynamicGroupCollector collector = invalidGroups.get(droupId);
            // Получаем группу
            AccessToken accessToken = accessControlService
                    .createSystemAccessToken(this.getClass().getName());
            DomainObject dynGroup = domainObjectDao.find(droupId, accessToken);

            // Выполняю пересчет
            List<Id> groupMembres = collector.getPersons(dynGroup
                    .getReference("object_id"));
            refreshGroupMembers(droupId, groupMembres);
        }

        /*
         * String status = getStatusFor(domainObject.getId());
         * 
         * List<DynamicGroupConfig> dynamicGroups =
         * getDynamicGroupsToRecalculate( domainObject.getId(), status); for
         * (DynamicGroupConfig dynamicGroupConfig : dynamicGroups) { Id
         * contextObjectId = getContextObjectId(dynamicGroupConfig,
         * domainObject.getId()); Id userGroupId =
         * getUserGroupByGroupNameAndObjectId( dynamicGroupConfig.getName(),
         * ((RdbmsId) contextObjectId).getId()); cleanGroupMembers(userGroupId);
         * 
         * Id dynamicGroupId = deleteUserGroupByGroupNameAndObjectId(
         * userGroupId, dynamicGroupConfig.getName(), ((RdbmsId)
         * contextObjectId).getId()); }
         */

    }

    /**
     * Метод вызывается после загрузки конфигурации. Пробегается по
     * конфигурации, собирает информацию о динамических группах, настраиваемых с
     * помощью классов и создает экземпляры этих классов, добавляет их в реестр
     */
    @Override
    public void onLoad() {
        try {
            // Поиск конфигураций динамических групп
            for (TopLevelConfig topConfig : configurationExplorer
                    .getConfiguration().getConfigurationList()) {

                if (topConfig instanceof DynamicGroupConfig) {
                    DynamicGroupConfig config = (DynamicGroupConfig) topConfig;
                    DynamicGroupCollector collector = null;
                    // Если динамическая группа настраивается классом
                    // коллектором,
                    // то создаем его экземпляр, и добавляем в реестр
                    if (config.getMembers() != null
                            && config.getMembers().getCollector() != null) {
                        Class<?> collectorClass = Class.forName(config
                                .getMembers().getCollector().getClassName());
                        collector = (DynamicGroupCollector) applicationContext
                                .getAutowireCapableBeanFactory()
                                .createBean(
                                        collectorClass,
                                        AutowireCapableBeanFactory.AUTOWIRE_BY_NAME,
                                        false);
                    } else {
                        // Специфичный коллектор не указан используем коллектор
                        // по умолчанию
                        collector = (DynamicGroupCollector) applicationContext
                                .getAutowireCapableBeanFactory()
                                .createBean(
                                        TrackDomainObjectCollector.class,
                                        AutowireCapableBeanFactory.AUTOWIRE_BY_NAME,
                                        false);

                    }
                    collector.init(config);

                    // Получение типов, которые отслеживает коллектор
                    List<String> types = collector.getTrackTypeNames();
                    // Регистрируем коллектор в реестре, для обработки
                    // только определенных типов
                    if (types != null) {
                        for (String type : types) {
                            registerCollector(type, collector);
                            // Ищем всех наследников и так же регистрируем
                            // их в
                            // реестре с данным коллектором
                            List<String> subTypes = getSubTypes(type);
                            for (String subtype : subTypes) {
                                registerCollector(subtype, collector);
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
     * Регистрация коллектора в реестре колекторов
     * 
     * @param type
     * @param collector
     */
    private void registerCollector(String type, DynamicGroupCollector collector) {
        List<DynamicGroupCollector> typeCollectors = collectors.get(type);
        if (typeCollectors == null) {
            typeCollectors = new ArrayList<DynamicGroupCollector>();
            collectors.put(type, typeCollectors);
        }
        typeCollectors.add(collector);
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

}
