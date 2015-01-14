package ru.intertrust.cm.core.gui.model.action;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.action.ActionConfig;

/**
 * @author Lesia Puhova
 *         Date: 23.12.2014
 *         Time: 16:58
 */
public class DownloadAttachmentActionContext  extends ActionContext {

    private Id id;
    private String tempName;

    public DownloadAttachmentActionContext() {
        super();
    }

    public DownloadAttachmentActionContext(ActionConfig actionConfig) {
        super(actionConfig);
    }

    public Id getId() {
        return id;
    }

    public void setId(Id id) {
        this.id = id;
    }

    public String getTempName() {
        return tempName;
    }

    public void setTempName(String tempName) {
        this.tempName = tempName;
    }
}
