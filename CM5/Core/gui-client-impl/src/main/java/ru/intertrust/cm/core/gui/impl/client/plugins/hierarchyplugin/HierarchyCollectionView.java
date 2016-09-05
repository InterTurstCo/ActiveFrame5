package ru.intertrust.cm.core.gui.impl.client.plugins.hierarchyplugin;


import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.gui.collection.view.CollectionColumnConfig;
import ru.intertrust.cm.core.config.gui.collection.view.CollectionViewConfig;
import ru.intertrust.cm.core.config.gui.navigation.hierarchyplugin.HierarchyCollectionConfig;
import ru.intertrust.cm.core.gui.impl.client.event.UpdateCollectionEvent;
import ru.intertrust.cm.core.gui.impl.client.event.UpdateCollectionEventHandler;
import ru.intertrust.cm.core.gui.impl.client.event.collection.OpenDomainObjectFormEvent;
import ru.intertrust.cm.core.gui.impl.client.event.hierarchyplugin.*;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.plugin.collection.CollectionRowItem;
import ru.intertrust.cm.core.gui.model.plugin.hierarchy.HierarchyPluginData;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * Developer: Ravil Abdulkhairov
 * Date: 29.07.2016
 * Time: 12:11
 * To change this template use File | Settings | File and Code Templates.
 */
public class HierarchyCollectionView extends HierarchyNode implements HierarchyActionEventHandler, CancelSelectionEventHandler, UpdateCollectionEventHandler {

    private Boolean expandable = false;
    private CollectionRowItem rowItem;
    private CollectionViewConfig collectionViewConfig;
    private FlexTable grid;

    public HierarchyCollectionView(HierarchyCollectionConfig aCollectionConfig, CollectionRowItem aRow,
                                   CollectionViewConfig aCollectionViewConfig,
                                   EventBus aCommmonBus, String aParentViewId) {
        super();
        commonBus = aCommmonBus;
        collectionViewConfig = aCollectionViewConfig;
        rowItem = aRow;
        collectionConfig = aCollectionConfig;
        rootPanel.addStyleName(STYLE_HEADER_CELL);
        headerPanel.addStyleName(STYLE_WRAP_CELL);
        childPanel.addStyleName(STYLE_CHILD_CELL);

        if (collectionConfig.getHierarchyGroupConfigs().size() > 0 ||
                collectionConfig.getHierarchyCollectionConfigs().size() > 0) {
            expandable = true;
        }
        parentViewID = aParentViewId;
        setViewID(rowItem.getId().toStringRepresentation() + "-" + parentViewID);
        addRepresentationCells(headerPanel);
        rootPanel.add(headerPanel);
        rootPanel.add(childPanel);
        childPanel.setVisible(expanded);
        initWidget(rootPanel);

        headerPanel.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                headerPanel.addStyleName(STYLE_FOCUS_WRAP_CELL);
                // Уведомляем предыдущую выбраную строку что нужно снять подсветку
                commonBus.fireEvent(new CancelSelectionEvent(true, rowItem.getId()));
            }
        }, ClickEvent.getType());

        headerPanel.addDomHandler(new DoubleClickHandler() {

                                      @Override
                                      public void onDoubleClick(final DoubleClickEvent event) {
                                          commonBus.fireEvent(new OpenDomainObjectFormEvent(rowItem.getId()));
                                      }
                                  },
                DoubleClickEvent.getType());

        commonBus.fireEvent(new NodeCreatedEvent(getViewID()));
        commonBus.addHandler(AutoOpenEvent.TYPE, this);
        localBus.addHandler(ExpandHierarchyEvent.TYPE, this);
        localBus.addHandler(HierarchyActionEvent.TYPE, this);
        commonBus.addHandler(CancelSelectionEvent.TYPE, this);
        commonBus.addHandler(UpdateCollectionEvent.TYPE, this);
    }

    @Override
    protected void addRepresentationCells(Panel container) {

        grid = new FlexTable();
        grid.addStyleName(STYLE_REPRESENTATION_CELL);
        if (expandable) {
            grid.setWidget(0, 0, guiElementsFactory.buildExpandCell(commonBus, localBus, rowItem.getId(), getViewID(), getParentViewID()));
            expandButton = grid.getWidget(0, 0);
        }

        renderData();

        container.add(grid);
    }

    private void renderData() {
        int columnIndex = 1;

        for (CollectionColumnConfig column : collectionViewConfig.getCollectionDisplayConfig().getColumnConfig()) {
            InlineHTML fieldName = new InlineHTML("<b>" + column.getName() + "</b>");
            fieldName.addStyleName(STYLE_FIELD_NAME);
            grid.setWidget(0, columnIndex, fieldName);
            InlineHTML fieldValue = new InlineHTML(rowItem.getRow().get(column.getName()).toString());
            fieldValue.addStyleName(STYLE_FIELD_VALUE);
            grid.setWidget(0, columnIndex + 1, fieldValue);
            columnIndex += 2;
        }
    }

    @Override
    public void onHierarchyActionEvent(HierarchyActionEvent event) {

    }

    @Override
    public void onCancelSelectionEvent(CancelSelectionEvent event) {
        if (!event.getRowId().equals(rowItem.getId())) {
            headerPanel.removeStyleName(STYLE_FOCUS_WRAP_CELL);
        }
    }

    @Override
    public void updateCollection(UpdateCollectionEvent event) {
        if (event.getIdentifiableObject().getId().equals(rowItem.getId())) {
            final HierarchyPluginData[] pData = {new HierarchyPluginData()};
            pData[0].setRequestedItemId(rowItem.getId());
            pData[0].setCollectionViewConfig(collectionViewConfig);
            Command command = new Command(GET_ITEM_INFO_METHOD_NAME, PLUGIN_COMPONENT_NAME, pData[0]);
            BusinessUniverseServiceAsync.Impl.executeCommand(command, new AsyncCallback<Dto>() {
                @Override
                public void onFailure(Throwable caught) {
                    GWT.log("Something was going wrong while obtaining data for row with ID " + rowItem.getId());
                    caught.printStackTrace();
                }

                @Override
                public void onSuccess(Dto result) {
                    pData[0] = (HierarchyPluginData) result;
                    if (pData[0].getCollectionRowItems().size() > 0) {
                        rowItem = pData[0].getCollectionRowItems().get(0);
                        renderData();
                    }
                }
            });
        }
    }
}
