package ru.intertrust.cm.core.gui.impl.server.action;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.plugin.PluginStorage;
import ru.intertrust.cm.core.gui.api.server.action.ConfigurationDeployer;
import ru.intertrust.cm.core.gui.model.plugin.DeployConfigType;

public class PluginDeployerImpl implements ConfigurationDeployer{
    @Autowired
    private PluginStorage pluginStorage;
    
    @Override
    public DeployConfigType getDeployConfigType() {
        return new DeployConfigType("plugin", "Плагин", "jar");
    }

    @Override
    public void deploy(String name, File file) {
        pluginStorage.deployPluginPackage(file.getPath());                            
    }

}
