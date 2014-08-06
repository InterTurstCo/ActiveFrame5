package ru.intertrust.cm.core.gui.model.action;

import ru.intertrust.cm.core.gui.model.form.widget.CollectionRowsResponse;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 06.08.2014
 *         Time: 0:24
 */
public class RefreshActionData extends ActionData {
    private CollectionRowsResponse response;

    public RefreshActionData() {
    }

    public RefreshActionData(CollectionRowsResponse response) {
        this.response = response;
    }

    public CollectionRowsResponse getResponse() {
        return response;
    }

    public void setResponse(CollectionRowsResponse response) {
        this.response = response;
    }
}
