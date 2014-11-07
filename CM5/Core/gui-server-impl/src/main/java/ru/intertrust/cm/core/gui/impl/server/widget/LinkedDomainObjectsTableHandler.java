package ru.intertrust.cm.core.gui.impl.server.widget;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.business.api.dto.form.PopupTitlesHolder;
import ru.intertrust.cm.core.config.gui.form.FormConfig;
import ru.intertrust.cm.core.config.gui.form.widget.*;
import ru.intertrust.cm.core.config.gui.form.widget.filter.SelectionFiltersConfig;
import ru.intertrust.cm.core.config.gui.navigation.CollectionRefConfig;
import ru.intertrust.cm.core.gui.api.server.plugin.FilterBuilder;
import ru.intertrust.cm.core.gui.api.server.widget.FormatHandler;
import ru.intertrust.cm.core.gui.api.server.widget.LinkEditingWidgetHandler;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.impl.server.form.FormSaver;
import ru.intertrust.cm.core.gui.impl.server.util.*;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.FieldPath;
import ru.intertrust.cm.core.gui.model.form.FormObjects;
import ru.intertrust.cm.core.gui.model.form.FormState;
import ru.intertrust.cm.core.gui.model.form.widget.*;
import ru.intertrust.cm.core.gui.model.util.WidgetUtil;
import ru.intertrust.cm.core.gui.model.validation.ValidationException;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ComponentName("linked-domain-objects-table")
public class LinkedDomainObjectsTableHandler extends LinkEditingWidgetHandler {
    @Autowired
    CrudService crudService;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    CollectionsService collectionsService;

    @Autowired
    private FilterBuilder filterBuilder;

    @Override
    public LinkedDomainObjectsTableState getInitialState(WidgetContext context) {

        LinkedDomainObjectsTableState state = new LinkedDomainObjectsTableState();
        LinkedDomainObjectsTableConfig domainObjectsTableConfig = context.getWidgetConfig();
        state.setLinkedDomainObjectTableConfig(domainObjectsTableConfig);
        SingleChoiceConfig singleChoiceConfig = domainObjectsTableConfig.getSingleChoiceConfig();
        Boolean singleChoiceFromConfig = singleChoiceConfig == null ? false : singleChoiceConfig.isSingleChoice();
        boolean singleChoice = isSingleChoice(context, singleChoiceFromConfig);
        state.setSingleChoice(singleChoice);
        ArrayList<Id> ids = context.getAllObjectIds();
        DomainObject domainObject = context.getFormObjects().getRootNode().getDomainObject();
        PopupTitlesHolder popupTitlesHolder = titleBuilder.buildPopupTitles(domainObjectsTableConfig
                .getLinkedFormConfig(), domainObject);
        state.setPopupTitlesHolder(popupTitlesHolder);
        state.setIds(ids);

        String linkedFormName = domainObjectsTableConfig.getLinkedFormConfig().getName();
        if (linkedFormName != null && !linkedFormName.isEmpty()) {
            FormConfig formConfig = configurationService.getConfig(FormConfig.class, linkedFormName);
            state.setObjectTypeName(formConfig.getDomainObjectType());
        }
        SelectionFiltersConfig selectionFiltersConfig = domainObjectsTableConfig.getSelectionFiltersConfig();
        CollectionRefConfig refConfig = domainObjectsTableConfig.getCollectionRefConfig();
        boolean collectionNameConfigured = refConfig != null;

        List<RowItem> rowItems = selectionFiltersConfig == null || !collectionNameConfigured ? generateRowItems(domainObjectsTableConfig, ids)
                : generateFilteredRowItems(domainObjectsTableConfig, ids, false);
        state.setRowItems(rowItems);

        return state;
    }


    private List<RowItem> generateFilteredRowItems(LinkedDomainObjectsTableConfig widgetConfig,
                                                   List<Id> selectedIds, boolean tooltipContent) {
        SelectionFiltersConfig selectionFiltersConfig = widgetConfig.getSelectionFiltersConfig();
        List<Filter> filters = new ArrayList<>();
        filterBuilder.prepareSelectionFilters(selectionFiltersConfig, null, filters);
        Filter includedIds = FilterBuilderUtil.prepareFilter(new HashSet<Id>(selectedIds), FilterBuilderUtil.INCLUDED_IDS_FILTER);
        filters.add(includedIds);

        String collectionName = widgetConfig.getCollectionRefConfig().getName();
        Integer limit = WidgetUtil.getLimit(selectionFiltersConfig);
        SortOrder sortOrder = SortOrderBuilder.getSelectionSortOrder(widgetConfig.getSelectionSortCriteriaConfig());
        IdentifiableObjectCollection collection = null;
        if (limit == -1) {
            collection = collectionsService.findCollection(collectionName, sortOrder, filters);

        } else {
            collection = tooltipContent
                    ? collectionsService.findCollection(collectionName, sortOrder, filters, limit, WidgetConstants.UNBOUNDED_LIMIT)
                    : collectionsService.findCollection(collectionName, sortOrder, filters, 0, limit);
        }
        List<Id> selectedFilteredIds = new ArrayList<>();
        for (IdentifiableObject object : collection) {
            selectedFilteredIds.add(object.getId());
        }
        List<RowItem> items = generateRowItems(widgetConfig, selectedFilteredIds);
        return items;
    }

