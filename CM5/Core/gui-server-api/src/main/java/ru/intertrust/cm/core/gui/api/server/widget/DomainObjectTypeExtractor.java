package ru.intertrust.cm.core.gui.api.server.widget;

import ru.intertrust.cm.core.business.api.dto.DomainObjectTypeAndAccessValue;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.gui.api.server.ComponentHandler;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 27.10.2014
 *         Time: 21:22
 */
public interface DomainObjectTypeExtractor extends ComponentHandler {
    StringValue getType(Dto input);
    DomainObjectTypeAndAccessValue getTypeAndAccess(Dto input);
}
