package ru.intertrust.cm.core.gui.impl.server.plugin.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.gui.api.server.plugin.PluginHandler;
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

    public HierarchyPluginData initialize(Dto config){
        return new HierarchyPluginData();
    }

    public HierarchyPluginData getCollectionItems(Dto request){
        HierarchyPluginData pData = new HierarchyPluginData();
        ArrayList<CollectionRowItem> items = new ArrayList<CollectionRowItem>();
/*
        IdentifiableObjectCollection collection = collectionsService.findCollection(hierarchyRequst.getCollectionName(),
                hierarchyRequst.getSortOrder(),
                hierarchyRequst.getFilters(),
                hierarchyRequst.getOffset(),
                hierarchyRequst.getCount());
*/
        CollectionRowItem aRow = new CollectionRowItem();
        aRow.setId(new RdbmsId(5067,1));
        aRow.setRow(new HashMap<String,Value>());
        aRow.getRow().put("ID",new LongValue(1));
        aRow.getRow().put("Name",new StringValue("Санкт-Петербург"));
        aRow.getRow().put("Population",new LongValue(3000000));

        items.add(aRow);

        pData.setCollectionRowItems(items);
        return pData;
    }
}
