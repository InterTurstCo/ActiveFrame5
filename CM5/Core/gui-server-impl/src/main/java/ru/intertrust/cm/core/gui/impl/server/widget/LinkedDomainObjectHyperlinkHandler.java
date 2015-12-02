package ru.intertrust.cm.core.gui.impl.server.widget;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.access.AccessVerificationService;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.business.api.dto.form.PopupTitlesHolder;
import ru.intertrust.cm.core.business.api.util.ObjectCloner;
import ru.intertrust.cm.core.config.gui.form.widget.FillParentOnAddConfig;
import ru.intertrust.cm.core.config.gui.form.widget.FormattingConfig;
import ru.intertrust.cm.core.config.gui.form.widget.LinkedDomainObjectHyperlinkConfig;
import ru.intertrust.cm.core.config.gui.form.widget.PatternConfig;
import ru.intertrust.cm.core.config.gui.form.widget.filter.SelectionFiltersConfig;
import ru.intertrust.cm.core.config.gui.form.widget.linkediting.CreatedObjectConfig;
import ru.intertrust.cm.core.config.gui.form.widget.linkediting.CreatedObjectsConfig;
import ru.intertrust.cm.core.config.gui.form.widget.linkediting.LinkedFormMappingConfig;
import ru.intertrust.cm.core.config.gui.navigation.CollectionRefConfig;
import ru.intertrust.cm.core.gui.api.server.plugin.FilterBuilder;
import ru.intertrust.cm.core.gui.api.server.plugin.SortOrderHelper;
import ru.intertrust.cm.core.gui.api.server.widget.FormatHandler;
import ru.intertrust.cm.core.gui.api.server.widget.TitleBuilder;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetHandler;
import ru.intertrust.cm.core.gui.impl.server.util.DomainObjectsSorter;
import ru.intertrust.cm.core.gui.impl.server.util.FilterBuilderUtil;
import ru.intertrust.cm.core.gui.impl.server.util.WidgetConstants;
import ru.intertrust.cm.core.gui.impl.server.util.WidgetServerUtil;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.filters.ComplexFiltersParams;
import ru.intertrust.cm.core.gui.model.form.widget.*;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginConfig;
import ru.intertrust.cm.core.gui.model.util.WidgetUtil;

import java.util.*;
import java.util.regex.Matcher;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 14.01.14
 *         Time: 10:25
 */
@ComponentName("linked-domain-object-hyperlink")
public class LinkedDomainObjectHyperlinkHandler extends WidgetHandler {

    @Autowired
    private CrudService crudService;

    @Autowired
    private CollectionsService collectionsService;

    @Autowired
    private FilterBuilder filterBuilder;

    @Autowired
    private TitleBuilder titleBuilder;

    @Autowired
    private AccessVerificationService accessVerificationService;

    @Autowired
    protected SortOrderHelper sortOrderHelper;

    @Override
    public LinkedDomainObjectHyperlinkState getInitialState(WidgetContext context) {
        LinkedDomainObjectHyperlinkConfig widgetConfig = ObjectCloner.getInstance().cloneObject(context.getWidgetConfig(),
                LinkedDomainObjectHyperlinkConfig.class);
        LinkedDomainObjectHyperlinkState state = new LinkedDomainObjectHyperlinkState();
        ArrayList<Id> selectedIds = context.getAllObjectIds();
        Id rootId = context.getFormObjects().getRootNode().getDomainObject().getId();
        if (!selectedIds.isEmpty()) {
            Id id = selectedIds.get(0);
            FormPluginConfig config = getFormPluginConfig(id);
            state.setConfig(config);
            DomainObject firstDomainObject = crudService.find(id);
            state.setDomainObjectType(firstDomainObject.getTypeName());
            SelectionFiltersConfig selectionFiltersConfig = widgetConfig.getSelectionFiltersConfig();
            ComplexFiltersParams filtersParams = new ComplexFiltersParams(rootId);
            CollectionRefConfig refConfig = widgetConfig.getCollectionRefConfig();
            boolean collectionNameConfigured = refConfig != null;
            List<Id> idsForItemsGenerating = selectionFiltersConfig == null || !collectionNameConfigured ? selectedIds
                    : getNotLimitedIds(widgetConfig, selectedIds, filtersParams,false);
            state.setFilteredItemsNumber(idsForItemsGenerating.size());
            int limit = WidgetUtil.getLimit(selectionFiltersConfig);
            WidgetServerUtil.doLimit(idsForItemsGenerating, limit);
            LinkedHashMap<Id, String> listValues = generateHyperlinkItems(widgetConfig, idsForItemsGenerating);

            state.setListValues(listValues);
        }
        state.setWidgetConfig(widgetConfig);
        state.setSelectedIds(selectedIds);
        state.setDisplayingAsHyperlinks(true);
        DomainObject domainObject = context.getFormObjects().getRootNode().getDomainObject();
        fillTypeTitleMap(domainObject, widgetConfig.getLinkedFormMappingConfig(), state);
        abandonAccessed(domainObject, widgetConfig.getCreatedObjectsConfig(), null);
        PopupTitlesHolder popupTitlesHolder = titleBuilder.buildPopupTitles(widgetConfig.getLinkedFormConfig(), domainObject);
        state.setPopupTitlesHolder(popupTitlesHolder);
        return state;
    }

