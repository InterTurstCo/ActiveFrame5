package ru.intertrust.cm.core.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.intertrust.cm.core.config.model.base.CollectionConfig;
import ru.intertrust.cm.core.config.model.gui.collection.view.CollectionColumnConfig;
import ru.intertrust.cm.core.config.model.gui.collection.view.CollectionDisplayConfig;
import ru.intertrust.cm.core.config.model.gui.collection.view.CollectionViewConfig;

import java.util.Collection;
import java.util.List;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 04/10/13
 *         Time: 12:05 PM
 */
public class CollectionViewLogicalValidator {
    final static Logger logger = LoggerFactory.getLogger(CollectionViewLogicalValidator.class);

    private ConfigurationExplorer configurationExplorer;

    public CollectionViewLogicalValidator(ConfigurationExplorer configurationExplorer) {
        this.configurationExplorer = configurationExplorer;
    }

    /**
     * Выполняет логическую валидацию конфигурации коллекции
     */
    public void validate() {
        Collection<CollectionConfig> collectionConfigList = configurationExplorer.getConfigs(CollectionConfig.class);

        Collection<CollectionViewConfig> collectionViewConfigList = configurationExplorer.getConfigs(CollectionViewConfig.class);

        if (collectionConfigList.isEmpty()) {
            logger.info("Collection config couldn't be resolved");
            return;
        }
        for (CollectionViewConfig collectionViewConfig : collectionViewConfigList) {
            validateCollectionViewConfig(collectionViewConfig, collectionConfigList);
        }
        logger.info("Collection view config has passed logical validation");
    }

    private void validateCollectionViewConfig(CollectionViewConfig collectionViewConfig,
                                          Collection<CollectionConfig> collectionConfigList) {
        if (collectionViewConfig == null) {
            return;
        }

        String collectionName  = collectionViewConfig.getCollection();

        if (collectionName == null) {
            return;
        }

        CollectionConfig collection = findRequiredCollectionByName(collectionConfigList, collectionName);

        CollectionDisplayConfig collectionDisplayConfig = collectionViewConfig.getCollectionDisplayConfig();
        if (collection == null) {
            return;
        }
        if (collectionDisplayConfig == null) {
            return;
        }

        validateSqlForRequiredFields(collection, collectionDisplayConfig);
        }

    private CollectionConfig findRequiredCollectionByName(Collection<CollectionConfig> collectionConfigs ,String collection)  {

        for (CollectionConfig collectionConfig : collectionConfigs) {

            if(collection.equalsIgnoreCase(collectionConfig.getName())) {
                logger.info("View has found collection with name '" + collection +"'");
                return collectionConfig;
            }
        }
        logger.error("Couldn't find for collection with name '" + collection + "'");
        return null;
    }

    private void validateSqlForRequiredFields(CollectionConfig collectionConfig,
                                              CollectionDisplayConfig collectionDisplayConfig) {
       List<CollectionColumnConfig> columns = collectionDisplayConfig.getColumnConfig();
       String sqlQuery = collectionConfig.getPrototype();
        for (CollectionColumnConfig column : columns) {
          if (column !=null && !column.isHidden()) {
          String field = column.getField();
          if (!sqlQuery.contains(field)) {
              logger.error("Couldn't find field '" + field
                      + "' in sql query for collection with name '" + collectionConfig.getName() + "'");
          }
        }
        }
    }

}
