package ru.intertrust.cm.core.gui.impl.server;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.ProfileService;
import ru.intertrust.cm.core.business.api.dto.SortOrder;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.base.CollectionConfig;
import ru.intertrust.cm.core.config.gui.collection.view.CollectionDisplayConfig;
import ru.intertrust.cm.core.config.gui.form.widget.linkediting.SelectionSortCriteriaConfig;
import ru.intertrust.cm.core.config.gui.navigation.DefaultSortCriteriaConfig;
import ru.intertrust.cm.core.gui.api.server.plugin.SortOrderHelper;
import ru.intertrust.cm.core.gui.impl.server.util.SortOrderBuilder;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 01.02.2015
 *         Time: 19:34
 */
public class SorOrderHelperImpl implements SortOrderHelper {
    @Autowired
    private ConfigurationExplorer configurationExplorer;
    @Autowired
    private ProfileService profileService;

    @Override
    public SortOrder buildSortOrder(String collectionName, DefaultSortCriteriaConfig defaultSortCriteriaConfig,
                                    CollectionDisplayConfig collectionDisplayConfig) {
        SortOrder result = SortOrderBuilder.getInitSortOrder(defaultSortCriteriaConfig, collectionDisplayConfig,
                profileService.getPersonLocale());
        if(result == null) {
            result = buildSortOrderByIdField(collectionName);

        }
        return result;
    }

    @Override
    public SortOrder buildSortOrder(String collectionName, DefaultSortCriteriaConfig defaultSortCriteriaConfig) {
        SortOrder result = SortOrderBuilder.getSimpleSortOrder(defaultSortCriteriaConfig);
        if(result == null){
            result = buildSortOrderByIdField(collectionName);
        }
        return result;
    }


    @Override
    public SortOrder buildSortOrder(String collectionName, SelectionSortCriteriaConfig sortCriteriaConfig) {
        SortOrder result = SortOrderBuilder.getSelectionSortOrder(sortCriteriaConfig);
        if(result == null){
            result = buildSortOrderByIdField(collectionName);
        }
        return result;
    }


    public SortOrder buildSortOrderByIdField(String collectionName){
        SortOrder result = null;
        CollectionConfig collectionConfig = configurationExplorer.getConfig(CollectionConfig.class, collectionName);
        if(collectionConfig != null){
            String idFieldName = collectionConfig.getIdField();
            result = SortOrderBuilder.getSortOderByFieldName(idFieldName);
        }
        return result;

    }
}
