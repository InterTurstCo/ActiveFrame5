package ru.intertrust.cm.core.dao.impl.access;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.FieldModification;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.RdbmsId;
import ru.intertrust.cm.core.config.model.CollectorSettings;
import ru.intertrust.cm.core.config.model.ContextRoleConfig;
import ru.intertrust.cm.core.config.model.DynamicGroupConfig;
import ru.intertrust.cm.core.config.model.TrackDomainObjectsConfig;
import ru.intertrust.cm.core.dao.access.AccessType;
import ru.intertrust.cm.core.dao.access.ContextRoleCollector;

public class ContextRoleTrackDomainObjectCollector extends BaseDynamicGroupServiceImpl implements ContextRoleCollector {

    private ContextRoleConfig config;
    private TrackDomainObjectsConfig trackDomainObjects;

    @Override
    public List<Id> getMembers(Id domainObjectId, Id contextId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> getTrackTypeNames() {
        List<String> result = new ArrayList<String>();
        // Не указан TrackDomainObjects, следим только за текущим типом
        if (trackDomainObjects == null) {
            result.add(config.getContext().getDomainObject().getType());
        } else {
            result.add(trackDomainObjects.getType());
        }
        return result;
    }

    @Override
    public List<Id> getInvalidContexts(DomainObject domainObject, List<FieldModification> modifiedFields) {
        String status = getStatusFor(domainObject.getId());

        List<Id> result = new ArrayList<Id>();

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
