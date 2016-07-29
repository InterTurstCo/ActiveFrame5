package ru.intertrust.cm.core.gui.impl.server.plugin.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObject;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.gui.api.server.plugin.PluginHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.plugin.collection.CollectionRowItem;
import ru.intertrust.cm.core.gui.model.plugin.hierarchy.HierarchyPluginData;
import ru.intertrust.cm.core.gui.model.plugin.hierarchy.HierarchyRequest;

import java.util.ArrayList;

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

    public HierarchyPluginData initialize(Dto config){
        return new HierarchyPluginData();
    }

    public HierarchyPluginData getCollectionItems(Dto request){
        HierarchyPluginData pData = new HierarchyPluginData();
        HierarchyRequest hierarchyRequst = (HierarchyRequest)request;
        ArrayList<CollectionRowItem> items = new ArrayList<CollectionRowItem>();

        IdentifiableObjectCollection collection = collectionsService.findCollection(hierarchyRequst.getCollectionName(),
                hierarchyRequst.getSortOrder(),
                hierarchyRequst.getFilters(),
                hierarchyRequst.getOffset(),
                hierarchyRequst.getCount());

        pData.setCollectionRowItems(items);
        return pData;
    }
}
