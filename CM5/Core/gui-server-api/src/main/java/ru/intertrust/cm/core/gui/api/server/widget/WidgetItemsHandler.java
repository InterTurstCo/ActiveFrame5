package ru.intertrust.cm.core.gui.api.server.widget;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.config.gui.form.widget.FormattingConfig;
import ru.intertrust.cm.core.config.gui.form.widget.SelectionPatternConfig;
import ru.intertrust.cm.core.gui.api.server.ComponentHandler;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 06.07.2014
 *         Time: 13:58
 */
public interface WidgetItemsHandler extends ComponentHandler {
    public LinkedHashMap<Id, String> generateWidgetItemsFromCollection(SelectionPatternConfig selectionPatternConfig,
                                                                       FormattingConfig formattingConfig,
                                                                       IdentifiableObjectCollection collection);
    public LinkedHashMap<Id, String> generateWidgetItemsFromCollectionAndIds(SelectionPatternConfig selectionPatternConfig,
                                                                             FormattingConfig formattingConfig,
                                                                             IdentifiableObjectCollection collection,
                                                                             List<Id> selectedIds);
    Dto fetchWidgetItems(Dto inputParams);
}
