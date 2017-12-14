package ru.intertrust.cm.core.gui.impl.client.plugins.reportupload;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.config.gui.form.widget.WidgetDisplayConfig;
import ru.intertrust.cm.core.config.localization.LocalizationKeys;
import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.api.client.LocalizeUtil;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginView;
import ru.intertrust.cm.core.gui.impl.client.form.widget.attachmentbox.AttachmentBoxWidget;
import ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants;
import ru.intertrust.cm.core.gui.model.form.widget.AttachmentBoxState;
import ru.intertrust.cm.core.gui.model.form.widget.AttachmentItem;

import java.util.List;

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
        mainPanel.add(new Label(LocalizeUtil.get(LocalizationKeys.ADD_REPORT_FILES_KEY,
                BusinessUniverseConstants.ADD_REPORT_FILES)));
        attachmentBox = createAttachmentBox();
        mainPanel.add(attachmentBox);
        Application.getInstance().unlockScreen();
    }

    @Override
    public IsWidget getViewWidget() {
        return mainPanel;
    }

    protected List<AttachmentItem> getAttachmentItems() {
        AttachmentBoxState state = (AttachmentBoxState)attachmentBox.getCurrentState();

        return state.getAttachments();
    }

    public void clear() {
        mainPanel.remove(attachmentBox);
        attachmentBox = createAttachmentBox();
        mainPanel.add(attachmentBox);
    }

    private AttachmentBoxWidget createAttachmentBox() {
        AttachmentBoxWidget attachmentBox = ComponentRegistry.instance.get("attachment-box");
        WidgetDisplayConfig displayConfig = new WidgetDisplayConfig();
        AttachmentBoxState state = new AttachmentBoxState();
        attachmentBox.setDisplayConfig(displayConfig);
        EventBus eventBus = GWT.create(SimpleEventBus.class);
        attachmentBox.setEventBus(eventBus);
        attachmentBox.setState(state);
        attachmentBox.asWidget().setStyleName("upload-report-template");

        return attachmentBox;
    }
}
