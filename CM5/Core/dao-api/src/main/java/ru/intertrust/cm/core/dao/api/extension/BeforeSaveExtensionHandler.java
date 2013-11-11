package ru.intertrust.cm.core.dao.api.extension;

import java.util.List;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.FieldModification;

/**
 * Интерфейс точки расширения, вызывающейся до сохранения доменного объекта в
 * хранилище. В данной точке расширения допускается изменять переданный доменный
 * объект, все эти изменения сохранятся
 * 
 * @author larin
 * 
 */
public interface BeforeSaveExtensionHandler extends ExtensionPointHandler {

    /**
     * Входная точка обработчика точки расширения.
     * 
     * @param domainObject
     */
    void onBeforeSave(DomainObject domainObject, List<FieldModification> changedFields);
}
