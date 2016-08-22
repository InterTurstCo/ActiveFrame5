package ru.intertrust.cm.core.gui.impl.server.plugin.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.gui.collection.view.CollectionColumnConfig;
import ru.intertrust.cm.core.config.gui.collection.view.CollectionViewConfig;
import ru.intertrust.cm.core.config.gui.navigation.InitialFiltersConfig;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.gui.api.server.GuiContext;
import ru.intertrust.cm.core.gui.api.server.UserSettingsFetcher;
import ru.intertrust.cm.core.gui.api.server.plugin.FilterBuilder;
import ru.intertrust.cm.core.gui.api.server.plugin.PluginHandler;
import ru.intertrust.cm.core.gui.impl.server.util.PluginHandlerHelper;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.filters.ComplexFiltersParams;
import ru.intertrust.cm.core.gui.model.plugin.collection.CollectionRowItem;
import ru.intertrust.cm.core.gui.model.plugin.hierarchy.HierarchyPluginData;
import ru.intertrust.cm.core.gui.model.plugin.hierarchy.HierarchyRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * Developer: Ravil Abdulkhairov
 * Date: 26.07.2016
 * Time: 15:16
 * To change this template use File | Settings | File and Code Templates.
 */
@ComponentName("hierarchy.plugin")
public class HierarchyPluginHandler extends PluginHandler {

    @Autowired
    private CollectionsService collectionsService;

    @Autowired
    private CrudService crudService;

    @Autowired
    private UserSettingsFetcher userSettingsFetcher;

    @Autowired
    private ConfigurationExplorer configurationService;

    @Autowired
    private CurrentUserAccessor currentUserAccessor;

    @Autowired
    private FilterBuilder filterBuilder;

    public HierarchyPluginData initialize(Dto config) {
        return new HierarchyPluginData();
    }

    public HierarchyPluginData getCollectionItems(Dto request) {
        HierarchyPluginData pData = (HierarchyPluginData) request;
        HierarchyRequest hRequest = pData.getHierarchyRequest();
        ArrayList<CollectionRowItem> items = new ArrayList<CollectionRowItem>();

        List<Filter> filters = new ArrayList<>();
        if (hRequest.getCollectionConfig().getCollectionExtraFiltersConfig() != null) {
            filterBuilder.prepareExtraFilters(hRequest.getCollectionConfig().getCollectionExtraFiltersConfig(), new ComplexFiltersParams(), filters);
        }


        IdentifiableObjectCollection collection = collectionsService.findCollection(hRequest.getCollectionName(),
                hRequest.getSortOrder(),
                filters,
                hRequest.getOffset(),
                hRequest.getCount());

        CollectionViewConfig viewConfig = PluginHandlerHelper.findCollectionViewConfig(hRequest.getCollectionName(), hRequest.getViewName(),
                currentUserAccessor.getCurrentUser(),
                null, configurationService, collectionsService, GuiContext.getUserLocale());

        for (IdentifiableObject iObject : collection) {
            CollectionRowItem aRow = new CollectionRowItem();
            aRow.setId(iObject.getId());
            aRow.setRow(new HashMap<String, Value>());
            if (viewConfig == null) {
                for (String fieldName : iObject.getFields()) {
                    aRow.getRow().put(fieldName, iObject.getValue(fieldName));
                }
            } else {
                for (CollectionColumnConfig column : viewConfig.getCollectionDisplayConfig().getColumnConfig()) {
                    aRow.getRow().put(column.getName(), iObject.getValue(column.getField()));
                }
            }

            items.add(aRow);
        }

        pData.setCollectionViewConfig(viewConfig);
        pData.setCollectionRowItems(items);
        return pData;
    }

    public HierarchyPluginData savePluginHistory(Dto request) {
        HierarchyPluginData pData = (HierarchyPluginData) request;
        DomainObject domainObject = userSettingsFetcher.getUserHipSettingsDomainObject(pData.getPluginId());
        Gson gson = new GsonBuilder().create();
        String jsonInString = gson.toJson(pData.getOpenedNodeList());
        domainObject.setString("plugin_state_json",jsonInString);
        crudService.save(domainObject);
        return pData;
    }
}
