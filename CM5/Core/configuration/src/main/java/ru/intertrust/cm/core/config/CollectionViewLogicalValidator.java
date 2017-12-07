package ru.intertrust.cm.core.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.intertrust.cm.core.business.api.dto.Case;
import ru.intertrust.cm.core.config.base.CollectionConfig;
import ru.intertrust.cm.core.config.gui.collection.view.CollectionColumnConfig;
import ru.intertrust.cm.core.config.gui.collection.view.CollectionDisplayConfig;
import ru.intertrust.cm.core.config.gui.collection.view.CollectionViewConfig;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 04/10/13
 *         Time: 12:05 PM
 */
public class CollectionViewLogicalValidator implements ConfigurationValidator {
    final static Logger logger = LoggerFactory.getLogger(CollectionViewLogicalValidator.class);

    private ConfigurationExplorer configurationExplorer;
    private List<LogicalErrors> logicalErrorsList = new ArrayList<>();

    public CollectionViewLogicalValidator(ConfigurationExplorer configurationExplorer) {
        this.configurationExplorer = configurationExplorer;
    }

    /**
     * Выполняет логическую валидацию конфигурации представлений коллекции
     */
    public List<LogicalErrors> validate() {
        Collection<CollectionConfig> collectionConfigList = configurationExplorer.getConfigs(CollectionConfig.class);

        Collection<CollectionViewConfig> collectionViewConfigList = configurationExplorer.
                getConfigs(CollectionViewConfig.class);

        if (collectionConfigList.isEmpty()) {
            LogicalErrors logicalErrors = LogicalErrors.getInstance("Default", "collection");
            logicalErrors.addError("Collection config couldn't be resolved");
        } else {
            for (CollectionViewConfig collectionViewConfig : collectionViewConfigList) {
                String name = collectionViewConfig.getName();
                LogicalErrors logicalErrors = LogicalErrors.getInstance(name, "collection-view");
                validateCollectionViewConfig(collectionViewConfig, collectionConfigList, logicalErrors);
                if (logicalErrors.getErrorCount() > 0) {
                    logicalErrorsList.add(logicalErrors);
                }
            }
        }

        return logicalErrorsList;
    }

    private void validateCollectionViewConfig(CollectionViewConfig collectionViewConfig,
                                              Collection<CollectionConfig> collectionConfigs, LogicalErrors logicalErrors) {
        if (collectionViewConfig == null) {
            return;
        }

        String collectionName = collectionViewConfig.getCollection();

        if (collectionName == null) {
            return;
        }

        CollectionConfig collection = findRequiredCollectionByName(collectionConfigs, collectionName, logicalErrors);

        CollectionDisplayConfig collectionDisplayConfig = collectionViewConfig.getCollectionDisplayConfig();
        if (collection == null) {
            return;
        }
        if (collectionDisplayConfig == null) {
            return;
        }

        validateSqlForRequiredFields(collection, collectionDisplayConfig, logicalErrors);
    }

    private CollectionConfig findRequiredCollectionByName(Collection<CollectionConfig> collectionConfigs,
                                                          String collection, LogicalErrors logicalErrors) {

        for (CollectionConfig collectionConfig : collectionConfigs) {

            if (collection.equalsIgnoreCase(collectionConfig.getName())) {
                return collectionConfig;
            }
        }
        String error = String.format("Couldn't find collection with name '%s'", collection);
        logger.error(error);
        logicalErrors.addError(error);

        return null;
    }

    private void validateSqlForRequiredFields(CollectionConfig colConf,
                                              CollectionDisplayConfig colDisplayConf, LogicalErrors logicalErrors) {
        List<CollectionColumnConfig> columns = colDisplayConf.getColumnConfig();
        String sqlQuery = colConf.getPrototype();
        if (sqlQuery == null) {
            return;
        }
        sqlQuery = sqlQuery.toLowerCase();
        for (CollectionColumnConfig column : columns) {
            if (column != null) {
                String field = column.getField();
                if (!sqlQuery.contains(Case.toLower(field))) {
                    String collectionName = colConf.getName();
                    String error = String.format("Couldn't find field '%s' in sql query for collection with name '%s'",
                            field, collectionName);
                    logger.error(error);
                    logicalErrors.addError(error);
                }
            }
        }
    }

}
