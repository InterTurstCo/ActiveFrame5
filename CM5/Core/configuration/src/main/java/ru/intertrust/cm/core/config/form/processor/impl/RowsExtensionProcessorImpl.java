package ru.intertrust.cm.core.config.form.processor.impl;

import ru.intertrust.cm.core.config.form.MarkupUtils;
import ru.intertrust.cm.core.config.form.processor.ExtensionOperationAction;
import ru.intertrust.cm.core.config.form.processor.ExtensionOperationStatus;
import ru.intertrust.cm.core.config.form.processor.RowsExtensionProcessor;
import ru.intertrust.cm.core.config.gui.form.MarkupConfig;
import ru.intertrust.cm.core.config.gui.form.RowConfig;
import ru.intertrust.cm.core.config.gui.form.TabConfig;
import ru.intertrust.cm.core.config.gui.form.TabGroupConfig;
import ru.intertrust.cm.core.config.gui.form.extension.IdentifiedFormExtensionOperation;
import ru.intertrust.cm.core.config.gui.form.extension.markup.AddRowsConfig;
import ru.intertrust.cm.core.config.gui.form.extension.markup.DeleteRowsConfig;
import ru.intertrust.cm.core.config.gui.form.extension.markup.ReplaceRowsConfig;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static ru.intertrust.cm.core.config.form.processor.ExtensionProcessorHelper.*;
import static ru.intertrust.cm.core.config.gui.form.extension.markup.ExtensionPlace.AFTER;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 18.05.2015
 *         Time: 8:25
 */
public class RowsExtensionProcessorImpl implements RowsExtensionProcessor {
    @Override
    public void processAddRows(MarkupConfig markupConfig, AddRowsConfig addRowsConfig, List<String> errors) {
        List<IdentifiedFormExtensionOperation<RowConfig>> operations = addRowsConfig.getOperations();
        Map<IdentifiedFormExtensionOperation<RowConfig>, ExtensionOperationStatus> extensionOperationMap =
                createOperationMap(operations);
        processHeaderRows(extensionOperationMap, MarkupUtils.getHeaderRows(markupConfig.getHeader()));
        processBodyRows(extensionOperationMap, markupConfig.getBody().getTabs());
        fillErrors(extensionOperationMap.values(), errors);
    }

    @Override
    public void processDeleteRows(MarkupConfig markupConfig, DeleteRowsConfig deleteRowsConfig, List<String> errors) {
        List<RowConfig> source = deleteRowsConfig.getRowConfigs();
        ExtensionOperationAction<RowConfig> action = new ExtensionOperationAction<RowConfig>() {
            @Override
            public void process(List<RowConfig> target, List<RowConfig> source, ExtensionOperationStatus operationStatus) {
                processDeleteConfigs(target, source, operationStatus);
            }
        };
        processRowExtensionAction(action, markupConfig, source, errors);
    }

    @Override
    public void processReplaceRows(MarkupConfig markupConfig, ReplaceRowsConfig replaceRowsConfig, List<String> errors) {
        List<RowConfig> source = replaceRowsConfig.getRowConfigs();
        ExtensionOperationAction<RowConfig> action = new ExtensionOperationAction<RowConfig>() {
            @Override
            public void process(List<RowConfig> target, List<RowConfig> source, ExtensionOperationStatus operationStatus) {
                processReplaceConfigs(target, source, operationStatus);
            }
        };
        processRowExtensionAction(action, markupConfig, source, errors);
    }
    private void processHeaderRows(Map<IdentifiedFormExtensionOperation<RowConfig>, ExtensionOperationStatus> extensionOperationMap,
                                   List<RowConfig> target) {
        Set<IdentifiedFormExtensionOperation<RowConfig>> operations = extensionOperationMap.keySet();
        for (IdentifiedFormExtensionOperation<RowConfig> operation : operations) {
            ExtensionOperationStatus operationStatus = extensionOperationMap.get(operation);
            if (AFTER.equals(operation.getExtensionPlace())) {
                processAddConfigsAfter(operation.getId(), target, operation.getSource(), operationStatus);
            } else {
                processAddConfigsBefore(operation.getId(), target, operation.getSource(), operationStatus);
            }
        }
    }

    private void processBodyRows(Map<IdentifiedFormExtensionOperation<RowConfig>, ExtensionOperationStatus> extensionOperationMap,
                                 List<TabConfig> tabs) {
        Set<IdentifiedFormExtensionOperation<RowConfig>> operations = extensionOperationMap.keySet();
        for (TabConfig tab : tabs) {
            if (isProcessedSuccessful(extensionOperationMap.values())) {
                break;
            }
            List<TabGroupConfig> tabGroups = tab.getGroupList().getTabGroupConfigs();
            for (TabGroupConfig tabGroup : tabGroups) {
                if (isProcessedSuccessful(extensionOperationMap.values())) {
                    break;
                }
                List<RowConfig> target = MarkupUtils.getTabGroupRows(tabGroup);
                for (IdentifiedFormExtensionOperation<RowConfig> operation : operations) {
                    ExtensionOperationStatus operationStatus = extensionOperationMap.get(operation);
                    if (operationStatus.isNotSuccessful()) {
                        if (AFTER.equals(operation.getExtensionPlace())) {
                            processAddConfigsAfter(operation.getId(), target, operation.getSource(), operationStatus);
                        } else {
                            processAddConfigsBefore(operation.getId(), target, operation.getSource(), operationStatus);
                        }
                    }
                }
            }
        }

    }
    private void processRowExtensionAction(ExtensionOperationAction<RowConfig> action, MarkupConfig markupConfig,
                                           List<RowConfig> source, List<String> errors) {
        ExtensionOperationStatus operationStatus = new ExtensionOperationStatus();
        List<RowConfig> headerTarget = MarkupUtils.getHeaderRows(markupConfig.getHeader());
        action.process(headerTarget, source, operationStatus);
        List<TabConfig> tabConfigs = markupConfig.getBody().getTabs();
        for (TabConfig tabConfig : tabConfigs) {
            if (operationStatus.isNotSuccessful()) {
                List<TabGroupConfig> tabGroupConfigs = tabConfig.getGroupList().getTabGroupConfigs();
                for (TabGroupConfig tabGroupConfig : tabGroupConfigs) {
                    if (operationStatus.isNotSuccessful()) {
                        List<RowConfig> tabGroupTarget = MarkupUtils.getTabGroupRows(tabGroupConfig);
                        action.process(tabGroupTarget, source, operationStatus);
                    } else {
                        break;
                    }
                }
            } else {
                break;
            }
        }
        fillErrors(operationStatus, errors);

    }
}
