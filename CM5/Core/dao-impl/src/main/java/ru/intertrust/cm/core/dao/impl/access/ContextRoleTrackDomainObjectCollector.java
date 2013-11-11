package ru.intertrust.cm.core.dao.impl.access;

import java.util.ArrayList;
import java.util.List;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.FieldModification;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.RdbmsId;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.config.model.CollectorSettings;
import ru.intertrust.cm.core.config.model.ContextRoleConfig;
import ru.intertrust.cm.core.config.model.TrackDomainObjectsConfig;
import ru.intertrust.cm.core.config.model.doel.DoelExpression;
import ru.intertrust.cm.core.dao.access.ContextRoleCollector;

public class ContextRoleTrackDomainObjectCollector extends BaseDynamicGroupServiceImpl implements ContextRoleCollector {

    private ContextRoleConfig config;
    private TrackDomainObjectsConfig trackDomainObjects;

    @Override
    public List<Id> getMembers(Id domainObjectId, Id contextId) {
        //Получаем доменные объекты, которые являются контекстами для динамических групп
        List<Id> contextGroupOwner = new ArrayList<Id>();
        if (trackDomainObjects.getGetGroup().getDoel() != null) {
            DoelExpression expr = DoelExpression.parse(trackDomainObjects.getGetGroup().getDoel());
            List<Value> contextIds = doelResolver.evaluate(expr, contextId);
            contextGroupOwner.addAll(getIdList(contextIds));
        } else {
            contextGroupOwner.add(contextId);
        }

        List<Id> result = new ArrayList<Id>();
        //Проверяем указана ли имя динамической группы. Если указана то берем динамическую группу с контекстом доменного объекта и именем из конфигурации
        if (trackDomainObjects.getGetGroup().getGroupName() != null) {
            for (Id id : contextGroupOwner) {
                result.add(getUserGroupByGroupNameAndObjectId(trackDomainObjects.getGetGroup().getGroupName(), ((RdbmsId) id).getId()));
            }
        } else {
            //имя не указано, полученные доменные объекты и ест контекстные группы
            result = contextGroupOwner;
        }
        return result;
    }

    @Override
    public List<String> getTrackTypeNames() {
        List<String> result = new ArrayList<String>();
        // Не указан TrackDomainObjects, следим только за текущим типом
        if (trackDomainObjects != null && trackDomainObjects.getType() != null) {
            result.add(trackDomainObjects.getType());
        } else {
            result.add(config.getContext().getDomainObject().getType());
        }
        return result;
    }

    @Override
    public List<Id> getInvalidContexts(DomainObject domainObject, List<FieldModification> modifiedFields) {
        List<Id> result = new ArrayList<Id>();
        //Получение всех контекстов, которые зависят от текущего измененного обьекта
        if (trackDomainObjects.getBindContext() != null && trackDomainObjects.getBindContext().getDoel() != null) {
            //Указан doel на зависимые объекты
            DoelExpression expr = DoelExpression.parse(trackDomainObjects.getBindContext().getDoel());
            List<Value> contextIds = doelResolver.evaluate(expr, domainObject.getId());
            result.addAll(getIdList(contextIds));

        } else {
            //зависимые объекты не указаны, значит контекстом является текущий объект
            //поверяем его статус
            if (trackDomainObjects.getStatus() != null && trackDomainObjects.getStatus().length() > 0) {
                String status = getStatusFor(domainObject.getId());
                if (trackDomainObjects.getStatus().equals(status)) {
                    result.add(domainObject.getId());
                }
            } else {
                result.add(domainObject.getId());
            }
        }
        return result;
    }

    @Override
    public void init(ContextRoleConfig config, CollectorSettings collectorSettings) {
        this.config = config;
        this.trackDomainObjects = (TrackDomainObjectsConfig) collectorSettings;
    }

    /**
     * Пересчитывает список доступа для динамичсекой группы для переданного доменного объекта.
     * @param objectId
     *            идентификатор доменного объекта, для которого пересчитывается список доступа
     * @param roleGroupConfig
     *            конфигурация динамической группы
     * @param accessType
     *            тип доступа для динамичской группы
     */
    /*private void processAclForDynamicGroup(Id objectId, Object roleGroupConfig, AccessType accessType) {
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
                // если путь к контекстному объекту не указан внутри тега group,
                // то контекстным объектом является
                // текущий объект
                Long contextObjectId = ((RdbmsId) objectId).getId();
                processAclForDynamicGroupWithContext(objectId, accessType, dynamicGroupName, contextObjectId);
            }

        } else {
            processAclForDynamicGroupWithoutContext(objectId, accessType, dynamicGroupName);

        }
    }*/
}