    private List<RowItem> generateRowItems(LinkedDomainObjectsTableConfig widgetConfig,
                                           List<Id> selectedIds) {
        List<RowItem> rowItems = new ArrayList<>();
        if (selectedIds.isEmpty()) {
            return rowItems;
        }
        List<DomainObject> domainObjects = crudService.find(selectedIds);
        DomainObjectsSorter.sort(widgetConfig.getSelectionSortCriteriaConfig(), domainObjects);
        List<SummaryTableColumnConfig> summaryTableColumnConfigs = widgetConfig
                .getSummaryTableConfig().getSummaryTableColumnConfigList();

        for (DomainObject domainObject : domainObjects) {
            RowItem rowItem;
            rowItem = map(domainObject, summaryTableColumnConfigs);
            rowItem.setObjectId(domainObject.getId());
            rowItems.add(rowItem);
        }
        return rowItems;
    }

    @Override
    public List<DomainObject> saveNewObjects(WidgetContext context, WidgetState state) {
        LinkedDomainObjectsTableState linkedDomainObjectsTableState = (LinkedDomainObjectsTableState) state;
        final FormObjects formObjects = context.getFormObjects();
        DomainObject rootDomainObject = formObjects.getRootNode().getDomainObject();
        LinkedHashMap<String, FormState> newFormStates = linkedDomainObjectsTableState.getNewFormStates();
        Set<Map.Entry<String, FormState>> entries = newFormStates.entrySet();

        LinkedDomainObjectsTableConfig linkedDomainObjectsTableConfig = linkedDomainObjectsTableState.getLinkedDomainObjectsTableConfig();
        FieldPath fieldPath = new FieldPath(linkedDomainObjectsTableConfig.getFieldPathConfig().getValue());
        ArrayList<DomainObject> newObjects = new ArrayList<>(entries.size());
        for (Map.Entry<String, FormState> entry : entries) {

            FormState formState = entry.getValue();
            DomainObject savedObject = null;

            List<String> validationResult = PluginHandlerHelper.doServerSideValidation(formState, applicationContext);
            if (!validationResult.isEmpty()) {
                throw new ValidationException("Server-side validation failed", validationResult);
            }
            FieldPath[] paths = FieldPath.createPaths(fieldPath.getPath());
            for (FieldPath path : paths) {
                String rootDomainObjectType = formState.getRootDomainObjectType();
                if (path.isOneToManyReference()) {
                    if (path.getLinkedObjectType().equalsIgnoreCase(rootDomainObjectType)) {
                        final HashMap<FieldPath, Value> rootObjectValues = new HashMap<>();
                        rootObjectValues.put(new FieldPath(path.getLinkToParentName()), new ReferenceValue(rootDomainObject.getId()));
                        FormSaver formSaver = getFormSaver(formState, rootObjectValues);
                        savedObject = formSaver.saveForm();
                    }
                } else if (path.isManyToManyReference()) {
                    if (path.getLinkToChildrenName().equalsIgnoreCase(rootDomainObjectType)) {
                        FormSaver formSaver = getFormSaver(formState, null);
                        savedObject = formSaver.saveForm();
                        String referenceType = path.getReferenceType();
                        DomainObject referencedObject = crudService.createDomainObject(referenceType);
                        referencedObject.setReference(path.getLinkToChildrenName(), savedObject);
                        referencedObject.setReference(path.getLinkToParentName(), rootDomainObject);
                        crudService.save(referencedObject);
                    }
                } else { // one-to-one reference
                    // todo: not-null constraint will fail!
                    FormSaver formSaver = getFormSaver(formState, null);
                    savedObject = formSaver.saveForm();
                    rootDomainObject.setReference(path.getFieldName(), savedObject);
                    crudService.save(rootDomainObject);
                }
                if (savedObject != null) {
                    newObjects.add(savedObject);
                }
            }
        }
        return newObjects;
    }

