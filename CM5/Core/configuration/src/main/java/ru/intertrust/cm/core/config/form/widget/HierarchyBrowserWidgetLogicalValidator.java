package ru.intertrust.cm.core.config.form.widget;

import ru.intertrust.cm.core.config.LogicalErrors;
import ru.intertrust.cm.core.config.WidgetConfigurationToValidate;
import ru.intertrust.cm.core.config.base.CollectionConfig;
import ru.intertrust.cm.core.config.gui.form.widget.HierarchyBrowserConfig;
import ru.intertrust.cm.core.config.gui.form.widget.NodeCollectionDefConfig;

import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 16.08.2015
 *         Time: 17:49
 */
public class HierarchyBrowserWidgetLogicalValidator extends AbstractWidgetLogicalValidator {
    @Override
    public void validate(WidgetConfigurationToValidate widget, LogicalErrors logicalErrors) {
        HierarchyBrowserConfig config = (HierarchyBrowserConfig) widget.getWidgetConfig();
        NodeCollectionDefConfig nodeConfig = config.getNodeCollectionDefConfig();

        if (nodeConfig != null && !widget.isMethodValidated("validateNode")) {
            validateHierarchyBrowserNode(widget, nodeConfig, logicalErrors);
            widget.addValidatedMethod("validateNode");
        }
    }

    private void validateHierarchyBrowserNode(WidgetConfigurationToValidate widget,
                                              NodeCollectionDefConfig nodeConfig, LogicalErrors logicalErrors) {
        String collectionName = nodeConfig.getCollection();
        CollectionConfig collectionConfig = validateIfCollectionExists(widget, collectionName, logicalErrors);
        if (collectionConfig != null) {
            validateIfFiltersExist(collectionConfig, nodeConfig.getParentFilter(), logicalErrors);
        }
        List<NodeCollectionDefConfig> childNodeConfigs = nodeConfig.getNodeCollectionDefConfigs();
        for (NodeCollectionDefConfig childNodeConfig : childNodeConfigs) {
            validateHierarchyBrowserNode(widget, childNodeConfig, logicalErrors);
        }
        validateIfFormsExist(widget, nodeConfig, logicalErrors);

    }

}
