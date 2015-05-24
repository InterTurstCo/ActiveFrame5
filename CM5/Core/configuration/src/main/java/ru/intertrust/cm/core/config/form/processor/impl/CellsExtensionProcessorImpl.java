package ru.intertrust.cm.core.config.form.processor.impl;

import ru.intertrust.cm.core.config.form.MarkupUtils;
import ru.intertrust.cm.core.config.form.processor.CellsExtensionProcessor;
import ru.intertrust.cm.core.config.form.processor.ExtensionOperationAction;
import ru.intertrust.cm.core.config.form.processor.ExtensionOperationStatus;
import ru.intertrust.cm.core.config.gui.form.*;
import ru.intertrust.cm.core.config.gui.form.extension.IdentifiedFormExtensionOperation;
import ru.intertrust.cm.core.config.gui.form.extension.markup.AddCellsConfig;
import ru.intertrust.cm.core.config.gui.form.extension.markup.DeleteCellsConfig;
import ru.intertrust.cm.core.config.gui.form.extension.markup.ReplaceCellsConfig;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static ru.intertrust.cm.core.config.form.processor.ExtensionProcessorHelper.*;
import static ru.intertrust.cm.core.config.gui.form.extension.markup.ExtensionPlace.AFTER;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 18.05.2015
 *         Time: 8:27
 */
public class CellsExtensionProcessorImpl implements CellsExtensionProcessor {
    @Override
    public void processAddCells(MarkupConfig markupConfig, AddCellsConfig addCellsConfig, List<String> errors) {
        List<IdentifiedFormExtensionOperation<CellConfig>> operations = addCellsConfig.getOperations();
        Map<IdentifiedFormExtensionOperation<CellConfig>, ExtensionOperationStatus> extensionOperationMap =
                createOperationMap(operations);
        processHeaderRowsCells(extensionOperationMap, markupConfig.getHeader());
        processBodyRowsCells(extensionOperationMap, markupConfig.getBody().getTabs());
        fillErrors(extensionOperationMap.values(), errors);
    }

    @Override
    public void processDeleteCells(MarkupConfig markupConfig, DeleteCellsConfig deleteCellsConfig, List<String> errors) {
        List<CellConfig> source = deleteCellsConfig.getCellConfigs();
        ExtensionOperationAction<CellConfig> action = new ExtensionOperationAction<CellConfig>() {
            @Override
            public void process(List<CellConfig> target, List<CellConfig> source, ExtensionOperationStatus operationStatus) {
                processDeleteConfigs(target, source, operationStatus);
            }
        };
        processCellsExtensionAction(action, markupConfig, source, errors);
    }


    @Override
    public void processReplaceCells(MarkupConfig markupConfig, ReplaceCellsConfig replaceCellsConfig, List<String> errors) {
        List<CellConfig> source = replaceCellsConfig.getCellConfigs();
        ExtensionOperationAction<CellConfig> action = new ExtensionOperationAction<CellConfig>() {
            @Override
            public void process(List<CellConfig> target, List<CellConfig> source, ExtensionOperationStatus operationStatus) {
                processReplaceConfigs(target, source, operationStatus);
            }
        };
        processCellsExtensionAction(action, markupConfig, source, errors);
    }

    private void processHeaderRowsCells(Map<IdentifiedFormExtensionOperation<CellConfig>, ExtensionOperationStatus> extensionOperationMap,
                                        HeaderConfig headerConfig) {
        processRowsCells(extensionOperationMap, MarkupUtils.getHeaderRows(headerConfig));
    }


    private void processBodyRowsCells(Map<IdentifiedFormExtensionOperation<CellConfig>, ExtensionOperationStatus> extensionOperationMap,
                                      List<TabConfig> tabs) {
        for (TabConfig tab : tabs) {
            if (isProcessedSuccessful(extensionOperationMap.values())) {
                break;
            }
            List<TabGroupConfig> tabGroups = tab.getGroupList().getTabGroupConfigs();
            for (TabGroupConfig tabGroup : tabGroups) {
                if (isProcessedSuccessful(extensionOperationMap.values())) {
                    break;
                }
                List<RowConfig> rows = MarkupUtils.getTabGroupRows(tabGroup);
                processRowsCells(extensionOperationMap, rows);
            }
        }

    }

    private void processRowsCells(Map<IdentifiedFormExtensionOperation<CellConfig>, ExtensionOperationStatus> extensionOperationMap,
                                  List<RowConfig> rowConfigs) {
        for (RowConfig rowConfig : rowConfigs) {
            if (isProcessedSuccessful(extensionOperationMap.values())) {
                break;
            }
            processCells(extensionOperationMap, rowConfig.getCells());
        }

    }

    private void processCells(Map<IdentifiedFormExtensionOperation<CellConfig>, ExtensionOperationStatus> extensionOperationMap,
                              List<CellConfig> target) {
        Set<IdentifiedFormExtensionOperation<CellConfig>> operations = extensionOperationMap.keySet();
        for (IdentifiedFormExtensionOperation<CellConfig> operation : operations) {
            ExtensionOperationStatus operationStatus = extensionOperationMap.get(operation);
            if (AFTER.equals(operation.getExtensionPlace())) {
                processAddConfigsAfter(operation.getId(), target, operation.getSource(), operationStatus);
            } else {
                processAddConfigsBefore(operation.getId(), target, operation.getSource(), operationStatus);
            }
        }
    }

    private void processCellsExtensionAction(ExtensionOperationAction<CellConfig> action, MarkupConfig markupConfig,
                                             List<CellConfig> source, List<String> errors) {
        ExtensionOperationStatus operationStatus = new ExtensionOperationStatus();
        processHeaderCellsExtensionAction(action, MarkupUtils.getHeaderRows(markupConfig.getHeader()), source, operationStatus);
        processBodyCellsExtensionAction(action, markupConfig.getBody().getTabs(), source, operationStatus);
        fillErrors(operationStatus, errors);

    }

    private void processHeaderCellsExtensionAction(ExtensionOperationAction<CellConfig> action, List<RowConfig> headerRows,
                                                   List<CellConfig> source, ExtensionOperationStatus operationStatus) {
        for (RowConfig rowConfig : headerRows) {
            if (operationStatus.isNotSuccessful()) {
                action.process(rowConfig.getCells(), source, operationStatus);
            } else {
                break;
            }
        }
    }

    private void processBodyCellsExtensionAction(ExtensionOperationAction<CellConfig> action, List<TabConfig> tabConfigs,
                                                 List<CellConfig> source, ExtensionOperationStatus operationStatus) {
        for (TabConfig tabConfig : tabConfigs) {
            if (operationStatus.isNotSuccessful()) {
                List<TabGroupConfig> tabGroupConfigs = tabConfig.getGroupList().getTabGroupConfigs();
                for (TabGroupConfig tabGroupConfig : tabGroupConfigs) {
                    if (operationStatus.isNotSuccessful()) {
                        List<RowConfig> tabGroupRows = MarkupUtils.getTabGroupRows(tabGroupConfig);
                        for (RowConfig tabGroupRow : tabGroupRows) {
                            if (operationStatus.isNotSuccessful()) {
                                action.process(tabGroupRow.getCells(), source, operationStatus);
                            } else {
                                break;
                            }
                        }

                    } else {
                        break;
                    }
                }
            } else {
                break;
            }
        }

    }
}
