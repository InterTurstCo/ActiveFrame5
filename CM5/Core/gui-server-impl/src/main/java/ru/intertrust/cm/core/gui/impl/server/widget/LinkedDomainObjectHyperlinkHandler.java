package ru.intertrust.cm.core.gui.impl.server.widget;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.ConfigurationService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.business.api.dto.form.PopupTitlesHolder;
import ru.intertrust.cm.core.config.gui.form.widget.FormattingConfig;
import ru.intertrust.cm.core.config.gui.form.widget.LinkedDomainObjectHyperlinkConfig;
import ru.intertrust.cm.core.config.gui.form.widget.PatternConfig;
import ru.intertrust.cm.core.config.gui.form.widget.filter.SelectionFiltersConfig;
import ru.intertrust.cm.core.config.gui.navigation.CollectionRefConfig;
import ru.intertrust.cm.core.gui.api.server.plugin.FilterBuilder;
import ru.intertrust.cm.core.gui.api.server.widget.FormatHandler;
import ru.intertrust.cm.core.gui.api.server.widget.TitleBuilder;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetHandler;
import ru.intertrust.cm.core.gui.impl.server.util.DomainObjectsSorter;
import ru.intertrust.cm.core.gui.impl.server.util.FilterBuilderUtil;
import ru.intertrust.cm.core.gui.impl.server.util.SortOrderBuilder;
import ru.intertrust.cm.core.gui.impl.server.util.WidgetConstants;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.LinkedDomainObjectHyperlinkState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetItemsRequest;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetItemsResponse;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginConfig;
import ru.intertrust.cm.core.gui.model.util.WidgetUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
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

    @Autowired
    private FilterBuilder filterBuilder;

    @Autowired
    protected TitleBuilder titleBuilder;

    @Override
    public LinkedDomainObjectHyperlinkState getInitialState(WidgetContext context) {
        LinkedDomainObjectHyperlinkConfig widgetConfig = context.getWidgetConfig();
        LinkedDomainObjectHyperlinkState state = new LinkedDomainObjectHyperlinkState();
        ArrayList<Id> selectedIds = context.getAllObjectIds();
        if (!selectedIds.isEmpty()) {
            Id id = selectedIds.get(0);
            FormPluginConfig config = getFormPluginConfig(id);
            state.setConfig(config);
            DomainObject firstDomainObject = crudService.find(id);
            state.setDomainObjectType(firstDomainObject.getTypeName());
            SelectionFiltersConfig selectionFiltersConfig = widgetConfig.getSelectionFiltersConfig();
            LinkedHashMap<Id, String> listValues = selectionFiltersConfig == null
                    ? generateHyperlinkItems(widgetConfig, selectedIds)
                    : generateFilteredHyperlinkItems(widgetConfig, selectedIds, false);
            state.setListValues(listValues);
        }
        state.setWidgetConfig(widgetConfig);

        if (context.getDefaultValues() != null) {
            List<Id> defaultIds = HandlerUtils.takeDefaultReferenceValues(context);
            state.setSelectedIds(new ArrayList<>(defaultIds));
        } else {
            state.setSelectedIds(selectedIds);
        }
        state.setDisplayingAsHyperlinks(true);
        DomainObject domainObject = context.getFormObjects().getRootNode().getDomainObject();
        PopupTitlesHolder popupTitlesHolder = titleBuilder.buildPopupTitles(widgetConfig.getLinkedFormConfig(), domainObject);
        state.setPopupTitlesHolder(popupTitlesHolder);
        return state;
    }

    private LinkedHashMap<Id, String> generateHyperlinkItems(LinkedDomainObjectHyperlinkConfig widgetConfig, List<Id> selectedIds) {
        LinkedHashMap<Id, String> listValues = new LinkedHashMap<>();
        FormattingConfig formattingConfig = widgetConfig.getFormattingConfig();
        String selectionPattern = widgetConfig.getPatternConfig().getValue();
        List<DomainObject> domainObjects = crudService.find(selectedIds);
        DomainObjectsSorter.sort(widgetConfig.getSelectionSortCriteriaConfig(), domainObjects);
        for (DomainObject domainObject : domainObjects) {
            Id id = domainObject.getId();
            String representation = buildStringRepresentation(domainObject, selectionPattern, formattingConfig);
            listValues.put(id, representation);
        }
        return listValues;
    }

    private LinkedHashMap<Id, String> generateFilteredHyperlinkItems(LinkedDomainObjectHyperlinkConfig widgetConfig,
                                                                     List<Id> selectedIds, boolean tooltipContent) {
        SelectionFiltersConfig selectionFiltersConfig = widgetConfig.getSelectionFiltersConfig();
        List<Filter> filters = new ArrayList<>();
        filterBuilder.prepareSelectionFilters(selectionFiltersConfig, null, filters);
        Filter includedIds = FilterBuilderUtil.prepareFilter(new HashSet<Id>(selectedIds), FilterBuilderUtil.INCLUDED_IDS_FILTER);
        filters.add(includedIds);
        String collectionName = widgetConfig.getCollectionRefConfig().getName();
        int limit = WidgetUtil.getLimit(selectionFiltersConfig);
        IdentifiableObjectCollection collection = null;
        SortOrder sortOrder = SortOrderBuilder.getSelectionSortOrder(widgetConfig.getSelectionSortCriteriaConfig());
        if(limit == -1) {
            collection = collectionsService.findCollection(collectionName, sortOrder, filters);
        } else {
            collection = tooltipContent
                    ? collectionsService.findCollection(collectionName, sortOrder, filters,limit, WidgetConstants.UNBOUNDED_LIMIT)
                    : collectionsService.findCollection(collectionName, sortOrder, filters, 0, limit);
        }
        List<Id> selectedFilteredIds = new ArrayList<>();
        for (IdentifiableObject object : collection) {
            selectedFilteredIds.add(object.getId());
        }
        LinkedHashMap<Id, String> listValues = generateHyperlinkItems(widgetConfig, selectedFilteredIds);
        return listValues;
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

    public WidgetItemsResponse fetchWidgetItems(Dto inputParams) {
        WidgetItemsRequest widgetItemsRequest = (WidgetItemsRequest) inputParams;
        LinkedDomainObjectHyperlinkConfig widgetConfig = new LinkedDomainObjectHyperlinkConfig();
        FormattingConfig formattingConfig = widgetItemsRequest.getFormattingConfig();
        widgetConfig.setFormattingConfig(formattingConfig);
        PatternConfig patternConfig = new PatternConfig();
        patternConfig.setValue(widgetItemsRequest.getSelectionPattern());
        widgetConfig.setPatternConfig(patternConfig);
        SelectionFiltersConfig selectionFiltersConfig = widgetItemsRequest.getSelectionFiltersConfig();
        widgetConfig.setSelectionFiltersConfig(selectionFiltersConfig);
        String collectionName = widgetItemsRequest.getCollectionName();
        CollectionRefConfig collectionRefConfig = new CollectionRefConfig();
        collectionRefConfig.setName(collectionName);
        widgetConfig.setCollectionRefConfig(collectionRefConfig);
        List<Id> selectedIds = widgetItemsRequest.getSelectedIds();
        LinkedHashMap<Id, String> listValues = generateFilteredHyperlinkItems(widgetConfig, selectedIds, true);
        WidgetItemsResponse response = new WidgetItemsResponse();
        response.setListValues(listValues);

        return response;
    }

}
