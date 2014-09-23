package ru.intertrust.cm.core.gui.model.action;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.action.AbstractActionConfig;
import ru.intertrust.cm.core.gui.model.plugin.CollectionRowsRequest;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 06.08.2014
 *         Time: 0:06
 */
public class RefreshActionContext extends ActionContext {
    private CollectionRowsRequest request;
    private Id id;

    public RefreshActionContext() {
    }

    public RefreshActionContext(AbstractActionConfig actionConfig) {
        super(actionConfig);
    }

    public Id getId() {
        return id;
    }

    public void setId(Id id) {
        this.id = id;
    }

    public CollectionRowsRequest getRequest() {
        return request;
    }

    public void setRequest(CollectionRowsRequest request) {
        this.request = request;
    }
}
