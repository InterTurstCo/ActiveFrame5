package ru.intertrust.cm.core.gui.model.plugin;

import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 03.06.14
 *         Time: 17:15
 */
public class ConfigurationDeployerPluginData extends ActivePluginData {
    private List<DeployConfigType> configTypes;
    
    public ConfigurationDeployerPluginData() {
    }

    public List<DeployConfigType> getConfigTypes() {
        return configTypes;
    }

    public void setConfigTypes(List<DeployConfigType> configTypes) {
        this.configTypes = configTypes;
    }
}
