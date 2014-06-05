package ru.intertrust.cm.core.gui.model.action;

import ru.intertrust.cm.core.config.gui.ActionConfig;
import ru.intertrust.cm.core.gui.model.form.widget.AttachmentItem;

import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 03.06.14
 *         Time: 17:15
 */
public class DeployConfigurationActionContext extends ActionContext {
    private List<AttachmentItem> attachmentItems;
    public DeployConfigurationActionContext() {
    }

    public DeployConfigurationActionContext(ActionConfig actionConfig) {
        super(actionConfig);
    }

    public List<AttachmentItem> getAttachmentItems() {
        return attachmentItems;
    }

    public void setAttachmentItems(List<AttachmentItem> attachmentItems) {
        this.attachmentItems = attachmentItems;
    }
}
