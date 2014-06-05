package ru.intertrust.cm.core.gui.model.action;

import ru.intertrust.cm.core.business.api.dto.ConfigurationDeployedItem;

import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 03.06.14
 *         Time: 17:15
 */
public class DeployConfigurationActionData extends ActionData {
    private List<ConfigurationDeployedItem> configurationDeployedItems;

    public List<ConfigurationDeployedItem> getConfigurationDeployedItems() {
        return configurationDeployedItems;
    }

    public void setConfigurationDeployedItems(List<ConfigurationDeployedItem> configurationDeployedItems) {
        this.configurationDeployedItems = configurationDeployedItems;
    }
}
