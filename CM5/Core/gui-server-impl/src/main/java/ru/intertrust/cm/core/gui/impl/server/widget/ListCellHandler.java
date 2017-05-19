package ru.intertrust.cm.core.gui.impl.server.widget;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.config.gui.collection.view.CollectionColumnConfig;
import ru.intertrust.cm.core.config.gui.collection.view.CollectionViewConfig;
import ru.intertrust.cm.core.config.gui.form.widget.ListCellConfig;
import ru.intertrust.cm.core.config.gui.navigation.InitialFiltersConfig;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.gui.api.server.GuiContext;
import ru.intertrust.cm.core.gui.api.server.plugin.FilterBuilder;
import ru.intertrust.cm.core.gui.api.server.widget.SelfManagingWidgetHandler;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetHandler;
import ru.intertrust.cm.core.gui.impl.server.util.PluginHandlerHelper;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.filters.ComplexFiltersParams;
import ru.intertrust.cm.core.gui.model.form.widget.ListCellState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;
import ru.intertrust.cm.core.gui.model.plugin.collection.CollectionRowItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Ravil on 18.05.2017.
 */
@ComponentName("list-cell")
public class ListCellHandler extends WidgetHandler implements SelfManagingWidgetHandler {
    private ListCellState widgetState;
    private String cName;
    private String vName;
    private List<Filter> filters;
    private SortOrder sortOrder;


    @Autowired
    private FilterBuilder filterBuilder;

    @Autowired
    private CurrentUserAccessor currentUserAccessor;

    @Autowired
    private CollectionsService collectionsService;

    @Override
    public ListCellState getInitialState(WidgetContext context) {
        ListCellConfig config =  context.getWidgetConfig();
        widgetState = new ListCellState();
        widgetState.setHeaderValue(config.getHeaderValue());
        widgetState.setCounterRequired((config.getCounterRequired()!=null)?config.getCounterRequired():false);
        widgetState.setRenderComponentName(config.getRenderFactoryComponentName());
        widgetState.setRenderComponentType(config.getRenderTypeName());

        initCollectionConfig(config);
        widgetState.setItems(getItems());
        return widgetState;
    }

    @Override
    public Value getValue(WidgetState state) {
        return null;
    }

    private void initCollectionConfig(ListCellConfig widgetConfig){
        cName = widgetConfig.getCollectionRefConfig().getName();
        vName = widgetConfig.getCollectionViewRefConfig().getName();

        filters = new ArrayList<>();
        if (widgetConfig.getCollectionExtraFiltersConfig() != null) {
            filterBuilder.prepareExtraFilters(widgetConfig.getCollectionExtraFiltersConfig(), new ComplexFiltersParams(), filters);
        }

        sortOrder = new SortOrder();
        if(widgetConfig.getDefaultSortCriteriaConfig()!=null){
            sortOrder.add(new SortCriterion(widgetConfig.getDefaultSortCriteriaConfig().getColumnField(),
                    widgetConfig.getDefaultSortCriteriaConfig().getOrder()));
        }
    }

    private List<CollectionRowItem> getItems(){
        ArrayList<CollectionRowItem> items = new ArrayList<CollectionRowItem>();
        IdentifiableObjectCollection collection = collectionsService.findCollection(
                cName,
                sortOrder,
                filters,
                0,
                0);
        CollectionViewConfig collectionViewConfig = getCollectionViewConfig(cName, vName);

        for (IdentifiableObject iObject : collection) {
            CollectionRowItem aRow = new CollectionRowItem();
            aRow.setId(iObject.getId());
            aRow.setRow(new HashMap<String, Value>());
            if (collectionViewConfig == null) {
                for (String fieldName : iObject.getFields()) {
                    aRow.getRow().put(fieldName, iObject.getValue(fieldName));
                }
            } else {
                for (CollectionColumnConfig column : collectionViewConfig.getCollectionDisplayConfig().getColumnConfig()) {
                    aRow.getRow().put(column.getName(), iObject.getValue(column.getField()));
                }
            }

            items.add(aRow);
        }
        return items;
    }

    private CollectionViewConfig getCollectionViewConfig(String cName, String vName) {
        CollectionViewConfig viewConfig = PluginHandlerHelper.findCollectionViewConfig(cName, vName,
                currentUserAccessor.getCurrentUser(),
                null, configurationService, collectionsService, GuiContext.getUserLocale());
        return viewConfig;
    }
}
