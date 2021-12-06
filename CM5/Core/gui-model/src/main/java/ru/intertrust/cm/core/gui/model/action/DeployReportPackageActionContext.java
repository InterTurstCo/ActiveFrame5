package ru.intertrust.cm.core.gui.model.action;

import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.gui.model.form.widget.AttachmentItem;

public class DeployReportPackageActionContext extends ActionContext {

    private AttachmentItem attachmentItem;

    public DeployReportPackageActionContext() {
    }

    public DeployReportPackageActionContext(ActionConfig actionConfig) {
        super(actionConfig);
    }

    public AttachmentItem getAttachmentItem() {
        return attachmentItem;
    }

    public void setAttachmentItem(AttachmentItem attachmentItem) {
        this.attachmentItem = attachmentItem;
    }
}
