package ru.intertrust.cm.core.config.gui.form.widget;

import ru.intertrust.cm.core.config.gui.form.widget.filter.SelectionFiltersConfig;
import ru.intertrust.cm.core.config.gui.navigation.CollectionRefConfig;
import ru.intertrust.cm.core.config.gui.navigation.DefaultSortCriteriaConfig;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 06.07.2014
 *         Time: 21:34
 */
public abstract class LinkEditingWidgetConfig extends WidgetConfig {
    public abstract SelectionStyleConfig getSelectionStyleConfig();
    public abstract SelectionFiltersConfig getSelectionFiltersConfig();
    public abstract CollectionRefConfig getCollectionRefConfig();
    public abstract FormattingConfig getFormattingConfig();
    public abstract DefaultSortCriteriaConfig getDefaultSortCriteriaConfig();
    public abstract SelectionPatternConfig getSelectionPatternConfig();

}
