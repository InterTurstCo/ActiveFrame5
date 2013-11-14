package ru.intertrust.cm.core.dao.api.extension;

import java.util.List;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.FieldModification;

/**
 * Интерфейс точки расширения, вызывающийся после сохранения доменного обьекта.
 * В данной точки расширения уже нельзя изменять переданный доменный объект, так
 * как данные изменения не сохранятся в базк
 * 
 * @author larin
 * 
 */
public interface AfterSaveExtensionHandler extends ExtensionPointHandler {

    /**
     * Входной метод точки расширения
     * 
     * @param domainObject
     */
    void onAfterSave(DomainObject domainObject, List<FieldModification> changedFields);
}
