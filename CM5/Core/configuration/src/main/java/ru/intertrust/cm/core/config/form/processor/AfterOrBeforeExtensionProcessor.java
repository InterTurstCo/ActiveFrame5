package ru.intertrust.cm.core.config.form.processor;


import ru.intertrust.cm.core.config.gui.form.CellConfig;
import ru.intertrust.cm.core.config.gui.form.RowConfig;
import ru.intertrust.cm.core.config.gui.form.TabConfig;
import ru.intertrust.cm.core.config.gui.form.TabGroupConfig;
import ru.intertrust.cm.core.config.gui.form.extension.markup.*;

import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 17.05.2015
 *         Time: 17:32
 */
public interface AfterOrBeforeExtensionProcessor {

    void processIdentifiedConfigs(List<TabConfig> target, AfterTabConfig afterTabConfig, ExtensionOperationStatus operationStatus);
    void processIdentifiedConfigs(List<TabConfig> target, BeforeTabConfig beforeTabConfig, ExtensionOperationStatus operationStatus);

    void processIdentifiedConfigs(List<TabGroupConfig> target, AfterTabGroupConfig afterTabGroupConfig, ExtensionOperationStatus operationStatus);
    void processIdentifiedConfigs(List<TabGroupConfig> target, BeforeTabGroupConfig beforeTabGroupConfig, ExtensionOperationStatus operationStatus);

    void processIdentifiedConfigs(List<RowConfig> target, AfterRowConfig afterRowConfig, ExtensionOperationStatus operationStatus);
    void processIdentifiedConfigs(List<RowConfig> target, BeforeRowConfig beforeRowConfig, ExtensionOperationStatus operationStatus);

    void processIdentifiedConfigs(List<CellConfig> target, AfterCellConfig afterCellConfig, ExtensionOperationStatus operationStatus);
    void processIdentifiedConfigs(List<CellConfig> target, BeforeCellConfig beforeCellConfig, ExtensionOperationStatus operationStatus);

}
