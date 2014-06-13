package ru.intertrust.cm.core.gui.model.action;

import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.gui.model.form.widget.AttachmentItem;

import java.util.List;

/**
 * @author Lesia Puhova
 *         Date: 14.04.14
 *         Time: 15:34
 */
public class DeployReportActionContext extends ActionContext {

    private List<AttachmentItem> attachmentItems;

    public DeployReportActionContext() {
    }

    public DeployReportActionContext(ActionConfig actionConfig) {
        super(actionConfig);
    }

    public List<AttachmentItem> getAttachmentItems() {
        return attachmentItems;
    }

    public void setAttachmentItems(List<AttachmentItem> attachmentItems) {
        this.attachmentItems = attachmentItems;
    }
}
