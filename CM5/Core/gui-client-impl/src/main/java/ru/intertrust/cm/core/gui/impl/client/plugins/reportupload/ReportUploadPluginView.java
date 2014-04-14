package ru.intertrust.cm.core.gui.impl.client.plugins.reportupload;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.VerticalPanel;
import ru.intertrust.cm.core.config.gui.form.widget.WidgetDisplayConfig;
import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginView;
import ru.intertrust.cm.core.gui.impl.client.form.widget.AttachmentBoxWidget;
import ru.intertrust.cm.core.gui.model.form.widget.AttachmentBoxState;

/**
 * @author Lesia Puhova
 *         Date: 14.04.14
 *         Time: 12:40
 */
public class ReportUploadPluginView  extends PluginView {

    private Panel mainPanel = new VerticalPanel();
    private AttachmentBoxWidget attachmentBox;

    public ReportUploadPluginView(Plugin plugin){
       super(plugin);
        init();
    }

    private void init() {
        mainPanel.add(new Label("Upload report templates"));

        attachmentBox = ComponentRegistry.instance.get("attachment-box");
        WidgetDisplayConfig displayConfig = new WidgetDisplayConfig();
        AttachmentBoxState state = new AttachmentBoxState();
        attachmentBox.setDisplayConfig(displayConfig);
        attachmentBox.setState(state);

        mainPanel.add(attachmentBox);
    }

    @Override
    public IsWidget getViewWidget() {
        Application.getInstance().hideLoadingIndicator();
        return mainPanel;
    }

    public AttachmentBoxWidget getAttachmentBox() {
        return attachmentBox;
    }
}
