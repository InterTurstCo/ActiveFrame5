package ru.intertrust.cm.core.gui.impl.server.widget;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.ConfigurationService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.config.gui.form.widget.FormattingConfig;
import ru.intertrust.cm.core.config.gui.form.widget.LinkedDomainObjectHyperlinkConfig;
import ru.intertrust.cm.core.config.gui.form.widget.filter.SelectionFiltersConfig;
import ru.intertrust.cm.core.gui.api.server.widget.FormatHandler;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetHandler;
import ru.intertrust.cm.core.gui.impl.server.util.FilterBuilder;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.HyperlinkItem;
import ru.intertrust.cm.core.gui.model.form.widget.LinkedDomainObjectHyperlinkState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginConfig;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 14.01.14
 *         Time: 10:25
 */
@ComponentName("linked-domain-object-hyperlink")
public class LinkedDomainObjectHyperlinkHandler extends WidgetHandler {
    @Autowired
    ConfigurationService configurationService;

    @Autowired
    private CrudService crudService;

    @Autowired
    private CollectionsService collectionsService;

    @Override
    public LinkedDomainObjectHyperlinkState getInitialState(WidgetContext context) {
        LinkedDomainObjectHyperlinkConfig widgetConfig = context.getWidgetConfig();
        LinkedDomainObjectHyperlinkState state = new LinkedDomainObjectHyperlinkState();
        ArrayList<Id> selectedIds = context.getAllObjectIds();
        FormattingConfig formattingConfig = widgetConfig.getFormattingConfig();
        state.setFormattingConfig(formattingConfig);
        String selectionPattern = widgetConfig.getPatternConfig().getValue();
        state.setSelectionPattern(selectionPattern);
        if (!selectedIds.isEmpty()) {
            Id id = selectedIds.get(0);
            FormPluginConfig config = getFormPluginConfig(id);
            state.setConfig(config);
            DomainObject firstDomainObject = crudService.find(id);
            state.setDomainObjectType(firstDomainObject.getTypeName());
            SelectionFiltersConfig selectionFiltersConfig = widgetConfig.getSelectionFiltersConfig();
            List<HyperlinkItem> hyperlinkItems = selectionFiltersConfig == null ? generateHyperlinkItems(widgetConfig, selectedIds)
                  : generateFilteredHyperlinkItems(widgetConfig, selectedIds);
            state.setHyperlinkItems(hyperlinkItems);
        }
        return state;
    }

    private List<HyperlinkItem> generateHyperlinkItems(LinkedDomainObjectHyperlinkConfig widgetConfig, List<Id> selectedIds){
        List<HyperlinkItem> hyperlinkItems = new ArrayList<>();
        FormattingConfig formattingConfig = widgetConfig.getFormattingConfig();
        String selectionPattern = widgetConfig.getPatternConfig().getValue();
        List<DomainObject> domainObjects = crudService.find(selectedIds);
        for (DomainObject domainObject : domainObjects) {
            Id id = domainObject.getId();
            String representation = buildStringRepresentation(domainObject, selectionPattern, formattingConfig);
            HyperlinkItem hyperlinkItem = new HyperlinkItem(id, representation);
            hyperlinkItems.add(hyperlinkItem);
        }
        return hyperlinkItems;
    }

    private List<HyperlinkItem> generateFilteredHyperlinkItems(LinkedDomainObjectHyperlinkConfig widgetConfig,
                                                               List<Id> selectedIds){
        SelectionFiltersConfig selectionFiltersConfig = widgetConfig.getSelectionFiltersConfig();
        List<Filter> filters = new ArrayList<>();
        FilterBuilder.prepareSelectionFilters(selectionFiltersConfig, null, filters);
        Filter includedIds = FilterBuilder.prepareFilter(new HashSet<Id>(selectedIds), FilterBuilder.INCLUDED_IDS_FILTER);
        filters.add(includedIds);
        String collectionName = widgetConfig.getCollectionRefConfig().getName();
        IdentifiableObjectCollection collection = collectionsService.findCollection(collectionName, null ,filters);
        List<Id> selectedFilteredIds = new ArrayList<>();
        for (IdentifiableObject object : collection) {
            selectedFilteredIds.add(object.getId());
        }
        List<HyperlinkItem> hyperlinkItems = generateHyperlinkItems(widgetConfig, selectedFilteredIds);
        return hyperlinkItems;
    }

    private String buildStringRepresentation(DomainObject domainObject, String selectionPattern,
                                             FormattingConfig formattingConfig) {

        Matcher matcher = FormatHandler.pattern.matcher(selectionPattern);
        String representation = formatHandler.format(domainObject, matcher, formattingConfig);
        return representation;
    }

    private FormPluginConfig getFormPluginConfig(Id id) {
        FormPluginConfig formConfig = new FormPluginConfig();
        formConfig.getPluginState().setEditable(false);
        formConfig.setDomainObjectId(id);
        return formConfig;
    }


    @Override
    public Value getValue(WidgetState state) {
        return null;
    }
}
