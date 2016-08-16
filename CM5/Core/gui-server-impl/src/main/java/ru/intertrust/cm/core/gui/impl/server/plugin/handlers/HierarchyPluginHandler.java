package ru.intertrust.cm.core.gui.impl.server.plugin.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.gui.collection.view.CollectionColumnConfig;
import ru.intertrust.cm.core.config.gui.collection.view.CollectionViewConfig;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.gui.api.server.GuiContext;
import ru.intertrust.cm.core.gui.api.server.plugin.PluginHandler;
import ru.intertrust.cm.core.gui.impl.server.util.PluginHandlerHelper;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.plugin.collection.CollectionRowItem;
import ru.intertrust.cm.core.gui.model.plugin.hierarchy.HierarchyPluginData;
import ru.intertrust.cm.core.gui.model.plugin.hierarchy.HierarchyRequest;

import java.util.ArrayList;
import java.util.HashMap;

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
    private ConfigurationExplorer configurationService;

    @Autowired
    private CurrentUserAccessor currentUserAccessor;

    public HierarchyPluginData initialize(Dto config){
        return new HierarchyPluginData();
    }

    public HierarchyPluginData getCollectionItems(Dto request){
        HierarchyPluginData pData = (HierarchyPluginData)request;
        HierarchyRequest hRequest = pData.getHierarchyRequest();
        ArrayList<CollectionRowItem> items = new ArrayList<CollectionRowItem>();

        IdentifiableObjectCollection collection = collectionsService.findCollection(hRequest.getCollectionName(),
                hRequest.getSortOrder(),
                hRequest.getFilters(),
                hRequest.getOffset(),
                hRequest.getCount());

        CollectionViewConfig viewConfig = PluginHandlerHelper.findCollectionViewConfig(hRequest.getCollectionName(), hRequest.getViewName(),
                currentUserAccessor.getCurrentUser(),
                null, configurationService, collectionsService, GuiContext.getUserLocale());

        for(IdentifiableObject iObject : collection){
            CollectionRowItem aRow = new CollectionRowItem();
            aRow.setId(iObject.getId());
            aRow.setRow(new HashMap<String,Value>());
            if(viewConfig == null) {
                for (String fieldName : iObject.getFields()) {
                    aRow.getRow().put(fieldName, iObject.getValue(fieldName));
                }
            } else {
                for(CollectionColumnConfig column : viewConfig.getCollectionDisplayConfig().getColumnConfig()){
                    aRow.getRow().put(column.getName(), iObject.getValue(column.getField()));
                }
            }

            items.add(aRow);
        }

        pData.setCollectionViewConfig(viewConfig);
        pData.setCollectionRowItems(items);
        return pData;
    }
}
