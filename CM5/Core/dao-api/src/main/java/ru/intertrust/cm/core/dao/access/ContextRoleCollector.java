package ru.intertrust.cm.core.dao.access;

import java.util.List;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.FieldModification;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.model.CollectorSettings;
import ru.intertrust.cm.core.config.model.ContextRoleConfig;

/**
 * Интерфейс класса коллектора для вычисления контекстных ролей
 * @author larin
 * 
 */
public interface ContextRoleCollector {

    /**
     * Возвращает состав контекстной роли
     * @param domainObjectId
     *            изменившийса доменный объект
     * @param contextId
     *            идентификатор контекста для контекстной роли
     * @return возвращает список групп, которые входят в состав контектной роли
     */
    List<Id> getMembers(Id domainObjectId, Id contextId);

    /**
     * Возвращает список типов, которые могут повлиять на состав контекстной
     * роли
     * @return
     */
    List<String> getTrackTypeNames();

    /**
     * Возвращает список невалидных контектов для контекстной роли. вызывается
     * при изменение доменных объектов типы которых были возвращены в методе
     * getTrackTypeNames
     * @param domainObject
     *            изменившийся доменный объект
     * @param modifiedFields
     *            изменившиеся поля
     * @return
     */
    List<Id> getInvalidContexts(DomainObject domainObject, List<FieldModification> modifiedFields);

    /**
     * Инициализация коллектора.
     * @param config
     */
    void init(ContextRoleConfig config, CollectorSettings collectorSettings);
}
