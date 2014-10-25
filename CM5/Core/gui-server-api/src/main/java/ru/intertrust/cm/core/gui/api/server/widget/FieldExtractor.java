package ru.intertrust.cm.core.gui.api.server.widget;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.gui.api.server.ComponentHandler;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 21.10.2014
 *         Time: 6:22
 */
public interface FieldExtractor extends ComponentHandler{
    Value extractField(DomainObject domainObject, String fieldPath);
}
