package ru.intertrust.cm.core.dao.impl.access;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.core.config.DynamicGroupConfig;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.access.UserGroupGlobalCache;
import ru.intertrust.cm.core.dao.api.*;
import ru.intertrust.cm.core.dao.impl.doel.DoelResolver;
import ru.intertrust.cm.core.dao.impl.utils.DaoUtils;

import java.util.*;

import static ru.intertrust.cm.core.dao.impl.DataStructureNamingHelper.getSqlName;

/**
 * @author atsvetkov
 */
public class BaseDynamicGroupServiceImpl {

    @Autowired
    protected DoelResolver doelResolver;

    @Autowired
    protected DomainObjectTypeIdCache domainObjectTypeIdCache;

    @Autowired
    protected DomainObjectDao domainObjectDao;

    @Autowired
    protected StatusDao statusDao;

    @Autowired
    protected AccessControlService accessControlService;

    @Autowired
    protected ConfigurationExplorer configurationExplorer;

    @Autowired
    protected PersonManagementServiceDao personManagementService;

    @Autowired
    protected CollectionsDao collectionsService;

    @Autowired
    protected CurrentUserAccessor currentUserAccessor;
    
    @Autowired
    protected UserGroupGlobalCache userGroupGlobalCache;

    public void setDoelResolver(DoelResolver doelResolver) {
        this.doelResolver = doelResolver;
        doelResolver.setDomainObjectTypeIdCache(domainObjectTypeIdCache);
    }

    public void setDomainObjectDao(DomainObjectDao domainObjectDao) {
        this.domainObjectDao = domainObjectDao;
    }

    public void setConfigurationExplorer(ConfigurationExplorer configurationExplorer) {
        this.configurationExplorer = configurationExplorer;
    }

    public void setDomainObjectTypeIdCache(DomainObjectTypeIdCache domainObjectTypeIdCache) {
        this.domainObjectTypeIdCache = domainObjectTypeIdCache;
    }


    public void setAccessControlService(AccessControlService accessControlService) {
        this.accessControlService = accessControlService;
    }

    /**
     * Возвращает идентификатор группы пользователей по имени группы и идентификатору контекстного объекта
     * @param groupName
     *            имя динамической группы
     * @param contextObjectId
     *            идентификатор контекстного объекта
     * @return идентификатор группы пользователей
     */
    protected Id getUserGroupByGroupNameAndObjectId(String groupName, Id contextObjectId) {
        Id result = null;
        DomainObject group = personManagementService.findDynamicGroup(groupName, contextObjectId);
        if (group != null){
            result = group.getId(); 
        }
        return result;
    }

    public Id getUserGroupByGroupName(String groupName) {
        AccessToken accessToken = accessControlService.createSystemAccessToken(this.getClass().getName());

        Filter filter = new Filter();
        filter.setFilter("byName");
        filter.addCriterion(0, new StringValue(groupName));

        IdentifiableObjectCollection resultCollection = collectionsService.findCollection("GroupByName",
                Collections.singletonList(filter), null, 0, 0, accessToken);

        if (resultCollection == null || resultCollection.size() == 0) {
            return null;
        }

        return resultCollection.getId(0);
    }

    /**
     * Возвращает строковое представление статуса доменного объекта
     * @param objectId
     *            идентификатор доменного объекта
     * @return статус доменного объекта
     */
    protected String getStatusFor(Id objectId) {
        final AccessToken accessToken = accessControlService.createSystemAccessToken(this.getClass().getName());
        final DomainObject object = domainObjectDao.find(objectId, accessToken);
        final Id statusId = object.getStatus();
        return statusId == null ? null : domainObjectDao.find(statusId, accessToken).getString("name");
    }

    /**
     * Возвращает строковое представление статуса доменного объекта
     * @param domainObject
     *            доменный объект
     * @return статус доменного объекта
     */
    protected String getStatusFor(DomainObject domainObject) {
        return statusDao.getStatusNameById(domainObject.getStatus());
    }

    /**
     * Получение имени типа документа
     * @param objectId
     * @return
     */
    protected String getTypeName(Id objectId) {
        return domainObjectTypeIdCache.getName(objectId);
    }

