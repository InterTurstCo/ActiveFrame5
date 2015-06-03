package ru.intertrust.cm.core.config.form.processor.impl;

import ru.intertrust.cm.core.config.form.processor.ExtensionOperationStatus;
import ru.intertrust.cm.core.config.form.processor.TabsExtensionProcessor;
import ru.intertrust.cm.core.config.gui.form.MarkupConfig;
import ru.intertrust.cm.core.config.gui.form.TabConfig;
import ru.intertrust.cm.core.config.gui.form.extension.IdentifiedFormExtensionOperation;
import ru.intertrust.cm.core.config.gui.form.extension.markup.AddTabsConfig;
import ru.intertrust.cm.core.config.gui.form.extension.markup.DeleteTabsConfig;
import ru.intertrust.cm.core.config.gui.form.extension.markup.ReplaceTabsConfig;

import java.util.List;
import java.util.Map;

import static ru.intertrust.cm.core.config.form.processor.ExtensionProcessorHelper.*;
import static ru.intertrust.cm.core.config.gui.form.extension.markup.ExtensionPlace.AFTER;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 17.05.2015
 *         Time: 21:44
 */
public class TabsExtensionProcessorImpl implements TabsExtensionProcessor {
    @Override
    public void processAddTabs(MarkupConfig markupConfig, AddTabsConfig addTabsConfig, List<String> errors) {
        List<TabConfig> tabConfigs = markupConfig.getBody().getTabs();
        List<IdentifiedFormExtensionOperation<TabConfig>> operations =
                addTabsConfig.getOperations();
        Map<IdentifiedFormExtensionOperation<TabConfig>, ExtensionOperationStatus> extensionOperationMap =
                createOperationMap(operations);
        for (IdentifiedFormExtensionOperation<TabConfig> operation : operations) {
            ExtensionOperationStatus operationStatus = extensionOperationMap.get(operation);
            if (AFTER.equals(operation.getExtensionPlace())) {
                processAddConfigsAfter(operation.getId(), tabConfigs, operation.getSource(), operationStatus);
            } else {
                processAddConfigsBefore(operation.getId(), tabConfigs, operation.getSource(), operationStatus);
            }

        }
        fillErrors(extensionOperationMap.values(), errors);
    }

    @Override
    public void processDeleteTabs(MarkupConfig markupConfig, DeleteTabsConfig deleteTabsConfig, List<String> errors) {
        List<TabConfig> target = markupConfig.getBody().getTabs();
        List<TabConfig> source = deleteTabsConfig.getTabConfigs();
        ExtensionOperationStatus operationStatus = new ExtensionOperationStatus();
        processDeleteConfigs(target, source, operationStatus);
        fillErrors(operationStatus, errors);
    }

    @Override
    public void processReplaceTabs(MarkupConfig markupConfig, ReplaceTabsConfig replaceTabsConfig, List<String> errors) {
        List<TabConfig> target = markupConfig.getBody().getTabs();
        List<TabConfig> source = replaceTabsConfig.getTabConfigs();
        ExtensionOperationStatus operationStatus = new ExtensionOperationStatus();
        processReplaceConfigs(target, source, operationStatus);
        fillErrors(operationStatus, errors);
    }
}