    public FormSaver getFormSaver(FormState formState, HashMap<FieldPath, Value> rootObjectValues) {
        final FormSaver formSaver = (FormSaver) applicationContext.getBean("formSaver");
        formSaver.setContext(formState, rootObjectValues);
        return formSaver;
    }

    @Override
    public boolean deleteEntriesOnLinkDrop(WidgetConfig config) {
        final Boolean deleteLinkedObjects = ((LinkedDomainObjectsTableConfig) config).isDeleteLinkedObjects();
        if (deleteLinkedObjects == Boolean.TRUE) {
            return true;
        } else if (deleteLinkedObjects == Boolean.FALSE) {
            return false;
        }
        // null - unknown behavior
        final FieldPathConfig fieldPathConfig = config.getFieldPathConfig();
        final FieldPathConfig.OnDeleteAction onRootDelete = fieldPathConfig.getOnRootDelete();
        if (onRootDelete == FieldPathConfig.OnDeleteAction.CASCADE) {
            return true;
        } else if (onRootDelete == FieldPathConfig.OnDeleteAction.UNLINK) {
            return false;
        }

        // when nothing is defined - delete objects in case it's a one-to-many reference - it's the common scenario
        return FieldPath.createPaths(fieldPathConfig.getValue())[0].isOneToManyReference();
    }

    public RowItem map(DomainObject domainObject, List<SummaryTableColumnConfig> summaryTableColumnConfigs) {
        RowItem rowItem = new RowItem();
        for (SummaryTableColumnConfig columnConfig : summaryTableColumnConfigs) {
            String displayValue;
            String valueGeneratorComponentName = columnConfig.getValueGeneratorComponent();
            if (valueGeneratorComponentName != null) {
                StringValueRenderer valueRenderer = (StringValueRenderer) applicationContext.getBean(valueGeneratorComponentName);
                displayValue = valueRenderer.render(domainObject);
            } else {
                LinkedTablePatternConfig patternConfig = findSuitablePatternForObjectType(columnConfig, domainObject.getTypeName());
                String columnPattern = patternConfig.getValue();
                FormattingConfig formattingConfig = columnConfig.getFormattingConfig();
                displayValue = formatHandler.format(domainObject, fieldPatternMatcher(columnPattern), formattingConfig);

            }
            rowItem.setValueByKey(columnConfig.getWidgetId(), displayValue);
        }
        return rowItem;
    }

    private LinkedTablePatternConfig findSuitablePatternForObjectType(SummaryTableColumnConfig columnConfig, String domainObjectType) {
        LinkedTablePatternConfig config = null;
        List<LinkedTablePatternConfig> patternConfigList = columnConfig.getPatternConfig();
        for (LinkedTablePatternConfig linkedTablePatternConfig : patternConfigList) {
            if (isPatternSuitableForType(domainObjectType, linkedTablePatternConfig)) {
                config = linkedTablePatternConfig;
                break;
            }
        }
        if (config == null) {
            config = findDefaultPattern(patternConfigList);
        }
        return config;
    }

    private LinkedTablePatternConfig findDefaultPattern(List<LinkedTablePatternConfig> patternConfigList) {
        for (LinkedTablePatternConfig config : patternConfigList) {
            if (config.getPatternDomainObjectTypesConfig() == null) {
                return config;
            }
        }
        return null;
    }

    private boolean isPatternSuitableForType(String domainObjectType, LinkedTablePatternConfig linkedTablePatternConfig) {
        PatternDomainObjectTypesConfig patternDomainObjectTypesConfig = linkedTablePatternConfig.getPatternDomainObjectTypesConfig();
        if (patternDomainObjectTypesConfig == null) {
            return false;
        }
        List<PatternDomainObjectTypeConfig> patternDomainObjectTypeConfigList = patternDomainObjectTypesConfig.getPatternDomainObjectTypeConfig();
        for (PatternDomainObjectTypeConfig patternDomainObjectTypeConfig : patternDomainObjectTypeConfigList) {
            if (patternDomainObjectTypeConfig.getName().equalsIgnoreCase(domainObjectType)) {
                return true;
            }
        }
        return false;
    }

    private Matcher fieldPatternMatcher(String pattern) {
        Pattern fieldPlaceholderPattern = Pattern.compile(FormatHandler.FIELD_PLACEHOLDER_PATTERN);
        return fieldPlaceholderPattern.matcher(pattern);
    }