    private String generateGetStatusForQuery(Id objectId) {
        RdbmsId id = (RdbmsId) objectId;

        //Получение типа верхнего уровня
        String parentType = configurationExplorer.getDomainObjectRootType(domainObjectTypeIdCache.getName(id.getTypeId()));
        DomainObjectTypeConfig typeConfig = configurationExplorer.getConfig(DomainObjectTypeConfig.class, parentType);        

        String tableName = getSqlName(typeConfig.getName());
        StringBuilder query = new StringBuilder();
        query.append("select s.").append(DaoUtils.wrap("name")).append(" from ").append(DaoUtils.wrap(tableName)).
                append(" o inner join ").append(DaoUtils.wrap(GenericDomainObject.STATUS_DO)).append(" s on ").
                append("s.").append(DaoUtils.wrap("id")).append(" = o.").append(DaoUtils.wrap(GenericDomainObject.STATUS_FIELD_NAME));
        query.append(" where o.").append(DaoUtils.wrap("id")).append(" = {0}");

        return query.toString();
    }

    protected Id createUserGroup(String dynamicGroupName, Id contextObjectId) {
        Id userGroupId;
        GenericDomainObject userGroupDO = new GenericDomainObject();
        userGroupDO.setTypeName(GenericDomainObject.USER_GROUP_DOMAIN_OBJECT);
        userGroupDO.setString("group_name", dynamicGroupName);
        if (contextObjectId != null) {
            userGroupDO.setReference("object_id", contextObjectId);
        }
        AccessToken accessToken = accessControlService.createSystemAccessToken("BaseDynamicGroupService");
        DomainObject updatedObject = domainObjectDao.save(userGroupDO, accessToken);
        userGroupId = updatedObject.getId();
        return userGroupId;
    }

    protected List<DomainObject> createUserGroups(Id contextObjectId, List<DynamicGroupConfig> configs) {
        AccessToken accessToken = accessControlService.createSystemAccessToken("BaseDynamicGroupService");
        ArrayList<DomainObject> userGroups = new ArrayList<>(configs.size());
        for (DynamicGroupConfig dynamicGroupConfig : configs) {
            GenericDomainObject userGroupDO = new GenericDomainObject();
            userGroupDO.setTypeName(GenericDomainObject.USER_GROUP_DOMAIN_OBJECT);
            userGroupDO.setString("group_name", dynamicGroupConfig.getName());
            userGroupDO.setReference("object_id", contextObjectId);
            userGroups.add(userGroupDO);
        }
        return domainObjectDao.save(userGroups, accessToken);
    }

    protected List<FieldModification> getNewObjectModificationList(
            DomainObject domainObject) {

        final ArrayList<String> fields = domainObject.getFields();
        List<FieldModification> result = new ArrayList<>(fields.size());
        for (String fieldName : fields) {
            result.add(new FieldModificationImpl(fieldName, null, domainObject
                    .getValue(fieldName)));
        }

        return result;
    }

    protected List<FieldModification> getDeletedModificationList(
            DomainObject domainObject) {
        final ArrayList<String> fields = domainObject.getFields();
        List<FieldModification> result = new ArrayList<>(fields.size());
        for (String fieldName : fields) {
            result.add(new FieldModificationImpl(fieldName, domainObject
                    .getValue(fieldName), null));
        }

        return result;
    }

    /**
     * Преобразование списка Value в список Id
     * @param valueList
     * @return
     */
    protected List<Id> getIdList(List<Value> valueList) {
        List<Id> result = new ArrayList<>(valueList.size());
        for (Value value : valueList) {
            if (value.get() != null) {
                result.add((Id) value.get());
            }
        }
        return result;
    }

    protected List<Id> getIdListFromDomainObjectList(List<DomainObject> domainObjectList) {
        if (domainObjectList == null || domainObjectList.isEmpty()) {
            return Collections.emptyList();
        }
        List<Id> result = new ArrayList<>(domainObjectList.size());
        if (domainObjectList != null) {
            for (DomainObject value : domainObjectList) {
                result.add(value.getId());
            }
        }
        return result;
    }

    /**
     * Добавление элементов коллекции без дублирования
     * @param targetCollection
     * @param sourceCollection
     */
    protected <T> void addAllWithoutDuplicate(Set<T> targetCollection, Collection<T> sourceCollection) {
        if (sourceCollection != null && targetCollection != null) {
            targetCollection.addAll(sourceCollection);
        }
    }


}
