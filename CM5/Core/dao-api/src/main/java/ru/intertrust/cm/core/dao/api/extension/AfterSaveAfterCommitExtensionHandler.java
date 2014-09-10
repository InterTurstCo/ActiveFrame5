package ru.intertrust.cm.core.dao.api.extension;

import java.util.List;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.FieldModification;

/**
 * Интерфейс обработчика точки расширения, 
 * вызывающийся после коммита транзакции, 
 * в случае если во время данной транзакции был изменен доменный объект. Если в течение транзакции 
 * доменный объект сохранялся несколько раз то обработчик вызовется только один раз и в параметре changedFields
 * будет список всех измененных полей во всех операциях save, причем в случае если один и тот же атрибут менялся несколько раз то 
 * в объекте FieldModification будет самое первое и самое последнее данного поля
 * @author larin
 *
 */
public interface AfterSaveAfterCommitExtensionHandler extends ExtensionPointHandler{
    
    /**
     * Входная точка обработчика точек расширения
     * @param domainObject
     * @param changedFields
     */
    void onAfterSave(DomainObject domainObject, List<FieldModification> changedFields);
}
