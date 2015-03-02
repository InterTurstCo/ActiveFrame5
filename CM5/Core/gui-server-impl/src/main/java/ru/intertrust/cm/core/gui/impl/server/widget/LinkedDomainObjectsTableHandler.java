package ru.intertrust.cm.core.gui.impl.server.widget;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.PersonService;
import ru.intertrust.cm.core.business.api.ProfileService;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.business.api.dto.form.PopupTitlesHolder;
import ru.intertrust.cm.core.config.gui.form.FormConfig;
import ru.intertrust.cm.core.config.gui.form.widget.*;
import ru.intertrust.cm.core.config.gui.form.widget.filter.SelectionFiltersConfig;
import ru.intertrust.cm.core.config.gui.form.widget.linkediting.LinkedFormMappingConfig;
import ru.intertrust.cm.core.config.gui.navigation.CollectionRefConfig;
import ru.intertrust.cm.core.gui.api.server.GuiService;
import ru.intertrust.cm.core.gui.api.server.plugin.FilterBuilder;
import ru.intertrust.cm.core.gui.api.server.widget.FormatHandler;
import ru.intertrust.cm.core.gui.api.server.widget.LinkEditingWidgetHandler;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.impl.server.action.access.AccessChecker;
import ru.intertrust.cm.core.gui.impl.server.form.FormResolver;
import ru.intertrust.cm.core.gui.impl.server.form.FormSaver;
import ru.intertrust.cm.core.gui.impl.server.util.*;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.filters.ComplicatedFiltersParams;
import ru.intertrust.cm.core.gui.model.form.FieldPath;
import ru.intertrust.cm.core.gui.model.form.FormObjects;
import ru.intertrust.cm.core.gui.model.form.FormState;
import ru.intertrust.cm.core.gui.model.form.widget.*;
import ru.intertrust.cm.core.gui.model.util.WidgetUtil;
import ru.intertrust.cm.core.gui.model.validation.ValidationException;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;

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

    @Autowired
    private GuiService guiService;

    @Autowired
    private FormResolver formResolver;

    @Autowired
    protected PersonService personService;

    @Deprecated
    private ProfileService profileService;

    private static final String DEFAULT_EDIT_ACCESS_CHECKER = "default.edit.access.checker";
    private static final String DEFAULT_DELETE_ACCESS_CHECKER = "default.delete.access.checker";

    private LinkedDomainObjectsTableConfig widgetConfig;
    private HashMap<String, FormConfig> formConfigByDomainObjectType = new HashMap<>();
    private HashMap<String, HashMap<String, EnumBoxConfig>> enumBoxConfigsByDomainObjectType = new HashMap<>();
    private String currentPersonUid;

    @Override
    public LinkedDomainObjectsTableState getInitialState(WidgetContext context) {
        this.widgetConfig = context.getWidgetConfig();
        this.currentPersonUid = personService.getCurrentPersonUid();

        LinkedDomainObjectsTableState state = new LinkedDomainObjectsTableState();
        state.setLinkedDomainObjectTableConfig(widgetConfig);
        SingleChoiceConfig singleChoiceConfig = widgetConfig.getSingleChoiceConfig();
        Boolean singleChoiceFromConfig = singleChoiceConfig == null ? false : singleChoiceConfig.isSingleChoice();
        boolean singleChoice = isSingleChoice(context, singleChoiceFromConfig);
        state.setSingleChoice(singleChoice);
        ArrayList<Id> ids = context.getAllObjectIds();
        DomainObject root = context.getFormObjects().getRootNode().getDomainObject();
        PopupTitlesHolder popupTitlesHolder = titleBuilder.buildPopupTitles(widgetConfig
                .getLinkedFormConfig(), root);
        state.setPopupTitlesHolder(popupTitlesHolder);
        state.setIds(ids);

        String linkedFormName = widgetConfig.getLinkedFormConfig().getName();
        if (linkedFormName != null && !linkedFormName.isEmpty()) {
            FormConfig defaultFormConfig = configurationService.getConfig(FormConfig.class, linkedFormName);
            state.setObjectTypeName(defaultFormConfig.getDomainObjectType());
        }
        SelectionFiltersConfig selectionFiltersConfig = widgetConfig.getSelectionFiltersConfig();
        CollectionRefConfig refConfig = widgetConfig.getCollectionRefConfig();
        boolean collectionNameConfigured = refConfig != null;
        ComplicatedFiltersParams filtersParams = new ComplicatedFiltersParams(root.getId());
        List<Id> idsForItemsGenerating = selectionFiltersConfig == null || !collectionNameConfigured ? ids : getNotLimitedIds(widgetConfig, ids, filtersParams, false);
        state.setFilteredItemsNumber(idsForItemsGenerating.size());
        int limit = WidgetUtil.getLimit(selectionFiltersConfig);
        WidgetServerUtil.doLimit(idsForItemsGenerating, limit);
        List<RowItem> rowItems = generateRowItems(widgetConfig, idsForItemsGenerating);
        state.setRowItems(rowItems);

        return state;
    }


    private List<Id> getNotLimitedIds(LinkedDomainObjectsTableConfig widgetConfig,
                                      List<Id> selectedIds, ComplicatedFiltersParams filtersParams, boolean tooltipContent) {
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
            selectedFilteredIds.add(object.getId());
        }
        return selectedFilteredIds;
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

            List<String> validationResult = PluginHandlerHelper.doServerSideValidation(formState, applicationContext,
                    profileService.getPersonLocale());
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

    // Column -> Pattern -> Domain Object Type
    public RowItem map(DomainObject domainObject, List<SummaryTableColumnConfig> summaryTableColumnConfigs) {
        RowItem rowItem = new RowItem();
        Map<String, Boolean> rowAccessMatrix = new HashMap<>();
        AccessChecker defaultAccessChecker = (AccessChecker) applicationContext.getBean(DEFAULT_EDIT_ACCESS_CHECKER);
        rowAccessMatrix.put(DEFAULT_EDIT_ACCESS_CHECKER, defaultAccessChecker.checkAccess(domainObject.getId()));
        defaultAccessChecker = (AccessChecker) applicationContext.getBean(DEFAULT_DELETE_ACCESS_CHECKER);
        rowAccessMatrix.put(DEFAULT_DELETE_ACCESS_CHECKER, defaultAccessChecker.checkAccess(domainObject.getId()));

        HashMap<String, EnumBoxConfig> enumBoxConfigsByFieldPath = getEnumBoxConfigs(domainObject.getTypeName());
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

    private HashMap<String, EnumBoxConfig> getEnumBoxConfigs(String domainObjectType) {
        HashMap<String, EnumBoxConfig> result = enumBoxConfigsByDomainObjectType.get(domainObjectType);
        if (result != null) {
            return result;
        }

        FormConfig formConfig = getFormConfig(domainObjectType);
        HashMap<String, EnumBoxConfig> configs = new HashMap<>();
        for (WidgetConfig config : formConfig.getWidgetConfigsById().values()) {
            if (config instanceof EnumBoxConfig) {
                configs.put(config.getFieldPathConfig().getValue(), (EnumBoxConfig) config);
            }
        }
        enumBoxConfigsByDomainObjectType.put(domainObjectType, configs);
        return configs;
    }

    private FormConfig getFormConfig(String domainObjectType) {
        final FormConfig formConfig = formConfigByDomainObjectType.get(domainObjectType);
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
            config = formResolver.findFormConfig(domainObjectType, FormConfig.TYPE_EDIT, currentPersonUid);
        } else {
            config = configurationService.getConfig(FormConfig.class, formName);
        }
        formConfigByDomainObjectType.put(domainObjectType, config);
        return config;
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
        return FormatHandler.pattern.matcher(pattern);
    }

    public Dto fetchWidgetItems(Dto inputParams) {
        LinkedTableTooltipRequest request = (LinkedTableTooltipRequest) inputParams;
        LinkedDomainObjectsTableConfig config = request.getConfig();
        List<Id> ids = request.getSelectedIds();
        ComplicatedFiltersParams filtersParams = request.getFiltersParams();
        SelectionFiltersConfig selectionFiltersConfig = config.getSelectionFiltersConfig();
        CollectionRefConfig refConfig = config.getCollectionRefConfig();
        boolean collectionNameConfigured = refConfig != null;
        List<Id> idsForItemsGenerating = selectionFiltersConfig == null || !collectionNameConfigured ? ids
                : getNotLimitedIds(config, ids, filtersParams, true);
        List<RowItem> rowItems = generateRowItems(config, idsForItemsGenerating);

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
            String widgetId = findWidgetIdFromMappings(summaryTableColumnConfig, request.getLinkedFormName());
            String columnId = summaryTableColumnConfig.getColumnId();
            WidgetState widgetState = createdObjectState.getFullWidgetsState().get(widgetId);
            StringBuilder representation = new StringBuilder();
            if (summaryTableColumnConfig.getValueGeneratorComponent() != null) {
                StringValueRenderer valueRenderer = (StringValueRenderer) applicationContext.getBean(summaryTableColumnConfig.getValueGeneratorComponent());
                representation.append(valueRenderer.render(createdObjectState));
                item.setValueByKey(columnId, representation.toString());
            } else {
                // todo: fix this parsing
                // Допустим, паттерн отображения такой: {name} {description}
                // Алгоритм: если встретили name, то из состояния формы вытаскиваем состояние виджета FormState.getWidgetState("name"),
                // его форматируем и подставляем. Если встретили {ref.text}, то ищем виджет с field-path==ref.text,
                // если такого нет - то с field-path == "ref", берём его состояние и если оно не пустое и id != null (не новый объект),
                // то "раскручиваем" дальше (получаем объект по Id, а у него поле text).
                // Если виджетов для field-path (в общем случае, составного) вообще нет на форме, то нужно попробовать (там тоже может не быть)
                // вытащить из базы, как это делается при построении таблицы этого виджета на сервере.
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

                    } else if (widgetState instanceof EnumBoxState) {
                        EnumBoxState enumBoxState = (EnumBoxState) widgetState;
                        String selectedText = enumBoxState.getSelectedText();
                        representation.append(formatHandler.format(new StringValue(selectedText), matcher, formattingConfig));
                    }
                    item.setValueByKey(columnId, representation.toString());
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
        return summaryTableColumnConfig.getWidgetId();
    }


}

