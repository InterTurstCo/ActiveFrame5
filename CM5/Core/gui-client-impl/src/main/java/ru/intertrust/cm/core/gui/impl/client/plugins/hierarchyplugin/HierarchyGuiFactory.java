package ru.intertrust.cm.core.gui.impl.client.plugins.hierarchyplugin;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.LongValue;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.config.gui.navigation.hierarchyplugin.HierarchyCollectionConfig;
import ru.intertrust.cm.core.config.gui.navigation.hierarchyplugin.HierarchyGroupConfig;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.plugin.collection.CollectionRowItem;
import ru.intertrust.cm.core.gui.model.plugin.hierarchy.HierarchyPluginData;
import ru.intertrust.cm.core.gui.model.plugin.hierarchy.HierarchyRequest;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

import java.util.HashMap;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * Developer: Ravil Abdulkhairov
 * Date: 27.07.2016
 * Time: 10:22
 * To change this template use File | Settings | File and Code Templates.
 */
public class HierarchyGuiFactory {

    public Widget buildGroup(HierarchyGroupConfig aGroupConfig){
        return new HierarchyGroupView(aGroupConfig);
    }

    /**
     * Возвращает панель с элементами коллекции и группами
     * @param aCollectionConfig
     * @return
     */
    public Widget buildCollection(final HierarchyCollectionConfig aCollectionConfig){
        final VerticalPanel lines = new VerticalPanel();
        HierarchyPluginData pData = new HierarchyPluginData();
        HierarchyRequest hRequest = new HierarchyRequest();
        hRequest.setCollectionName(aCollectionConfig.getCollectionRefConfig().getName());
        hRequest.setViewName(aCollectionConfig.getHierarchyCollectionViewConfig().getCollectionView());
        pData.setHierarchyRequest(hRequest);

        Command command = new Command(HierarchyPluginStaticData.GET_COL_ROWS_METHOD_NAME,
                HierarchyPluginStaticData.PLUGIN_COMPONENT_NAME, pData);
        BusinessUniverseServiceAsync.Impl.executeCommand(command, new AsyncCallback<Dto>() {
            @Override
            public void onFailure(Throwable caught) {
                GWT.log("Something was going wrong while obtaining rows for collection with Id =  "+aCollectionConfig.getCid());
                caught.printStackTrace();
            }

            @Override
            public void onSuccess(Dto result) {
                HierarchyPluginData collectionRowsResponse = (HierarchyPluginData) result;
                List<CollectionRowItem> collectionRowItems = collectionRowsResponse.getCollectionRowItems();
                if (collectionRowItems.size() != 0) {
                    for(CollectionRowItem rItem :collectionRowItems){
                        lines.add(new HierarchyCollectionView(aCollectionConfig, rItem, collectionRowsResponse.getCollectionViewConfig()));
                    }
                }
            }
        });

        return lines;
    }


}
