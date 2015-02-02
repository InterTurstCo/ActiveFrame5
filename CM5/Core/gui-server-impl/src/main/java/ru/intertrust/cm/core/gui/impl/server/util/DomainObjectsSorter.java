package ru.intertrust.cm.core.gui.impl.server.util;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.SortOrder;
import ru.intertrust.cm.core.business.api.util.ModelUtil;
import ru.intertrust.cm.core.config.gui.form.widget.linkediting.SelectionSortCriteriaConfig;

import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 05.10.2014
 *         Time: 9:03
 */
public class DomainObjectsSorter {
    public static void sort(SelectionSortCriteriaConfig selectionSortCriteriaConfig, List<DomainObject> domainObjects) {
        SortOrder sortOrder = SortOrderBuilder.getSelectionSortOrder(selectionSortCriteriaConfig);
        if (sortOrder == null) {
           sortOrder = SortOrderBuilder.getSortOderByDefaultField();
        }
        ModelUtil.sort(domainObjects, sortOrder);
    }
}
