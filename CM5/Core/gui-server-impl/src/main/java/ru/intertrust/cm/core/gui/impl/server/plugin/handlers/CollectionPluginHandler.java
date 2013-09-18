package ru.intertrust.cm.core.gui.impl.server.plugin.handlers;

import ru.intertrust.cm.core.business.api.ConfigurationService;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.model.base.CollectionConfig;
import ru.intertrust.cm.core.config.model.gui.collection.view.CollectionViewConfig;
import ru.intertrust.cm.core.gui.api.server.GuiService;
import ru.intertrust.cm.core.gui.api.server.plugin.PluginHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.GuiException;
import ru.intertrust.cm.core.gui.model.plugin.ActivePluginData;
import ru.intertrust.cm.core.gui.model.plugin.CollectionPluginData;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Collection;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 13/9/13
 *         Time: 12:05 PM
 */
@ComponentName("collection.plugin")
public class CollectionPluginHandler extends PluginHandler {

    public CollectionPluginData initialize(Dto param) {
        CollectionPluginData pluginData = new CollectionPluginData();
        pluginData.setCollectionConfigs(getCollectionConfig());
        //pluginData.setCollectionViewConfigs(getCollectionViewConfig());
        return pluginData;
    }

    public ActivePluginData doSomethingVeryGood(Dto dto) {
        System.out.println("SomeActivePluginHandler executed doSomethingVeryGood()");
        return null;
    }
    private Collection<CollectionConfig> getCollectionConfig()  {
        ConfigurationService configurationService = getConfigurationService();
        Collection<CollectionConfig> collectionConfigList = configurationService.getConfigs(CollectionConfig.class);
        return collectionConfigList;
    }
    private Collection<CollectionViewConfig> getCollectionViewConfig()  {
        ConfigurationService configurationService = getConfigurationService();
        Collection<CollectionViewConfig> collectionViewConfigList = configurationService.
                getConfigs(CollectionViewConfig.class);
        return collectionViewConfigList;
    }

    private ConfigurationService getConfigurationService() {
        InitialContext ctx;
        try {
            ctx = new InitialContext();
            return (ConfigurationService) ctx.
                lookup("java:app/web-app/ConfigurationServiceImpl!ru.intertrust.cm.core.business.api.ConfigurationService");
        } catch (NamingException ex) {
            throw new GuiException("EJB not found", ex);
        }
    }

    private GuiService getGuiService() {
        InitialContext ctx;
        try {
            ctx = new InitialContext();
            return (GuiService) ctx.lookup("java:app/web-app/GuiServiceImpl!ru.intertrust.cm.core.gui.api.server.GuiService");
        } catch (NamingException ex) {
            throw new GuiException("EJB not found", ex);
        }
    }
}
