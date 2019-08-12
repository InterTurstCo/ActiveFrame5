package ru.intertrust.cm.core.gui.model.action;

import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.gui.model.form.widget.AttachmentItem;

import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 03.06.14
 *         Time: 17:15
 */
public class DeployConfigurationActionContext extends ToggleActionContext {
    private List<AttachmentItem> attachmentItems;
    private String configType;
    
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

    public String getConfigType() {
        return configType;
    }

    public void setConfigType(String configType) {
        this.configType = configType;
    }
}
