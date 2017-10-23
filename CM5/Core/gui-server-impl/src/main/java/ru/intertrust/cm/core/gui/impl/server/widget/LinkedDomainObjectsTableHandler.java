package ru.intertrust.cm.core.gui.impl.server.widget;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.util.CollectionUtils;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.PersonService;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.business.api.dto.form.PopupTitlesHolder;
import ru.intertrust.cm.core.business.api.util.ObjectCloner;
import ru.intertrust.cm.core.config.gui.form.FormConfig;
import ru.intertrust.cm.core.config.gui.form.widget.*;
import ru.intertrust.cm.core.config.gui.form.widget.filter.SelectionFiltersConfig;
import ru.intertrust.cm.core.config.gui.form.widget.linkediting.CreatedObjectConfig;
import ru.intertrust.cm.core.config.gui.form.widget.linkediting.CreatedObjectsConfig;
import ru.intertrust.cm.core.config.gui.form.widget.linkediting.LinkedFormMappingConfig;
import ru.intertrust.cm.core.config.gui.navigation.CollectionRefConfig;
import ru.intertrust.cm.core.gui.api.server.GuiContext;
import ru.intertrust.cm.core.gui.api.server.GuiService;
import ru.intertrust.cm.core.gui.api.server.form.FieldPathHelper;
import ru.intertrust.cm.core.gui.api.server.plugin.FilterBuilder;
import ru.intertrust.cm.core.gui.api.server.widget.FormatHandler;
import ru.intertrust.cm.core.gui.api.server.widget.LinkEditingWidgetHandler;
import ru.intertrust.cm.core.gui.api.server.widget.ValueEditingWidgetHandler;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.impl.server.action.access.AccessChecker;
import ru.intertrust.cm.core.gui.impl.server.form.FormResolver;
import ru.intertrust.cm.core.gui.impl.server.form.FormSaver;
import ru.intertrust.cm.core.gui.impl.server.util.*;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.filters.ComplexFiltersParams;
import ru.intertrust.cm.core.gui.model.form.FieldPath;
import ru.intertrust.cm.core.gui.model.form.FormState;
import ru.intertrust.cm.core.gui.model.form.widget.*;
import ru.intertrust.cm.core.gui.model.util.WidgetUtil;
import ru.intertrust.cm.core.gui.model.validation.ValidationException;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;

import static ru.intertrust.cm.core.gui.impl.server.widget.util.WidgetRepresentationUtil.getDisplayValue;

@ComponentName("linked-domain-objects-table")
public class LinkedDomainObjectsTableHandler extends LinkEditingWidgetHandler {
    private static Logger log = LoggerFactory.getLogger(LinkedDomainObjectsTableHandler.class);

    @Autowired
    protected CrudService crudService;

    @Autowired
    protected ApplicationContext applicationContext;

    @Autowired
    protected CollectionsService collectionsService;

    @Autowired
    protected FilterBuilder filterBuilder;

    @Autowired
    protected GuiService guiService;

    @Autowired
    protected FormResolver formResolver;

    @Autowired
    protected PersonService personService;

    @Autowired
    protected FieldPathHelper fieldPathHelper;

    private static final String DEFAULT_EDIT_ACCESS_CHECKER = "default.edit.access.checker";
    private static final String DEFAULT_DELETE_ACCESS_CHECKER = "default.delete.access.checker";
    private static final String DEFAULT_VIEW_ACCESS_CHECKER = "default.view.access.checker";
    private static final String DEFAULT_CREATE_NEW_ACCESS_CHECKER = "default.create.new.access.checker";

