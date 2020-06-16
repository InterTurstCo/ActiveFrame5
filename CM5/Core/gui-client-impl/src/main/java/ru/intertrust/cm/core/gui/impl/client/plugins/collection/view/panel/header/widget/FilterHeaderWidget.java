package ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.panel.header.widget;

import ru.intertrust.cm.core.gui.impl.client.plugins.collection.CollectionColumn;
import ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants;
import ru.intertrust.cm.core.gui.impl.client.util.HeaderWidgetUtil;
import ru.intertrust.cm.core.gui.model.CollectionColumnProperties;

import java.util.List;

import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.*;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 22.06.2014
 *         Time: 23:26
 */
public abstract class FilterHeaderWidget extends HeaderWidget {
    protected String id;
    protected boolean showFilter;
    protected String fieldName;
    protected String filterValuesRepresentation;
    protected int filterWidth;

    protected FilterHeaderWidget(CollectionColumn column, CollectionColumnProperties columnProperties,
                                 List<String> initialFilterValues, String valueSeparator) {
        title = column.getDataStoreName();
        int userColumnWidth = column.getUserWidth();
        this.filterWidth = userColumnWidth == 0
                ? column.getMinWidth() - BusinessUniverseConstants.FILTER_CONTAINER_MARGIN
                : userColumnWidth - BusinessUniverseConstants.FILTER_CONTAINER_MARGIN;
        id = (title + System.currentTimeMillis() + (int)(Math.random() * 50 + 1)).replaceAll(" ", "");
        fieldName = (String) columnProperties.getProperty(CollectionColumnProperties.FIELD_NAME);
        this.filterValuesRepresentation = HeaderWidgetUtil.initFilterValuesRepresentation(valueSeparator, initialFilterValues);
    }

    @Override
    public boolean hasFilter() {
        return true;
    }

    public String getId() {
        return id;
    }

    public String getHtml() {
        return html;
    }

    @Override
    public void setFilterInputWidth(int filterWidth) {
        this.filterWidth = filterWidth;
    }

    public boolean isShowFilter() {
        return showFilter;
    }

    public void setShowFilter(boolean showFilter) {
        this.showFilter = showFilter;
    }

    public void setFilterValuesRepresentation(String filterValuesRepresentation) {
        this.filterValuesRepresentation = filterValuesRepresentation;
    }

    public String getFilterValuesRepresentation() {
        return filterValuesRepresentation;
    }

    public String getFieldName() {
        return fieldName;
    }

    protected String getSearchContainerStyle() {
        String styleDisplayingFilter = showFilter ? "style=\"" : " style=\"display:none; ";
        StringBuilder styleForSearchContainerBuilder = new StringBuilder(styleDisplayingFilter);
        styleForSearchContainerBuilder.append(" width:");
        int searchContainerWidth = filterWidth - RESIZE_HANDLE_WIDTH - MOVE_HANDLE_WIDTH;
        styleForSearchContainerBuilder.append(searchContainerWidth);
        styleForSearchContainerBuilder.append("px;\" ");
        return styleForSearchContainerBuilder.toString();

    }

    protected String getSearchInputStyle() {
        StringBuilder styleForSearchInputBuilder = new StringBuilder("style=\"");
        int searchInputWidth = filterWidth - RESIZE_HANDLE_WIDTH - MOVE_HANDLE_WIDTH - CLEAR_BUTTON_WIDTH - SEARCH_INPUT_OFFSET;
        if (searchInputWidth < 10) {
            styleForSearchInputBuilder.append("display:none;\"");
        } else {
            styleForSearchInputBuilder.append("width:");
            styleForSearchInputBuilder.append(searchInputWidth);
            styleForSearchInputBuilder.append("px;\"/>");

        }
        return styleForSearchInputBuilder.toString();
    }
}