    public Dto fetchWidgetItems(Dto inputParams) {
        LinkedTableTooltipRequest request = (LinkedTableTooltipRequest) inputParams;
        LinkedDomainObjectsTableConfig config = request.getConfig();
        List<Id> ids = request.getSelectedIds();

        SelectionFiltersConfig selectionFiltersConfig = config.getSelectionFiltersConfig();
        List<RowItem> rowItems = selectionFiltersConfig == null ? generateRowItems(config, ids)
                : generateFilteredRowItems(config, ids, true);

        return new LinkedTableTooltipResponse(rowItems);
    }

    public Dto convertFormStateToRowItem(Dto inputParams) {
        RepresentationRequest request = (RepresentationRequest) inputParams;
        FormState createdObjectState = request.getCreatedObjectState();
        SummaryTableConfig summaryTableConfig = request.getSummaryTableConfig();
        RowItem item = new RowItem();
        List<Id> requestIds = request.getIds();
        if (requestIds != null && !requestIds.isEmpty()) {
            item.setObjectId(requestIds.get(0));
        }
        for (SummaryTableColumnConfig summaryTableColumnConfig : summaryTableConfig.getSummaryTableColumnConfigList()) {
            String widgetId;
            widgetId = findWidgetIdFromMappings(summaryTableColumnConfig, request.getLinkedFormName());
            if (widgetId == null) {
                //default widget-id
                widgetId = summaryTableColumnConfig.getWidgetId();
            }
            WidgetState widgetState = createdObjectState.getFullWidgetsState().get(widgetId);
            StringBuilder representation = new StringBuilder();
            if (summaryTableColumnConfig.getValueGeneratorComponent() != null) {
                StringValueRenderer valueRenderer = (StringValueRenderer) applicationContext.getBean(summaryTableColumnConfig.getValueGeneratorComponent());
                representation.append(valueRenderer.render(createdObjectState));
                item.setValueByKey(widgetId, representation.toString());
            } else {
                String selectionPattern = findSuitablePatternForObjectType(summaryTableColumnConfig, createdObjectState.getRootDomainObjectType()).getValue();
                Matcher matcher = fieldPatternMatcher(selectionPattern);
                if (widgetState != null) {
                    FormattingConfig formattingConfig = summaryTableColumnConfig.getFormattingConfig();
                    if (widgetState instanceof TextState) {
                        TextState textBoxState = (TextState) widgetState;
                        String text = textBoxState.getText();
                        representation.append(formatHandler.format(new StringValue(text), matcher, formattingConfig));
                    } else if (widgetState instanceof IntegerBoxState) {
                        IntegerBoxState integerBoxState = (IntegerBoxState) widgetState;
                        Long number = integerBoxState.getNumber();
                        representation.append(formatHandler.format(new LongValue(number), matcher, formattingConfig));

                    } else if (widgetState instanceof DecimalBoxState) {
                        DecimalBoxState decimalBoxState = (DecimalBoxState) widgetState;
                        BigDecimal number = decimalBoxState.getNumber();
                        representation.append(formatHandler.format(new DecimalValue(number), matcher, formattingConfig));

                    } else if (widgetState instanceof CheckBoxState) {
                        CheckBoxState checkBoxState = (CheckBoxState) widgetState;
                        Boolean checked = checkBoxState.isSelected();
                        representation.append(formatHandler.format(new BooleanValue(checked), matcher, formattingConfig));

                    } else if (widgetState instanceof DateBoxState) {
                        DateBoxState dateBoxState = (DateBoxState) widgetState;
                        representation.append(formatHandler.format(dateBoxState, matcher, formattingConfig));

                    } else if (widgetState instanceof LinkEditingWidgetState && !(widgetState instanceof AttachmentBoxState)) {
                        LinkEditingWidgetState linkEditingWidgetState = (LinkEditingWidgetState) widgetState;
                        List<Id> ids = linkEditingWidgetState.getIds();
                        representation.append(formatHandler.format(selectionPattern, ids, formattingConfig));

                    }
                    item.setValueByKey(widgetId, representation.toString());
                }
            }

        }
        return item;
    }

    private String findWidgetIdFromMappings(SummaryTableColumnConfig summaryTableColumnConfig, String linkedFormName) {
        if (summaryTableColumnConfig.getWidgetIdMappingsConfig() != null) {
            for (WidgetIdMappingConfig widgetIdMappingConfig :
                    summaryTableColumnConfig.getWidgetIdMappingsConfig().getWidgetIdMappingConfigs()) {
                if (widgetIdMappingConfig.getLinkedFormName().equalsIgnoreCase(linkedFormName)) {
                    return widgetIdMappingConfig.getWidgetId();
                }
            }
        }
        return null;
    }


}

