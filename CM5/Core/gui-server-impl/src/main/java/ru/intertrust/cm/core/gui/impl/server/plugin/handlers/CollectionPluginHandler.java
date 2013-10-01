package ru.intertrust.cm.core.gui.impl.server.plugin.handlers;

import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.ConfigurationService;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.config.model.base.CollectionConfig;
import ru.intertrust.cm.core.config.model.gui.collection.view.CollectionViewConfig;
import ru.intertrust.cm.core.config.model.gui.navigation.CollectionRefConfig;
import ru.intertrust.cm.core.config.model.gui.navigation.CollectionViewerConfig;
import ru.intertrust.cm.core.gui.api.server.plugin.PluginHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.GuiException;
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

    private CollectionsService collectionsService;

    public CollectionPluginData initialize(Dto param) {

        collectionsService = getCollectionsService();
        CollectionViewerConfig collectionViewerConfig =(CollectionViewerConfig) param;
        CollectionRefConfig collectionRefConfig = collectionViewerConfig.getCollectionRefConfig();
        String collectionName = collectionRefConfig.getName();

        CollectionPluginData pluginData = new CollectionPluginData();
        CollectionConfig collectionConfig = getCollectionConfig(collectionName);
        pluginData.setCollectionConfig(collectionConfig);

        CollectionViewConfig collectionViewConfig = findRequiredCollectionView(collectionName);
        pluginData.setCollectionViewConfig(collectionViewConfig);

        IdentifiableObjectCollection identifiableObjectCollection = getData(collectionName);
        pluginData.setCollection(identifiableObjectCollection);

        return pluginData;
    }

    private IdentifiableObjectCollection getData(String collectionName) {
       return collectionsService.findCollection(collectionName);
    }

    private CollectionConfig getCollectionConfig(String collectionName)  {
        ConfigurationService configurationService = getConfigurationService();
        CollectionConfig collectionConfig = configurationService.getConfig(CollectionConfig.class, collectionName);

        return collectionConfig;
    }
    private Collection<CollectionViewConfig> getCollectionViewConfig()  {
       ConfigurationService configurationService = getConfigurationService();
       Collection<CollectionViewConfig> collectionViewConfigList = configurationService.
               getConfigs(CollectionViewConfig.class);

          return collectionViewConfigList;

    }

      private CollectionViewConfig findRequiredCollectionView(String collection)  {

          Collection<CollectionViewConfig> collectionViewConfigs = getCollectionViewConfig();
          for (CollectionViewConfig collectionViewConfig : collectionViewConfigs) {

              if(collectionViewConfig.getCollection().equalsIgnoreCase(collection)) {
                  return collectionViewConfig;
              }
          }
          throw new GuiException("Couldn't find for collection with name '" + collection + "'");
      }

    private ConfigurationService getConfigurationService() {
        InitialContext ctx;
        try {
            ctx = new InitialContext();
            return (ConfigurationService) ctx.
                lookup("java:app/web-app/ConfigurationServiceImpl!" +
                        "ru.intertrust.cm.core.business.api.ConfigurationService");
        } catch (NamingException ex) {
            throw new GuiException("EJB not found", ex);
        }
    }
    private CollectionsService getCollectionsService() {
        InitialContext ctx;
        try {
            ctx = new InitialContext();
            return (CollectionsService) ctx.lookup("java:app/web-app/CollectionsServiceImpl!" +
                    "ru.intertrust.cm.core.business.api.CollectionsService");
        } catch (NamingException ex) {
            throw new GuiException("EJB not found", ex);
        }
    }

}
