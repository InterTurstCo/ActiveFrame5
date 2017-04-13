package ru.intertrust.cm.core.config.form.processor.impl;

import ru.intertrust.cm.core.config.form.processor.ExtensionOperationStatus;
import ru.intertrust.cm.core.config.form.processor.FormExtensionProcessor;
import ru.intertrust.cm.core.config.gui.form.WidgetGroupConfig;
import ru.intertrust.cm.core.config.gui.form.WidgetGroupsConfig;
import ru.intertrust.cm.core.config.gui.form.extension.FormExtensionOperation;
import ru.intertrust.cm.core.config.gui.form.extension.widget.groups.AddWidgetGroupsConfig;
import ru.intertrust.cm.core.config.gui.form.extension.widget.groups.DeleteWidgetGroupsConfig;
import ru.intertrust.cm.core.config.gui.form.extension.widget.groups.ReplaceWidgetGroupsConfig;

import java.util.List;

import static ru.intertrust.cm.core.config.form.processor.ExtensionProcessorHelper.*;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 12.05.2015
 *         Time: 9:15
 */
public class WidgetGroupsExtensionProcessor implements FormExtensionProcessor {
    private WidgetGroupsConfig widgetGroupsConfig;
    private List<String> errors;
    private AddWidgetGroupsConfig addWidgetGroupsConfig;
    private DeleteWidgetGroupsConfig deleteWidgetGroupsConfig;
    private ReplaceWidgetGroupsConfig replaceWidgetGroupsConfig;

    public WidgetGroupsExtensionProcessor() {
    }

    public WidgetGroupsExtensionProcessor(WidgetGroupsConfig widgetGroupsConfig, List<String> errors) {
        this.widgetGroupsConfig = widgetGroupsConfig;
        this.errors = errors;
    }

    public WidgetGroupsExtensionProcessor(WidgetGroupsConfig widgetGroupsConfig,
                                                 FormExtensionOperation formExtensionOperation, List<String> errors) {
        this(widgetGroupsConfig, errors);
        final Class<? extends FormExtensionOperation> clazz = formExtensionOperation.getClass();
        if (clazz.equals(AddWidgetGroupsConfig.class)) {
            this.addWidgetGroupsConfig = (AddWidgetGroupsConfig) formExtensionOperation;
        } else if (clazz.equals(DeleteWidgetGroupsConfig.class)) {
            this.deleteWidgetGroupsConfig = (DeleteWidgetGroupsConfig) formExtensionOperation;
        } else if (clazz.equals(ReplaceWidgetGroupsConfig.class)) {
            this.replaceWidgetGroupsConfig = (ReplaceWidgetGroupsConfig) formExtensionOperation;
        }
    }

    public WidgetGroupsExtensionProcessor(WidgetGroupsConfig widgetGroupsConfig,
                                          AddWidgetGroupsConfig addWidgetGroupsConfig, List<String> errors) {
        this(widgetGroupsConfig, errors);
        this.addWidgetGroupsConfig = addWidgetGroupsConfig;
    }

    public WidgetGroupsExtensionProcessor(WidgetGroupsConfig widgetGroupsConfig,
                                          DeleteWidgetGroupsConfig deleteWidgetGroupsConfig, List<String> errors) {
        this(widgetGroupsConfig, errors);
        this.deleteWidgetGroupsConfig = deleteWidgetGroupsConfig;
    }

    public WidgetGroupsExtensionProcessor(WidgetGroupsConfig widgetGroupsConfig,
                                          ReplaceWidgetGroupsConfig replaceWidgetGroupsConfig, List<String> errors) {
        this(widgetGroupsConfig, errors);
        this.replaceWidgetGroupsConfig = replaceWidgetGroupsConfig;
    }

    public void processFormExtension(){
        if(addWidgetGroupsConfig != null){
            processAddWidgetGroups();
        }else if(deleteWidgetGroupsConfig != null){
            processDeleteWidgetGroups();
        } else if(replaceWidgetGroupsConfig != null){
            processReplaceWidgetGroups();
        }
    }

    public void processAddWidgetGroups() {
        List<WidgetGroupConfig> target = widgetGroupsConfig.getWidgetGroupConfigList();
        List<WidgetGroupConfig> source = addWidgetGroupsConfig.getWidgetGroupConfigs();
        processAddConfigs(target, source);
    }


    public void processDeleteWidgetGroups() {
        List<WidgetGroupConfig> target = widgetGroupsConfig.getWidgetGroupConfigList();
        List<WidgetGroupConfig> source = deleteWidgetGroupsConfig.getWidgetGroupConfigs();
        ExtensionOperationStatus operationStatus = new ExtensionOperationStatus();
        processDeleteConfigs(target, source, operationStatus);
        fillErrors(operationStatus, errors);
    }

    public void processReplaceWidgetGroups() {
        List<WidgetGroupConfig> target = widgetGroupsConfig.getWidgetGroupConfigList();
        List<WidgetGroupConfig> source = replaceWidgetGroupsConfig.getWidgetGroupConfigs();
        ExtensionOperationStatus operationStatus = new ExtensionOperationStatus();
        processReplaceConfigs(target, source, operationStatus);
        fillErrors(operationStatus, errors);

    }
}
