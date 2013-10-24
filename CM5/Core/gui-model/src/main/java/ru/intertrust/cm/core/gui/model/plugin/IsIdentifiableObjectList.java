package ru.intertrust.cm.core.gui.model.plugin;

import ru.intertrust.cm.core.business.api.dto.Id;

import java.util.List;

/**
 * @author Denis Mitavskiy
 *         Date: 24.10.13
 *         Time: 13:53
 */
public interface IsIdentifiableObjectList {
    List<Id> getSelectedIds();
}
