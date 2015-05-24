package ru.intertrust.cm.core.config.form.processor.impl;

import ru.intertrust.cm.core.config.form.processor.ExtensionOperationAction;
import ru.intertrust.cm.core.config.form.processor.ExtensionOperationStatus;
import ru.intertrust.cm.core.config.form.processor.TabGroupsExtensionProcessor;
import ru.intertrust.cm.core.config.gui.form.MarkupConfig;
import ru.intertrust.cm.core.config.gui.form.TabConfig;
import ru.intertrust.cm.core.config.gui.form.TabGroupConfig;
import ru.intertrust.cm.core.config.gui.form.extension.IdentifiedFormExtensionOperation;
import ru.intertrust.cm.core.config.gui.form.extension.markup.AddTabGroupsConfig;
import ru.intertrust.cm.core.config.gui.form.extension.markup.DeleteTabGroupsConfig;
import ru.intertrust.cm.core.config.gui.form.extension.markup.ReplaceTabGroupsConfig;

import java.util.List;
import java.util.Map;

import static ru.intertrust.cm.core.config.form.processor.ExtensionProcessorHelper.*;
import static ru.intertrust.cm.core.config.gui.form.extension.markup.ExtensionPlace.AFTER;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 17.05.2015
 *         Time: 21:56
 */
public class TabGroupsExtensionProcessorImpl implements TabGroupsExtensionProcessor {
    @Override
    public void processAddTabGroups(MarkupConfig markupConfig, AddTabGroupsConfig addTabGroupsConfig, List<String> errors) {
        List<TabConfig> tabConfigs = markupConfig.getBody().getTabs();
        List<IdentifiedFormExtensionOperation<TabGroupConfig>> operations = addTabGroupsConfig.getOperations();
        Map<IdentifiedFormExtensionOperation<TabGroupConfig>, ExtensionOperationStatus> extensionOperationMap =
                createOperationMap(operations);
        for (TabConfig tabConfig : tabConfigs) {
            if (isProcessedSuccessful(extensionOperationMap.values())) {
                break;
            }
            List<TabGroupConfig> tabGroupConfigs = tabConfig.getGroupList().getTabGroupConfigs();
            for (IdentifiedFormExtensionOperation<TabGroupConfig> operation : operations) {
                ExtensionOperationStatus operationStatus = extensionOperationMap.get(operation);
                if (operationStatus.isNotSuccessful()) {
                    if (AFTER.equals(operation.getExtensionPlace())) {
                        processAddConfigsAfter(operation.getId(), tabGroupConfigs, operation.getSource(), operationStatus);
                    } else {
                        processAddConfigsBefore(operation.getId(), tabGroupConfigs, operation.getSource(), operationStatus);
                    }
                }
            }

        }
        fillErrors(extensionOperationMap.values(), errors);
    }

    @Override
    public void processDeleteTabGroups(MarkupConfig markupConfig, DeleteTabGroupsConfig deleteTabGroupsConfig, List<String> errors) {
        List<TabGroupConfig> source = deleteTabGroupsConfig.getTabGroupConfigs();
        ExtensionOperationAction<TabGroupConfig> action = new ExtensionOperationAction<TabGroupConfig>() {
            @Override
            public void process(List<TabGroupConfig> target, List<TabGroupConfig> source, ExtensionOperationStatus operationStatus) {
                processDeleteConfigs(target, source, operationStatus);
            }
        };
        processTabGroupsExtensionAction(action, markupConfig.getBody().getTabs(), source, errors);
    }

    @Override
    public void processReplaceTabGroups(MarkupConfig markupConfig, ReplaceTabGroupsConfig replaceTabGroupsConfig, List<String> errors) {
        List<TabGroupConfig> source = replaceTabGroupsConfig.getTabGroupConfigs();
        ExtensionOperationAction<TabGroupConfig> action = new ExtensionOperationAction<TabGroupConfig>() {
            @Override
            public void process(List<TabGroupConfig> target, List<TabGroupConfig> source, ExtensionOperationStatus operationStatus) {
                processReplaceConfigs(target, source, operationStatus);
            }
        };
        processTabGroupsExtensionAction(action, markupConfig.getBody().getTabs(), source, errors);
    }
    
    private void processTabGroupsExtensionAction(ExtensionOperationAction<TabGroupConfig> action, List<TabConfig> tabConfigs,
                                                 List<TabGroupConfig> source, List<String> errors) {
        ExtensionOperationStatus operationStatus = new ExtensionOperationStatus();
        for (TabConfig tabConfig : tabConfigs) {
            if (operationStatus.isNotSuccessful()) {
                List<TabGroupConfig> target = tabConfig.getGroupList().getTabGroupConfigs();
                action.process(target, source, operationStatus);
            } else {
                break;
            }
        }
        fillErrors(operationStatus, errors);
    }
}
