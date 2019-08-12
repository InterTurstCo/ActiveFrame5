package ru.intertrust.cm.core.gui.impl.client.plugins.configurationdeployer;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
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
import ru.intertrust.cm.core.gui.model.plugin.DeployConfigType;
import ru.intertrust.cm.core.model.FatalException;

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
    private ListBox configType = new ListBox();
    private Label attachmentLabel = new Label();

    public ConfigurationDeployerPluginView(Plugin plugin){
        super(plugin);
        init();
    }

    private void init() {
        mainPanel.add(new Label(LocalizeUtil.get(LocalizationKeys.CONFIG_FILES_TYPE_KEY,
                BusinessUniverseConstants.CONFIG_FILES_TYPE)));

        configType.setMultipleSelect(false);
        // Пустой элемент
        configType.addItem(LocalizeUtil.get(LocalizationKeys.SELECT_CONFIG_TYPE_KEY,
                BusinessUniverseConstants.SELECT_CONFIG_TYPE), "");
        for (DeployConfigType configItem : getConfigItems()) {
            configType.addItem(configItem.getDescription(), configItem.getType());
        }
        configType.addChangeHandler(new ChangeHandler() {
            
            @Override
            public void onChange(ChangeEvent event) {
                if (configType.getSelectedValue().equals("")) {
                    if (attachmentBox != null) {
                        mainPanel.remove(attachmentBox);
                        attachmentBox = null;
                    }
                    attachmentLabel.setVisible(false);
                }else {
                    if (attachmentBox != null) {
                        mainPanel.remove(attachmentBox);
                    }
                    DeployConfigType deployConfigType = getConfigItem(configType.getSelectedValue());
                    attachmentBox = createAttachmentBox(deployConfigType.getExtension());
                    mainPanel.add(attachmentBox);
                    
                    attachmentLabel.setVisible(true);
                }                
            }
        });
        mainPanel.add(configType);
        
        attachmentLabel = new Label(LocalizeUtil.get(LocalizationKeys.ADD_CONFIG_FILES_KEY,
                BusinessUniverseConstants.ADD_CONFIG_FILES));
        attachmentLabel.setVisible(false);
        mainPanel.add(attachmentLabel);
        
        mainPanel.setStyleName("uploadConfigurationWrapper");
        
        Application.getInstance().unlockScreen();
        Application.getInstance().getHistoryManager()
                .setMode(HistoryManager.Mode.APPLY, ConfigurationDeployerPlugin.class.getSimpleName());
    }

    /**
     *  Получаем список поддерживаемых обработчиков конфигураций
     * @return
     */
    private List<DeployConfigType> getConfigItems() {
        ConfigurationDeployerPlugin configurationDeployerPlugin = (ConfigurationDeployerPlugin)plugin;        
        return configurationDeployerPlugin.getConfigTypes();
    }
    
    private DeployConfigType getConfigItem(String configType) {
        List<DeployConfigType> configItems = getConfigItems();
        for (DeployConfigType deployConfigType : configItems) {
            if (configType.equalsIgnoreCase(deployConfigType.getType())) {
                return deployConfigType;
            }
        }
        throw new FatalException("Not found config for " + configType);
    }
    

    @Override
    public IsWidget getViewWidget() {
        return mainPanel;
    }

    protected List<AttachmentItem> getAttachmentItems() {
        AttachmentBoxState state = (AttachmentBoxState)attachmentBox.getCurrentState();

        return state.getAttachments();
    }

    protected String getConfigType() {        
        return configType.getSelectedValue();
    }    
    
    public void clear() {
        mainPanel.remove(attachmentBox);
        if (!configType.getSelectedValue().equals("")) {
            attachmentBox = createAttachmentBox(configType.getSelectedValue());
            mainPanel.add(attachmentBox);
        }
    }

    private AttachmentBoxWidget createAttachmentBox(String extensions) {
        AttachmentBoxWidget attachmentBox = ComponentRegistry.instance.get("attachment-box");
        WidgetDisplayConfig displayConfig = new WidgetDisplayConfig();
        AttachmentBoxState state = new AttachmentBoxState();
        AcceptedTypesConfig acceptedTypesConfig = new AcceptedTypesConfig();
        List<AcceptedTypeConfig> acceptedTypeConfigs = new ArrayList<AcceptedTypeConfig>();
        AcceptedTypeConfig acceptedTypeConfig = new AcceptedTypeConfig();
        acceptedTypeConfig.setExtensions(extensions);
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
