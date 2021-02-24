package ru.intertrust.cm.core.gui.impl.client.plugins.reportupload;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.config.gui.form.widget.AcceptedTypeConfig;
import ru.intertrust.cm.core.config.gui.form.widget.AcceptedTypesConfig;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReportPackageImportPluginView extends PluginView {

    private Panel mainPanel = new VerticalPanel();
    private AttachmentBoxWidget attachmentBox;

    public ReportPackageImportPluginView(Plugin plugin) {
        super(plugin);
        init();
    }

    private void init() {
        mainPanel.add(new Label(LocalizeUtil.get(LocalizationKeys.ADD_REPORTS_PACKAGE_KEY,
                BusinessUniverseConstants.ADD_REPORTS_PACKAGE)));
        attachmentBox = createAttachmentBox();
        mainPanel.add(attachmentBox);
        Application.getInstance().unlockScreen();
    }

    @Override
    public IsWidget getViewWidget() {
        return mainPanel;
    }

    protected AttachmentItem getAttachmentItem() {
        AttachmentBoxState state = (AttachmentBoxState) attachmentBox.getCurrentState();
        List<AttachmentItem> attachmentItems = state.getAttachments();
        return attachmentItems != null && !attachmentItems.isEmpty() ? attachmentItems.get(0) : null;
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
        state.setSingleChoice(true);
        AcceptedTypeConfig acceptedTypeConfig = new AcceptedTypeConfig();
        acceptedTypeConfig.setExtensions("jar");
        acceptedTypeConfig.setMimeType("application/java-archive");
        AcceptedTypesConfig acceptedTypesConfig = new AcceptedTypesConfig();
        acceptedTypesConfig.setAcceptedTypeConfigs(new ArrayList(Arrays.asList(acceptedTypeConfig)));
        state.setAcceptedTypesConfig(acceptedTypesConfig);
        attachmentBox.setDisplayConfig(displayConfig);
        EventBus eventBus = GWT.create(SimpleEventBus.class);
        attachmentBox.setEventBus(eventBus);
        attachmentBox.setState(state);
        attachmentBox.asWidget().setStyleName("upload-report-template");
        return attachmentBox;
    }
}
