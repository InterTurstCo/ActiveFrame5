package ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.panel.header;

import java.util.List;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 21/03/14
 *         Time: 12:05 PM
 */
public class CollectionColumnHeaderController {
    private List<CollectionColumnHeader> headers;

    public List<CollectionColumnHeader> getHeaders() {
        return headers;
    }

    public void setHeaders(List<CollectionColumnHeader> headers) {
        this.headers = headers;
    }

    public void changeFiltersInputsVisibility(boolean showFilter) {
        for (CollectionColumnHeader header : headers) {
            header.setSearchAreaVisibility(showFilter);

        }

    }
    public void clearFilters() {
        for (CollectionColumnHeader header : headers) {
        header.hideClearButton();
        header.resetFilterValue();
        }
    }

    public void updateFilterValues() {
        for (CollectionColumnHeader header : headers) {

            header.updateFilterValue();
        }
    }

    public void saveFilterValues() {
        for (CollectionColumnHeader header : headers) {
            header.saveFilterValue();
        }
    }

    public void setFocus() {
        for (CollectionColumnHeader header : headers) {
            header.setFocus();
        }
    }

}
