package ru.intertrust.cm.core.gui.impl.client.plugins.configurationdeployer;

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
import ru.intertrust.cm.core.gui.api.client.history.HistoryManager;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginView;
import ru.intertrust.cm.core.gui.impl.client.form.widget.attachmentbox.AttachmentBoxWidget;
import ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants;
import ru.intertrust.cm.core.gui.model.form.widget.AttachmentBoxState;
import ru.intertrust.cm.core.gui.model.form.widget.AttachmentItem;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 03.06.14
 *         Time: 17:15
 */

public class ConfigurationDeployerPluginView extends PluginView {
    private Panel mainPanel = new VerticalPanel();
    private AttachmentBoxWidget attachmentBox;

    public ConfigurationDeployerPluginView(Plugin plugin){
        super(plugin);
        init();
    }

    private void init() {
        mainPanel.add(new Label(LocalizeUtil.get(LocalizationKeys.ADD_CONFIG_FILES_KEY,
                BusinessUniverseConstants.ADD_CONFIG_FILES)));
        mainPanel.setStyleName("uploadConfigurationWrapper");
        attachmentBox = createAttachmentBox();
        mainPanel.add(attachmentBox);
        Application.getInstance().unlockScreen();
        Application.getInstance().getHistoryManager()
                .setMode(HistoryManager.Mode.APPLY, ConfigurationDeployerPlugin.class.getSimpleName());
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
        AcceptedTypesConfig acceptedTypesConfig = new AcceptedTypesConfig();
        List<AcceptedTypeConfig> acceptedTypeConfigs = new ArrayList<AcceptedTypeConfig>();
        AcceptedTypeConfig acceptedTypeConfig = new AcceptedTypeConfig();
        acceptedTypeConfig.setExtensions("xml, txt, bpmn, csv");
        acceptedTypeConfig.setMimeType("application/txt");
        acceptedTypeConfigs.add(acceptedTypeConfig);
        acceptedTypesConfig.setAcceptedTypeConfigs(acceptedTypeConfigs);
        state.setAcceptedTypesConfig(acceptedTypesConfig);
        attachmentBox.setDisplayConfig(displayConfig);
        EventBus eventBus = GWT.create(SimpleEventBus.class);
        attachmentBox.setEventBus(eventBus);
        attachmentBox.setState(state);
        attachmentBox.asWidget().setStyleName("uploadReportTemplate");

        return attachmentBox;
    }
}
