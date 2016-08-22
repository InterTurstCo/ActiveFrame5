package ru.intertrust.cm.core.gui.impl.client.plugins.hierarchyplugin;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.config.gui.form.widget.filter.ExtraParamConfig;
import ru.intertrust.cm.core.config.gui.form.widget.filter.extra.ExtraFilterConfig;
import ru.intertrust.cm.core.config.gui.navigation.hierarchyplugin.HierarchyCollectionConfig;
import ru.intertrust.cm.core.config.gui.navigation.hierarchyplugin.HierarchyGroupConfig;
import ru.intertrust.cm.core.gui.impl.client.event.hierarchyplugin.AutoOpenedEvent;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.plugin.collection.CollectionRowItem;
import ru.intertrust.cm.core.gui.model.plugin.hierarchy.HierarchyPluginData;
import ru.intertrust.cm.core.gui.model.plugin.hierarchy.HierarchyRequest;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * Developer: Ravil Abdulkhairov
 * Date: 27.07.2016
 * Time: 10:22
 * To change this template use File | Settings | File and Code Templates.
 */
public class HierarchyGuiFactory implements HierarchyPluginConstants {

    public Widget buildGroup(HierarchyGroupConfig aGroupConfig, Id aParentId, EventBus aCommonBus, String parentViewId, Boolean autoClick) {
        HierarchyGroupView grView = new HierarchyGroupView(aGroupConfig, aParentId, aCommonBus, parentViewId);
        return grView;
    }

    /**
     * Возвращает панель с элементами коллекции и группами
     *
     * @param aCollectionConfig
     * @return
     */
    public Widget buildCollection(final HierarchyCollectionConfig aCollectionConfig, Id aParentId, final EventBus aCommonBus, final String parentViewId, final Boolean autoClick) {
        final VerticalPanel lines = new VerticalPanel();
        HierarchyPluginData pData = new HierarchyPluginData();
        HierarchyRequest hRequest = new HierarchyRequest();
        hRequest.setCollectionName(aCollectionConfig.getCollectionRefConfig().getName());
        hRequest.setViewName(aCollectionConfig.getHierarchyCollectionViewConfig().getCollectionView());

        if (aCollectionConfig.getCollectionExtraFiltersConfig() != null) {
            for (ExtraFilterConfig extraFilterConfig : aCollectionConfig.getCollectionExtraFiltersConfig().getFilterConfigs()) {
                if (extraFilterConfig.getParamConfigs() == null || extraFilterConfig.getParamConfigs().size() == 0) {
                    ExtraParamConfig eXtraParamConfig = new ExtraParamConfig();
                    eXtraParamConfig.setName(0);
                    eXtraParamConfig.setType(REF_TYPE_NAME);
                    eXtraParamConfig.setValue(aParentId.toStringRepresentation());
                    extraFilterConfig.setParamConfigs(new ArrayList<ExtraParamConfig>());
                    extraFilterConfig.getParamConfigs().add(eXtraParamConfig);
                }
            }
        }


        hRequest.setCollectionConfig(aCollectionConfig);
        pData.setHierarchyRequest(hRequest);

        Command command = new Command(GET_COL_ROWS_METHOD_NAME, PLUGIN_COMPONENT_NAME, pData);
        BusinessUniverseServiceAsync.Impl.executeCommand(command, new AsyncCallback<Dto>() {
            @Override
            public void onFailure(Throwable caught) {
                GWT.log("Something was going wrong while obtaining rows for collection with Id =  " + aCollectionConfig.getCid());
                caught.printStackTrace();
            }

            @Override
            public void onSuccess(Dto result) {
                HierarchyPluginData collectionRowsResponse = (HierarchyPluginData) result;
                List<CollectionRowItem> collectionRowItems = collectionRowsResponse.getCollectionRowItems();
                if (collectionRowItems.size() != 0) {
                    for (CollectionRowItem rItem : collectionRowItems) {
                        lines.add(new HierarchyCollectionView(aCollectionConfig, rItem, collectionRowsResponse.getCollectionViewConfig(), aCommonBus, parentViewId));
                    }
                }

                if (aCollectionConfig.getCollectionExtraFiltersConfig() != null) {
                    for (ExtraFilterConfig extraFilterConfig : aCollectionConfig.getCollectionExtraFiltersConfig().getFilterConfigs()) {
                        if (extraFilterConfig.getParamConfigs() != null) {
                            if (extraFilterConfig.getParamConfigs().get(0).getType().equals(REF_TYPE_NAME)) {
                                extraFilterConfig.getParamConfigs().clear();
                            }
                        }
                    }
                }
                if(autoClick){
                aCommonBus.fireEvent(new AutoOpenedEvent(parentViewId));}
            }
        });

        return lines;
    }


}
