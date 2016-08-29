package ru.intertrust.cm.core.gui.impl.client.plugins.collection;



import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.web.bindery.event.shared.HandlerRegistration;
import ru.intertrust.cm.core.business.api.dto.SearchQuery;
import ru.intertrust.cm.core.config.gui.navigation.CollectionViewerConfig;
import ru.intertrust.cm.core.config.gui.navigation.InitialFiltersConfig;
import ru.intertrust.cm.core.gui.impl.client.event.SaveToCsvEvent;
import ru.intertrust.cm.core.gui.impl.client.event.SaveToCsvEventHandler;
import ru.intertrust.cm.core.gui.impl.client.util.JsonUtil;
import ru.intertrust.cm.core.gui.model.plugin.ExtendedSearchDomainObjectSurfacePluginData;
import ru.intertrust.cm.core.gui.model.plugin.ExtendedSearchPluginData;
import ru.intertrust.cm.core.gui.model.plugin.collection.CollectionRowsRequest;
import ru.intertrust.cm.core.gui.model.plugin.collection.ExtendedSearchCollectionPluginData;
import ru.intertrust.cm.core.gui.model.plugin.collection.ExtendedSearchCollectionRowsRequest;

/**
 * Created by Vitaliy Orlov on 16.08.2016.
 */
public class ExtendedSearchCollectionPluginView extends CollectionPluginView {
    public ExtendedSearchCollectionPluginView(ExtendedSearchCollectionPlugin plugin) {
        super(plugin);
    }

    @Override
    public CollectionRowsRequest createRequest() {
           return new ExtendedSearchCollectionRowsRequest(super.createRequest(), ((ExtendedSearchCollectionPluginData)plugin.getInitialData()).getSearchQuery());
    }


    @Override
    public SaveToCsvEventHandler createExportToCSVActionHahdler() {
        return new SaveToCsvEventHandler() {
            @Override
            public void saveToCsv(SaveToCsvEvent saveToCsvEvent) {
            SearchQuery searchQuery = ((ExtendedSearchCollectionPluginData) getPluginData()).getSearchQuery();
                final InitialFiltersConfig initialFiltersConfig =
                        ((CollectionViewerConfig) plugin.getConfig()).getInitialFiltersConfig();
                JSONObject requestObj = new JSONObject();
                JsonUtil.prepareJsonAttributes(requestObj, getPluginData().getCollectionName(), getSimpleSearchQuery(),
                        getSearchArea());
                JsonUtil.prepareJsonSortCriteria(requestObj, getPluginData().getDomainObjectFieldPropertiesMap(),
                        getSortCollectionState());
                JsonUtil.prepareJsonColumnProperties(requestObj, getPluginData().getDomainObjectFieldPropertiesMap(),
                        getFiltersMap());
                JsonUtil.prepareJsonInitialFilters(requestObj, initialFiltersConfig, "jsonInitialFilters");
                JsonUtil.prepareJsonHierarchicalFiltersConfig(requestObj, getHierarchicalFiltersConfig(),
                        "jsonHierarchicalFilters");

                JsonUtil.prepareJsonExtendedSearchParams(requestObj, searchQuery, "jsonSearchQuery");

                getCsvController().doExtendedSearchPostRequest(requestObj.toString());
            }
        };
    }
}
