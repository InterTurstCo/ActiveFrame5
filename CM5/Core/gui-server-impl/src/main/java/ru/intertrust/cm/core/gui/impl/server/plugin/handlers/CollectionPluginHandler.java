package ru.intertrust.cm.core.gui.impl.server.plugin.handlers;

import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.ConfigurationService;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.config.model.base.CollectionConfig;
import ru.intertrust.cm.core.config.model.gui.collection.view.CollectionColumnConfig;
import ru.intertrust.cm.core.config.model.gui.collection.view.CollectionDisplayConfig;
import ru.intertrust.cm.core.config.model.gui.collection.view.CollectionViewConfig;
import ru.intertrust.cm.core.config.model.gui.navigation.CollectionRefConfig;
import ru.intertrust.cm.core.config.model.gui.navigation.CollectionViewerConfig;
import ru.intertrust.cm.core.gui.api.server.plugin.PluginHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.GuiException;
import ru.intertrust.cm.core.gui.model.plugin.CollectionPluginData;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 13/9/13
 *         Time: 12:05 PM
 */
@ComponentName("collection.plugin")
public class CollectionPluginHandler extends PluginHandler {

    private  CollectionsService collectionsService;

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

        List<String> columnNames = getColumnNames(collectionViewConfig);

        pluginData.setColumnNames(columnNames);
        IdentifiableObjectCollection identifiableObjectCollection = getData(collectionName);
        pluginData.setCollection(identifiableObjectCollection);
        if (identifiableObjectCollection != null) {
            List<Id> ids = new ArrayList<>(identifiableObjectCollection.size());
            for (IdentifiableObject identifiableObject : identifiableObjectCollection) {
                ids.add(identifiableObject.getId());
            }
            pluginData.setIds(ids);
        }

        List<String> columnFields = getColumnFields(collectionViewConfig);
        List<List<String>> rowsList = preparingRowsForWidget(identifiableObjectCollection, columnFields);
        pluginData.setStringList(rowsList);

       // pluginData.setIdentifiableObjects(identifiableObjectCollection);

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
    private List<String> getColumnNames(CollectionViewConfig collectionViewConfig){
        List<String> columnNames = new ArrayList<String>();
        CollectionDisplayConfig collectionDisplay = collectionViewConfig.getCollectionDisplayConfig();
        if(collectionDisplay != null) {
        List<CollectionColumnConfig> columnConfigs = collectionDisplay.getColumnConfig();
        for (CollectionColumnConfig collectionColumnConfig : columnConfigs) {
            if (collectionColumnConfig.isHidden()) {
                continue;
            }
            String columnName = collectionColumnConfig.getName();
            columnNames.add(columnName);
        }
           return  columnNames;

        } else throw  new GuiException("Collection view config has no display tags configured ");

    }
    private List<String> getColumnFields(CollectionViewConfig collectionViewConfig){
        List<String> columnFields = new ArrayList<String>();
        CollectionDisplayConfig collectionDisplay = collectionViewConfig.getCollectionDisplayConfig();
        List<CollectionColumnConfig> columnConfigs = collectionDisplay.getColumnConfig();
        for (CollectionColumnConfig collectionColumnConfig : columnConfigs) {
           if (collectionColumnConfig.isHidden()) {
               continue;
           }
            String columnName = collectionColumnConfig.getField();
            columnFields.add(columnName);
        }
        return  columnFields;
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
            return (CollectionsService) ctx.lookup("java:app/web-app/CollectionsServiceImpl!ru.intertrust.cm.core.business.api.CollectionsService");
        } catch (NamingException ex) {
            throw new GuiException("EJB not found", ex);
        }
    }

    private List<List<String>> preparingRowsForWidget
            (IdentifiableObjectCollection identifiableObjectCollection, List<String> columnFields) {
        List<List<String>> rowsList = new ArrayList<List<String>>();
        for( int i = 0; i < identifiableObjectCollection.size(); i++){
            IdentifiableObject identifiableObject = identifiableObjectCollection.get(i);
            List<String> rowList = new ArrayList<String>();
            for(String field: columnFields){
                String fieldValue;
                if ("id".equalsIgnoreCase(field)) {
                    fieldValue = identifiableObject.getId().toStringRepresentation();
                } else {
                    Value value = identifiableObject.getValue(field);
                    fieldValue = value == null || value.get() == null ? "" : value.get().toString();
                }

                rowList.add(fieldValue);
            }
            rowsList.add(rowList);
    }
        return  rowsList;
    }
}