    @Override
    public LinkedDomainObjectsTableState getInitialState(WidgetContext context) {
        final LinkedDomainObjectsTableConfig widgetConfig = context.getWidgetConfig();
        final LinkedDomainObjectsTableState state = new LinkedDomainObjectsTableState();
        state.setLinkedDomainObjectTableConfig(widgetConfig);
        SingleChoiceConfig singleChoiceConfig = widgetConfig.getSingleChoiceConfig();
        Boolean singleChoiceFromConfig = singleChoiceConfig == null ? false : singleChoiceConfig.isSingleChoice();
        boolean singleChoice = isSingleChoice(context, singleChoiceFromConfig);
        state.setSingleChoice(singleChoice);
        ArrayList<Id> ids = context.getAllObjectIds();
        DomainObject root = context.getFormObjects().getRootNode().getDomainObject();
        Map<String, PopupTitlesHolder> typeTitleMap = titleBuilder.buildTypeTitleMap(widgetConfig.getLinkedFormMappingConfig(), root);
        state.setTypeTitleMap(typeTitleMap);
        PopupTitlesHolder popupTitlesHolder = titleBuilder.buildPopupTitles(widgetConfig
                .getLinkedFormConfig(), root);
        state.setPopupTitlesHolder(popupTitlesHolder);
        state.setIds(ids);

        SelectionFiltersConfig selectionFiltersConfig = widgetConfig.getSelectionFiltersConfig();
        CollectionRefConfig refConfig = widgetConfig.getCollectionRefConfig();
        boolean collectionNameConfigured = refConfig != null;
        ComplexFiltersParams filtersParams = new ComplexFiltersParams(root.getId());
        List<Id> idsForItemsGenerating = selectionFiltersConfig == null || !collectionNameConfigured ? ids
                : getNotLimitedIds(widgetConfig, ids, filtersParams, false);
        state.setFilteredItemsNumber(idsForItemsGenerating.size());
        int limit = WidgetUtil.getLimit(selectionFiltersConfig);
        WidgetServerUtil.doLimit(idsForItemsGenerating, limit);
        CreatedObjectsConfig restrictedCreatedObjectsConfig = createRestrictedCreateObjectsConfig(root, widgetConfig);
        state.setRestrictedCreatedObjectsConfig(restrictedCreatedObjectsConfig);
        List<RowItem> rowItems = generateRowItems(widgetConfig, idsForItemsGenerating, state.hasAllowedCreationDoTypes(), new Cache(personService.getCurrentPersonUid()));
        state.setRowItems(rowItems);
        state.setParentWidgetIdsForNewFormMap(createParentWidgetIdsForNewFormMap(widgetConfig,
                context.getWidgetConfigsById().values()));

        return state;
    }

    protected CreatedObjectsConfig createRestrictedCreateObjectsConfig(DomainObject root, LinkedDomainObjectsTableConfig widgetConfig) {
        CreatedObjectsConfig restrictedCreatedObjectsConfig = null;
        if (widgetConfig.getCreatedObjectsConfig() != null) {
            restrictedCreatedObjectsConfig = ObjectCloner.getInstance().cloneObject(widgetConfig.getCreatedObjectsConfig(),
                    CreatedObjectsConfig.class);
            abandonAccessed(root, restrictedCreatedObjectsConfig, null);
        } else {
            restrictedCreatedObjectsConfig = new CreatedObjectsConfig();
            List<CreatedObjectConfig> createdObjectConfigs = new ArrayList<CreatedObjectConfig>(1);
            restrictedCreatedObjectsConfig.setCreateObjectConfigs(createdObjectConfigs);
            String linkedFormName = widgetConfig.getLinkedFormConfig().getName();
            if (linkedFormName != null && !linkedFormName.isEmpty()) {
                FormConfig defaultFormConfig = configurationService.getConfig(FormConfig.class, linkedFormName);
                String domainObjectType = defaultFormConfig.getDomainObjectType();
                if(accessVerificationService.isCreatePermitted(domainObjectType)){
                    CreatedObjectConfig createdObjectConfig = new CreatedObjectConfig();
                    createdObjectConfig.setDomainObjectType(domainObjectType);
                    createdObjectConfig.setText(domainObjectType);
                    createdObjectConfigs.add(createdObjectConfig);
                }
            }
        }
        return restrictedCreatedObjectsConfig;
    }


    protected List<Id> getNotLimitedIds(LinkedDomainObjectsTableConfig widgetConfig,
                                      List<Id> selectedIds, ComplexFiltersParams filtersParams, boolean tooltipContent) {
        SelectionFiltersConfig selectionFiltersConfig = widgetConfig.getSelectionFiltersConfig();
        List<Filter> filters = new ArrayList<>();
        filterBuilder.prepareSelectionFilters(selectionFiltersConfig, filtersParams, filters);
        Filter includedIds = FilterBuilderUtil.prepareFilter(new HashSet<Id>(selectedIds), FilterBuilderUtil.INCLUDED_IDS_FILTER);
        filters.add(includedIds);

        String collectionName = widgetConfig.getCollectionRefConfig().getName();
        SortOrder sortOrder = sortOrderHelper.buildSortOrder(collectionName, widgetConfig.getSelectionSortCriteriaConfig());
        IdentifiableObjectCollection collection = null;
        if (tooltipContent) {
            int limit = WidgetUtil.getLimit(selectionFiltersConfig);
            collection = collectionsService.findCollection(collectionName, sortOrder, filters, limit, WidgetConstants.UNBOUNDED_LIMIT);
        } else {
            collection = collectionsService.findCollection(collectionName, sortOrder, filters);
        }
        List<Id> selectedFilteredIds = new ArrayList<>();
        for (IdentifiableObject object : collection) {
            selectedFilteredIds.add(object.getId());// todo: in many to many - NOT ID!
        }
        return selectedFilteredIds;
    }

