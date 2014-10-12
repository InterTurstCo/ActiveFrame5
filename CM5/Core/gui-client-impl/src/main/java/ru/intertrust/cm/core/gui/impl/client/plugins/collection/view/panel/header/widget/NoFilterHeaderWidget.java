package ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.panel.header.widget;

import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 22.06.2014
 *         Time: 21:31
 */
public class NoFilterHeaderWidget extends HeaderWidget {

    public NoFilterHeaderWidget(String title) {
        this.title = title;
    }

    public void init() {
        html = getTitleHtml();
    }

    @Override
    public boolean hasFilter() {
        return false;
    }

    @Override
    public String getId() {
        return null;
    }

    @Override
    public String getFilterValuesRepresentation() {
        return null;
    }

    @Override
    public List<String> getFilterValues() {
        return null;
    }

    @Override
    public void setFilterInputWidth(int filterWidth) {

    }

    @Override
    public void setFilterValuesRepresentation(String filterValue) {

    }

    @Override
    public String getFieldName() {
        return null;
    }

    @Override
    public boolean isShowFilter() {
        return false;
    }

    @Override
    public void setShowFilter(boolean showFilter) {

    }
}
