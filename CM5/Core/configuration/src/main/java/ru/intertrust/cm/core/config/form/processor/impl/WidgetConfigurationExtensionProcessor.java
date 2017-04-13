package ru.intertrust.cm.core.config.form.processor.impl;

import ru.intertrust.cm.core.config.form.processor.ExtensionOperationStatus;
import ru.intertrust.cm.core.config.form.processor.FormExtensionProcessor;
import ru.intertrust.cm.core.config.gui.form.WidgetRefConfig;
import ru.intertrust.cm.core.config.gui.form.extension.FormExtensionOperation;
import ru.intertrust.cm.core.config.gui.form.extension.widget.configuration.AddWidgetsConfig;
import ru.intertrust.cm.core.config.gui.form.extension.widget.configuration.DeleteWidgetsConfig;
import ru.intertrust.cm.core.config.gui.form.extension.widget.configuration.ReplaceWidgetsConfig;
import ru.intertrust.cm.core.config.gui.form.widget.WidgetConfig;
import ru.intertrust.cm.core.config.gui.form.widget.WidgetConfigurationConfig;

import java.util.List;

import static ru.intertrust.cm.core.config.form.processor.ExtensionProcessorHelper.*;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 12.05.2015
 *         Time: 8:32
 */
public class WidgetConfigurationExtensionProcessor implements FormExtensionProcessor {
    private WidgetConfigurationConfig widgetConfigurationConfig;
    private List<String> errors;

    private AddWidgetsConfig addWidgetsConfig;
    private DeleteWidgetsConfig deleteWidgetsConfig;
    private ReplaceWidgetsConfig replaceWidgetsConfig;

    public WidgetConfigurationExtensionProcessor() {
    }

    public WidgetConfigurationExtensionProcessor(WidgetConfigurationConfig widgetConfigurationConfig, List<String> errors) {
        this.widgetConfigurationConfig = widgetConfigurationConfig;
        this.errors = errors;
    }

    public WidgetConfigurationExtensionProcessor(WidgetConfigurationConfig widgetConfigurationConfig,
                                                 FormExtensionOperation formExtensionOperation, List<String> errors) {
        this(widgetConfigurationConfig, errors);
        final Class<? extends FormExtensionOperation> clazz = formExtensionOperation.getClass();
        if (clazz.equals(AddWidgetsConfig.class)) {
            this.addWidgetsConfig = (AddWidgetsConfig) formExtensionOperation;
        } else if (clazz.equals(DeleteWidgetsConfig.class)) {
            this.deleteWidgetsConfig = (DeleteWidgetsConfig) formExtensionOperation;
        } else if (clazz.equals(ReplaceWidgetsConfig.class)) {
            this.replaceWidgetsConfig = (ReplaceWidgetsConfig) formExtensionOperation;
        }
    }

    public WidgetConfigurationExtensionProcessor(WidgetConfigurationConfig widgetConfigurationConfig,
                                                 AddWidgetsConfig addWidgetsConfig, List<String> errors) {
        this(widgetConfigurationConfig, errors);
        this.addWidgetsConfig = addWidgetsConfig;
    }

    public WidgetConfigurationExtensionProcessor(WidgetConfigurationConfig widgetConfigurationConfig,
                                                 DeleteWidgetsConfig deleteWidgetsConfig, List<String> errors) {
        this(widgetConfigurationConfig, errors);
        this.deleteWidgetsConfig = deleteWidgetsConfig;
    }

    public WidgetConfigurationExtensionProcessor(WidgetConfigurationConfig widgetConfigurationConfig,
                                                 ReplaceWidgetsConfig replaceWidgetsConfig, List<String> errors) {
        this(widgetConfigurationConfig, errors);
        this.replaceWidgetsConfig = replaceWidgetsConfig;
    }


    @Override
    public void processFormExtension() {
       if(addWidgetsConfig != null){
           processAddWidgetConfiguration();
       } else if(deleteWidgetsConfig != null){
           processDeleteWidgetConfiguration();
       } else if(replaceWidgetsConfig != null){
           processReplaceWidgetConfiguration();
       }
    }

    public void processAddWidgetConfiguration() {
        List<WidgetConfig> target = widgetConfigurationConfig.getWidgetConfigList();
        List<WidgetConfig> source = addWidgetsConfig.getWidgetConfigs();
        processAddConfigs(target, source);

    }
    public void processDeleteWidgetConfiguration() {
        List<WidgetConfig> target = widgetConfigurationConfig.getWidgetConfigList();
        List<WidgetRefConfig> source = deleteWidgetsConfig.getWidgetRefConfigs();
        ExtensionOperationStatus operationStatus = new ExtensionOperationStatus();
        processDeleteConfigs(target, source, operationStatus);
        fillErrors(operationStatus, errors);

    }

    public void processReplaceWidgetConfiguration() {
        List<WidgetConfig> target = widgetConfigurationConfig.getWidgetConfigList();
        List<WidgetConfig> source = replaceWidgetsConfig.getWidgetConfigs();
        ExtensionOperationStatus operationStatus = new ExtensionOperationStatus();
        processReplaceConfigs(target, source, operationStatus);
        fillErrors(operationStatus, errors);

    }

}
