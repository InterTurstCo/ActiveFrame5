package ru.intertrust.cm.core.gui.api.server.plugin;

import ru.intertrust.cm.core.business.api.dto.SortOrder;
import ru.intertrust.cm.core.config.gui.collection.view.CollectionDisplayConfig;
import ru.intertrust.cm.core.config.gui.form.widget.linkediting.SelectionSortCriteriaConfig;
import ru.intertrust.cm.core.config.gui.navigation.DefaultSortCriteriaConfig;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 01.02.2015
 *         Time: 15:47
 */
public interface SortOrderHelper {
    SortOrder buildSortOrder(String collectionName, DefaultSortCriteriaConfig defaultSortCriteriaConfig,
                             CollectionDisplayConfig collectionDisplayConfig);

    SortOrder buildSortOrder(String collectionName, DefaultSortCriteriaConfig defaultSortCriteriaConfig);

    SortOrder buildSortOrder(String collectionName, SelectionSortCriteriaConfig sortCriteriaConfig);

    SortOrder buildSortOrderByIdField(String collectionName);
}