    private LinkedHashMap<Id, String> generateHyperlinkItems(LinkedDomainObjectHyperlinkConfig widgetConfig, List<Id> selectedIds) {
        LinkedHashMap<Id, String> listValues = new LinkedHashMap<>();
        FormattingConfig formattingConfig = widgetConfig.getFormattingConfig();
        String selectionPattern = widgetConfig.getPatternConfig().getValue();
        List<DomainObject> domainObjects = selectedIds.isEmpty() ?  new ArrayList<DomainObject>(0) : crudService.find(selectedIds);
        DomainObjectsSorter.sort(widgetConfig.getSelectionSortCriteriaConfig(), domainObjects);
        for (DomainObject domainObject : domainObjects) {
            Id id = domainObject.getId();
            String representation = buildStringRepresentation(domainObject, selectionPattern, formattingConfig);
            listValues.put(id, representation);
        }
        return listValues;
    }

    private List<Id> getNotLimitedIds(LinkedDomainObjectHyperlinkConfig widgetConfig,
                                                                     List<Id> selectedIds, ComplexFiltersParams filtersParams,
                                                                     boolean tooltipContent) {
        SelectionFiltersConfig selectionFiltersConfig = widgetConfig.getSelectionFiltersConfig();
        List<Filter> filters = new ArrayList<>();
        filterBuilder.prepareSelectionFilters(selectionFiltersConfig, filtersParams, filters);
        Filter includedIds = FilterBuilderUtil.prepareFilter(new HashSet<Id>(selectedIds), FilterBuilderUtil.INCLUDED_IDS_FILTER);
        filters.add(includedIds);
        String collectionName = widgetConfig.getCollectionRefConfig().getName();

        SortOrder sortOrder = sortOrderHelper.buildSortOrder(collectionName,widgetConfig.getSelectionSortCriteriaConfig());
        IdentifiableObjectCollection collection = null;
        if (tooltipContent) {
            int limit = WidgetUtil.getLimit(selectionFiltersConfig);
            collection = collectionsService.findCollection(collectionName, sortOrder, filters, limit, WidgetConstants.UNBOUNDED_LIMIT);
        } else {
            collection = collectionsService.findCollection(collectionName, sortOrder, filters);
        }
        List<Id> selectedFilteredIds = new ArrayList<>();
        for (IdentifiableObject object : collection) {
            selectedFilteredIds.add(object.getId());
        }

        return selectedFilteredIds;
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
        ComplexFiltersParams filtersParams = widgetItemsRequest.getComplexFiltersParams();
        List<Id> idsForItemsGenerating = getNotLimitedIds(widgetConfig, selectedIds, filtersParams, true);
        int limit = WidgetUtil.getLimit(selectionFiltersConfig);
        WidgetServerUtil.doLimit(idsForItemsGenerating, limit);
        LinkedHashMap<Id, String> listValues = generateHyperlinkItems(widgetConfig, idsForItemsGenerating);
        WidgetItemsResponse response = new WidgetItemsResponse();
        response.setListValues(listValues);

        return response;
    }
    //TODO don't duplicate code, try to make LinkedDomainObjectHyperlink LinkEditing
    protected void fillTypeTitleMap(DomainObject root, LinkedFormMappingConfig mappingConfig, LinkCreatorWidgetState state){
        Map<String, PopupTitlesHolder> typeTitleMap = titleBuilder.buildTypeTitleMap(mappingConfig, root);
        state.setTypeTitleMap(typeTitleMap);
    }

    public boolean abandonAccessed(DomainObject root, CreatedObjectsConfig createdObjectsConfig,
                                   FillParentOnAddConfig fillParentOnAddConfig) {
        if (createdObjectsConfig != null) {
            List<CreatedObjectConfig> createdObjectConfigs = createdObjectsConfig.getCreateObjectConfigs();
            if (WidgetUtil.isNotEmpty(createdObjectConfigs)) {
                Iterator<CreatedObjectConfig> iterator = createdObjectConfigs.iterator();
                while (iterator.hasNext()) {
                    CreatedObjectConfig createdObjectConfig = iterator.next();
                    String domainObjectType = createdObjectConfig.getDomainObjectType();
                    boolean displayingCreateButton = fillParentOnAddConfig == null
                            ? accessVerificationService.isCreatePermitted(domainObjectType)
                            : accessVerificationService.isCreateChildPermitted(domainObjectType, root.getId());
                    if (!displayingCreateButton) {
                        iterator.remove();
                    }
                }
                if(!createdObjectConfigs.isEmpty()){
                    return  true;
                }
            }
        }
        return false;

    }

    public Dto getRepresentationForOneItem(Dto inputParams){
        return formatHandler.getRepresentationForOneItem(inputParams);
    }


}