    protected List<RowItem> generateRowItems(LinkedDomainObjectsTableConfig widgetConfig,
                                           List<Id> selectedIds, boolean hasAllowedCreationDoTypes, Cache cache) {
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
            rowItem = map(domainObject, widgetConfig, summaryTableColumnConfigs, hasAllowedCreationDoTypes, cache);
            rowItem.setObjectId(domainObject.getId());
            rowItems.add(rowItem);
        }
        return rowItems;
    }

    @Override
    public List<DomainObject> saveNewObjects(WidgetContext context, WidgetState state) {
        DomainObject rootDomainObject = context.getFormObjects().getRootNode().getDomainObject();
        Collection<FormState> formStates = ((LinkedDomainObjectsTableState) state).getNewFormStates().values();

        ArrayList<DomainObject> newObjects = new ArrayList<>(formStates.size());
        for (FormState formState : formStates) {
            DomainObject savedObject = null;
            String savedObjectType = formState.getRootDomainObjectType();

            List<String> validationResult = PluginHandlerHelper.doServerSideValidation(formState, applicationContext,
                    GuiContext.getUserLocale());
            if (!validationResult.isEmpty()) {
                throw new ValidationException("Server-side validation failed", validationResult);
            }
            FieldPath[] paths = context.getFieldPaths();
            for (FieldPath path : paths) {
                if (!fieldPathHelper.typeMatchesFieldPath(savedObjectType, rootDomainObject.getTypeName(), path, false)) {
                    continue;
                }
                if (path.isOneToManyReference()) {
                    final HashMap<FieldPath, Value> rootObjectValues = new HashMap<>();
                    rootObjectValues.put(new FieldPath(path.getLinkToParentName()), new ReferenceValue(rootDomainObject.getId()));
                    FormSaver formSaver = getFormSaver(formState, rootObjectValues);
                    savedObject = formSaver.saveForm();
                } else if (path.isManyToManyReference()) {
                    FormSaver formSaver = getFormSaver(formState, null);
                    savedObject = formSaver.saveForm();
                    String referenceType = path.getReferenceType();
                    DomainObject referencedObject = crudService.createDomainObject(referenceType);
                    referencedObject.setReference(path.getLinkToChildrenName(), savedObject);
                    referencedObject.setReference(path.getLinkToParentName(), rootDomainObject);
                    crudService.save(referencedObject);
                } else { // one-to-one reference
                    FormSaver formSaver = getFormSaver(formState, null);
                    savedObject = formSaver.saveForm();
                    rootDomainObject.setReference(path.getFieldName(), savedObject);
                    crudService.save(rootDomainObject);
                }
                if (savedObject != null) {
                    newObjects.add(savedObject);
                    break; // same object can not be saved to different field-paths
                }
            }
            if (savedObject == null) {
                log.error("Object not saved. Type: " + savedObjectType + " doesn't match field-path: " + context.getFieldPathConfig().getValue());
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

    // Column -> Pattern -> Domain Object Type
    public RowItem map(DomainObject domainObject, LinkedDomainObjectsTableConfig widgetConfig, List<SummaryTableColumnConfig> summaryTableColumnConfigs,
                       boolean hasAllowedCreationDoTypes, Cache cache) {
        RowItem rowItem = new RowItem();
        rowItem.setDomainObjectType(domainObject.getTypeName());
        Map<String, Boolean> rowAccessMatrix = new HashMap<>();
        AccessChecker defaultAccessChecker = (AccessChecker) applicationContext.getBean(DEFAULT_EDIT_ACCESS_CHECKER);
        rowAccessMatrix.put(DEFAULT_EDIT_ACCESS_CHECKER, defaultAccessChecker.checkAccess(domainObject.getId()));
        defaultAccessChecker = (AccessChecker) applicationContext.getBean(DEFAULT_DELETE_ACCESS_CHECKER);
        rowAccessMatrix.put(DEFAULT_DELETE_ACCESS_CHECKER, defaultAccessChecker.checkAccess(domainObject.getId()));
        defaultAccessChecker = (AccessChecker) applicationContext.getBean(DEFAULT_VIEW_ACCESS_CHECKER);
        rowAccessMatrix.put(DEFAULT_VIEW_ACCESS_CHECKER, defaultAccessChecker.checkAccess(domainObject.getId()));
        rowAccessMatrix.put(DEFAULT_CREATE_NEW_ACCESS_CHECKER, hasAllowedCreationDoTypes);

       if(widgetConfig.getSummaryTableConfig().getSummaryTableActionsColumnConfig()!=null){
           for(SummaryTableActionColumnConfig saConfig : widgetConfig.getSummaryTableConfig().
                   getSummaryTableActionsColumnConfig().
                   getSummaryTableActionColumnConfig()){
               if(saConfig.getAccessChecker()!=null){
                   String accessCheckerComponent = saConfig.getAccessChecker();
                   if (applicationContext.containsBeanDefinition(accessCheckerComponent)){
                       AccessChecker accessChecker = (AccessChecker) applicationContext.getBean(accessCheckerComponent);
                       if (accessChecker != null) {
                           Boolean result = accessChecker.checkAccess(domainObject.getId());
                           if("delete".equals(saConfig.getType())){
                               Boolean summarizedAccess = result&&rowAccessMatrix.get(DEFAULT_DELETE_ACCESS_CHECKER);
                               rowAccessMatrix.put(DEFAULT_DELETE_ACCESS_CHECKER,summarizedAccess);
                           }
                           if("edit".equals(saConfig.getType())){
                               Boolean summarizedAccess = result&&rowAccessMatrix.get(DEFAULT_EDIT_ACCESS_CHECKER);
                               rowAccessMatrix.put(DEFAULT_EDIT_ACCESS_CHECKER,summarizedAccess);
                           }

                       }
                   }
               }
           }
       }

        HashMap<String, EnumBoxConfig> enumBoxConfigsByFieldPath = getEnumBoxConfigs(domainObject.getTypeName(), widgetConfig, cache);
        for (SummaryTableColumnConfig columnConfig : summaryTableColumnConfigs) {
            SummaryTableActionColumnConfig summaryTableActionColumnConfig = columnConfig.getSummaryTableActionColumnConfig();
            if (summaryTableActionColumnConfig != null) {
                String accessCheckerComponent = summaryTableActionColumnConfig.getAccessChecker();
                if (accessCheckerComponent != null) {
                    if (applicationContext.containsBeanDefinition(accessCheckerComponent)) {
                        AccessChecker accessChecker = (AccessChecker) applicationContext.getBean(accessCheckerComponent);
                        if (accessChecker != null) {
                            rowAccessMatrix.put(summaryTableActionColumnConfig.getComponentName(),
                                    accessChecker.checkAccess(domainObject.getId()));
                        }
                    }

                }
            }

            String displayValue;
            String valueGeneratorComponentName = columnConfig.getValueGeneratorComponent();
            if (valueGeneratorComponentName != null) {
                StringValueRenderer valueRenderer = (StringValueRenderer) applicationContext.getBean(valueGeneratorComponentName);
                displayValue = valueRenderer.render(domainObject);
            } else {
                LinkedTablePatternConfig patternConfig = findSuitablePatternForObjectType(columnConfig, domainObject.getTypeName());
                String columnPattern = patternConfig.getValue();
                FormattingConfig formattingConfig = columnConfig.getFormattingConfig();
                displayValue = formatHandler.format(domainObject, fieldPatternMatcher(columnPattern), formattingConfig, enumBoxConfigsByFieldPath);
            }
            rowItem.setValueByKey(columnConfig.getColumnId(), displayValue);
            rowItem.setAccessMatrix(rowAccessMatrix);
        }
        return rowItem;
    }

    protected HashMap<String, EnumBoxConfig> getEnumBoxConfigs(String domainObjectType, LinkedDomainObjectsTableConfig widgetConfig, Cache cache) {
        HashMap<String, EnumBoxConfig> result = cache.enumBoxConfigsByDomainObjectType.get(domainObjectType);
        if (result != null) {
            return result;
        }

        FormConfig formConfig = getFormConfig(domainObjectType, widgetConfig, cache);
        HashMap<String, EnumBoxConfig> configs = new HashMap<>();
        for (WidgetConfig config : formConfig.getWidgetConfigsById().values()) {
            if (config instanceof EnumBoxConfig) {
                configs.put(config.getFieldPathConfig().getValue(), (EnumBoxConfig) config);
            }
        }
        cache.enumBoxConfigsByDomainObjectType.put(domainObjectType, configs);
        return configs;
    }

    protected FormConfig getFormConfig(String domainObjectType, LinkedDomainObjectsTableConfig widgetConfig, Cache cache) {
        final FormConfig formConfig = cache.formConfigByDomainObjectType.get(domainObjectType);
        if (formConfig != null) {
            return formConfig;
        }
        String formName = null;
        final LinkedFormMappingConfig linkedFormMappingConfig = widgetConfig.getLinkedFormMappingConfig();
        if (linkedFormMappingConfig != null && linkedFormMappingConfig.getLinkedFormConfigs() != null) {
            final List<LinkedFormConfig> linkedFormConfigs = linkedFormMappingConfig.getLinkedFormConfigs();
            for (LinkedFormConfig linkedFormConfig : linkedFormConfigs) {
                if (linkedFormConfig.getDomainObjectType().equals(domainObjectType)) {
                    formName = linkedFormConfig.getName();
                    break;
                }
            }
        }
        FormConfig config;
        if (formName == null) {
            config = formResolver.findFormConfig(domainObjectType, FormConfig.TYPE_EDIT, cache.currentPersonUid);
        } else {
            config = configurationService.getConfig(FormConfig.class, formName);
        }
        cache.formConfigByDomainObjectType.put(domainObjectType, config);
        return config;
    }

    protected LinkedTablePatternConfig findSuitablePatternForObjectType(SummaryTableColumnConfig columnConfig, String domainObjectType) {
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

    protected LinkedTablePatternConfig findDefaultPattern(List<LinkedTablePatternConfig> patternConfigList) {
        for (LinkedTablePatternConfig config : patternConfigList) {
            if (config.getPatternDomainObjectTypesConfig() == null) {
                return config;
            }
        }
        return null;
    }

    protected boolean isPatternSuitableForType(String domainObjectType, LinkedTablePatternConfig linkedTablePatternConfig) {
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

    protected Matcher fieldPatternMatcher(String pattern) {
        return FormatHandler.pattern.matcher(pattern);
    }

    public Dto fetchWidgetItems(Dto inputParams) {
        LinkedTableTooltipRequest request = (LinkedTableTooltipRequest) inputParams;
        LinkedDomainObjectsTableConfig config = request.getConfig();
        List<Id> ids = request.getSelectedIds();
        ComplexFiltersParams filtersParams = request.getFiltersParams();
        SelectionFiltersConfig selectionFiltersConfig = config.getSelectionFiltersConfig();
        CollectionRefConfig refConfig = config.getCollectionRefConfig();
        boolean collectionNameConfigured = refConfig != null;
        List<Id> idsForItemsGenerating = selectionFiltersConfig == null || !collectionNameConfigured ? ids
                : getNotLimitedIds(config, ids, filtersParams, true);
        CreatedObjectsConfig createdObjectsConfig = createRestrictedCreateObjectsConfig(null, config);
        boolean hasAllowedCreationDoTypes = !createdObjectsConfig.getCreateObjectConfigs().isEmpty();
        List<RowItem> rowItems = generateRowItems(config, idsForItemsGenerating, hasAllowedCreationDoTypes, new Cache(personService.getCurrentPersonUid()));

        return new LinkedTableTooltipResponse(rowItems);
    }

    public Dto convertFormStateToRowItem(Dto inputParams) {
        RepresentationRequest request = (RepresentationRequest) inputParams;
        FormState createdObjectState = request.getCreatedObjectState();
        SummaryTableConfig summaryTableConfig = request.getSummaryTableConfig();
        Id id = CollectionUtils.isEmpty(request.getIds()) ? null : request.getIds().get(0);
        return convertFormStateToRowItem(id, request.getRootId(), createdObjectState, summaryTableConfig);
    }

    public Dto convertFormStatesToRowItem(Dto inputParams) {
        RepresentationRequest request = (RepresentationRequest) inputParams;
        LinkedHashMap<String, FormState> createdObjectStates = request.getNewFormStates();
        SummaryTableConfig summaryTableConfig = request.getSummaryTableConfig();
        LinkedHashMap<String,RowItem> result = new LinkedHashMap<>(createdObjectStates.size());
        for (Map.Entry<String, FormState> entry : createdObjectStates.entrySet()) {
            RowItem rowItem = convertFormStateToRowItem(null, null, entry.getValue(), summaryTableConfig);
            result.put(entry.getKey(), rowItem);
        }
        return new RowItemsResponse(result);
    }

    protected RowItem convertFormStateToRowItem(Id objectId, Id rootId,FormState createdObjectState,
                                              SummaryTableConfig summaryTableConfig){
        RowItem item = new RowItem();
        item.setDomainObjectType(createdObjectState.getRootDomainObjectType());
        item.setObjectId(objectId);
        Map<String, WidgetState> fieldPathWidgetStateMap = createWidgetStateByFieldPath(createdObjectState.getName(),
                createdObjectState.getFullWidgetsState());
        for (SummaryTableColumnConfig summaryTableColumnConfig : summaryTableConfig.getSummaryTableColumnConfigList()) {
            String columnId = summaryTableColumnConfig.getColumnId();

            StringBuffer replacement = new StringBuffer();
            if (summaryTableColumnConfig.getValueGeneratorComponent() != null) {
                StringValueRenderer valueRenderer = (StringValueRenderer) applicationContext.getBean(summaryTableColumnConfig.getValueGeneratorComponent());
                replacement.append(valueRenderer.render(createdObjectState));
                item.setValueByKey(columnId, replacement.toString());
            } else {

                String selectionPattern = findSuitablePatternForObjectType(summaryTableColumnConfig,
                        createdObjectState.getRootDomainObjectType()).getValue();
                Matcher matcher = fieldPatternMatcher(selectionPattern);
                FormattingConfig formattingConfig = summaryTableColumnConfig.getFormattingConfig();
                while (matcher.find()) {
                    String group = matcher.group();

                    String fieldPathValue = group.substring(1, group.length() - 1);
                    /**
                     * 1) если виджета с field-path == main_street или с field-path == main_street.name на форме нет,
                     * то берём из базы, как обычно (если объект-город уже существует)
                     * 2) если есть виджет с field-path == main_street, то значение ссылки вытаскиваем из виджета, а main_street - из базы
                     * 3) если есть виджет с field-path == main_street.name, то значение вытаскиваем из виджета
                     */
                    WidgetState widgetState = fieldPathWidgetStateMap.get(fieldPathValue);
                    String displayValue = widgetState == null ? findWidgetStateAndFormat(fieldPathValue,
                            fieldPathWidgetStateMap, formattingConfig)
                            : formatFromWidgetState(fieldPathValue, widgetState, formattingConfig);
                    if ("".equals(displayValue)) {
                        displayValue = formatFromDb(fieldPathValue, rootId, formattingConfig);
                    }
                    matcher.appendReplacement(replacement, displayValue);

                }
                matcher.appendTail(replacement);
                matcher.reset();
                item.setValueByKey(columnId, replacement.toString());
            }
        }
        return item;
    }

    protected String formatFromDb(String fieldPathValue, Id id, FormattingConfig formattingConfig) {
        if (id == null) {
            return "";
        }

        return formatHandler.format(Arrays.asList(id), formattingConfig, fieldPathValue, false);

    }

    protected String findWidgetStateAndFormat(String fieldPathValue, Map<String, WidgetState> fieldPathWidgetStateMap, FormattingConfig formattingConfig) {
        StringBuilder displayValue = new StringBuilder();
        PatternIterator patternIterator = new PatternIterator(fieldPathValue);
        patternIterator.moveToNext();
        WidgetState widgetState = null;
        if (PatternIterator.ReferenceType.DIRECT_REFERENCE.equals(patternIterator.getType())) {
            widgetState = fieldPathWidgetStateMap.get(patternIterator.getValue());

        } else if (PatternIterator.ReferenceType.BACK_REFERENCE_ONE_TO_ONE.equals(patternIterator.getType())) {
            patternIterator.moveToNext();
            widgetState = fieldPathWidgetStateMap.get(patternIterator.getValue());
        }
        if (widgetState != null && widgetState instanceof LinkEditingWidgetState && !(widgetState instanceof AttachmentBoxState)) {
            LinkEditingWidgetState linkEditingWidgetState = (LinkEditingWidgetState) widgetState;
            List<Id> ids = linkEditingWidgetState.getIds();
            displayValue.append(formatHandler.format(ids, formattingConfig, fieldPathValue, true));
        }

        return displayValue.toString();
    }


    protected String formatFromWidgetState(String fieldPathValue, WidgetState widgetState, FormattingConfig formattingConfig) {
        StringBuilder displayValue = new StringBuilder();
        if (widgetState != null) {
            if (widgetState instanceof TextState) {
                TextState textBoxState = (TextState) widgetState;
                String text = textBoxState.getText();
                displayValue.append(getDisplayValue(fieldPathValue, new StringValue(text), formattingConfig));
            } else if (widgetState instanceof IntegerBoxState) {
                IntegerBoxState integerBoxState = (IntegerBoxState) widgetState;
                Long number = integerBoxState.getNumber();
                displayValue.append(getDisplayValue(fieldPathValue, new LongValue(number), formattingConfig));

            } else if (widgetState instanceof DecimalBoxState) {
                DecimalBoxState decimalBoxState = (DecimalBoxState) widgetState;
                BigDecimal number = decimalBoxState.getNumber();
                displayValue.append(getDisplayValue(fieldPathValue, new DecimalValue(number), formattingConfig));

            } else if (widgetState instanceof CheckBoxState) {
                CheckBoxState checkBoxState = (CheckBoxState) widgetState;
                Boolean checked = checkBoxState.isSelected();
                displayValue.append(getDisplayValue(fieldPathValue, new BooleanValue(checked), formattingConfig));

            } else if (widgetState instanceof DateBoxState) {
                DateBoxState dateBoxState = (DateBoxState) widgetState;
                ValueEditingWidgetHandler valueEditingWidgetHandler = (ValueEditingWidgetHandler) applicationContext.getBean("date-box");
                Value value = valueEditingWidgetHandler.getValue(dateBoxState);
                displayValue.append(getDisplayValue(fieldPathValue, value, formattingConfig));

            } else if (widgetState instanceof LinkEditingWidgetState && !(widgetState instanceof AttachmentBoxState)) {
                LinkEditingWidgetState linkEditingWidgetState = (LinkEditingWidgetState) widgetState;
                List<Id> ids = linkEditingWidgetState.getIds();
                displayValue.append(formatHandler.format(ids, formattingConfig, fieldPathValue, true));

            } else if (widgetState instanceof EnumBoxState) {
                EnumBoxState enumBoxState = (EnumBoxState) widgetState;
                String selectedText = enumBoxState.getSelectedText();
                displayValue.append(getDisplayValue(fieldPathValue, new StringValue(selectedText), formattingConfig));
            }

        }
        return displayValue.toString();
    }

    protected Map<String, WidgetState> createWidgetStateByFieldPath(String formName, Map<String, WidgetState> widgetStateMap) {
        Map<String, WidgetState> fieldPathMap = new CaseInsensitiveHashMap<>();
        List<WidgetConfig> widgetConfigs = getWidgetConfigs(formName);
        for (WidgetConfig widgetConfig : widgetConfigs) {
            String widgetId = widgetConfig.getId();
            WidgetState state = widgetStateMap.get(widgetId);
            String fpv = widgetConfig.getFieldPathConfig() == null ? null : widgetConfig.getFieldPathConfig().getValue();
            if (state != null && fpv != null) {
                fieldPathMap.put(fpv, state);
            }

        }
        return fieldPathMap;

    }

    protected List<WidgetConfig> getWidgetConfigs(String formName) {
        FormConfig formConfig = configurationService.getPlainFormConfig(formName);
        return formConfig.getWidgetConfigurationConfig().getWidgetConfigList();
    }

    public static final class Cache {
        private HashMap<String, FormConfig> formConfigByDomainObjectType = new HashMap<>();
        private HashMap<String, HashMap<String, EnumBoxConfig>> enumBoxConfigsByDomainObjectType = new HashMap<>();
        private String currentPersonUid;

        public Cache(String currentPersonUid) {
            this.currentPersonUid = currentPersonUid;
        }
    }
}

