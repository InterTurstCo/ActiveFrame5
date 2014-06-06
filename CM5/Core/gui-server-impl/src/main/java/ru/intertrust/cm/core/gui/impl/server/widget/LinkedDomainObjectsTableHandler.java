package ru.intertrust.cm.core.gui.impl.server.widget;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.config.gui.form.FormConfig;
import ru.intertrust.cm.core.config.gui.form.widget.*;
import ru.intertrust.cm.core.gui.api.server.widget.FormatHandler;
import ru.intertrust.cm.core.gui.api.server.widget.LinkEditingWidgetHandler;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.impl.server.form.FormSaver;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.FieldPath;
import ru.intertrust.cm.core.gui.model.form.FormObjects;
import ru.intertrust.cm.core.gui.model.form.FormState;
import ru.intertrust.cm.core.gui.model.form.widget.LinkedDomainObjectsTableState;
import ru.intertrust.cm.core.gui.model.form.widget.RowItem;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ComponentName("linked-domain-objects-table")
public class LinkedDomainObjectsTableHandler extends LinkEditingWidgetHandler {

    @Autowired
    CrudService crudService;
    @Autowired
    ApplicationContext applicationContext;

    @Override
    public LinkedDomainObjectsTableState getInitialState(WidgetContext context) {

        LinkedDomainObjectsTableState state = new LinkedDomainObjectsTableState();
        LinkedDomainObjectsTableConfig domainObjectsTableConfig = context.getWidgetConfig();
        state.setLinkedDomainObjectTableConfig(domainObjectsTableConfig);
        SingleChoiceConfig singleChoiceConfig = domainObjectsTableConfig.getSingleChoiceConfig();
        boolean singleChoiceFromConfig = singleChoiceConfig == null ? false : singleChoiceConfig.isSingleChoice();
        state.setSingleChoice(singleChoiceFromConfig);
        List<DomainObject> domainObjects = new ArrayList<>();
        List<Id> ids = context.getAllObjectIds();
        if (!ids.isEmpty()) {
            domainObjects = crudService.find(ids);
        }
        String linkedFormName = domainObjectsTableConfig.getLinkedFormConfig().getName();
        if (linkedFormName != null && !linkedFormName.isEmpty()) {
            FormConfig formConfig = configurationService.getConfig(FormConfig.class, linkedFormName);
            state.setObjectTypeName(formConfig.getDomainObjectType());
        }

        List<RowItem> rowItems = new ArrayList<>();
        List<SummaryTableColumnConfig> summaryTableColumnConfigs = domainObjectsTableConfig
                .getSummaryTableConfig().getSummaryTableColumnConfig();

        for (DomainObject domainObject : domainObjects) {
            RowItem rowItem;
            rowItem = map(domainObject, summaryTableColumnConfigs);
            rowItem.setObjectId(domainObject.getId());
            rowItems.add(rowItem);
        }
        state.setRowItems(rowItems);
        return state;
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
            DomainObject savedObject;

            if (fieldPath.isOneToManyReference()) {
                final HashMap<FieldPath, Value> rootObjectValues = new HashMap<>();
                rootObjectValues.put(new FieldPath(fieldPath.getLinkToParentName()), new ReferenceValue(rootDomainObject.getId()));
                FormSaver formSaver = (FormSaver) applicationContext.getBean("formSaver", formState, rootObjectValues);
                savedObject = formSaver.saveForm();
            } else if (fieldPath.isManyToManyReference()) {
                FormSaver formSaver = (FormSaver) applicationContext.getBean("formSaver", formState, null);
                savedObject = formSaver.saveForm();
                String referenceType = fieldPath.getReferenceType();
                DomainObject referencedObject = crudService.createDomainObject(referenceType);
                referencedObject.setReference(fieldPath.getLinkToChildrenName(), savedObject);
                referencedObject.setReference(fieldPath.getLinkToParentName(), rootDomainObject);
                crudService.save(referencedObject);
            } else { // one-to-one reference
                // todo: not-null constraint will fail!
                FormSaver formSaver = (FormSaver) applicationContext.getBean("formSaver", formState, null);
                savedObject = formSaver.saveForm();
                rootDomainObject.setReference(fieldPath.getFieldName(), savedObject);
                crudService.save(rootDomainObject);
            }

            newObjects.add(savedObject);
        }
        return newObjects;
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
            PatternConfig patternConfig = columnConfig.getPatternConfig();
            String columnPattern = patternConfig.getValue();

            List<String> fieldsInPattern = takeFieldNamesFromPattern(columnPattern);
            FormattingConfig formattingConfig = columnConfig.getFormattingConfig();

            Map<String, String> formattedFields = new HashMap<>();

            if (formattingConfig != null) {
                NumberFormatConfig numberFormatConfig = formattingConfig.getNumberFormatConfig();
                if (numberFormatConfig != null) {
                    formatFields(new NumberFormatter(domainObject), formattedFields, numberFormatConfig.getPattern(),
                            numberFormatConfig.getFieldsPathConfig());
                }
                DateFormatConfig dateFormatConfig = formattingConfig.getDateFormatConfig();
                if (dateFormatConfig != null) {
                    formatFields(new DateFormatter(domainObject), formattedFields, dateFormatConfig.getPattern(),
                            dateFormatConfig.getFieldsPathConfig());
                }
            }
            for (String fieldName : fieldsInPattern) {
                String displayValue = formatHandler.format(domainObject, fieldPatternMatcher(columnPattern), formattingConfig);
                formattedFields.put(fieldName, displayValue);

            }

            String columnValue = applyColumnPattern(columnPattern, formattedFields);
            rowItem.setValueByKey(columnConfig.getWidgetId(), columnValue);
        }
        return rowItem;
    }

    private List<String> takeFieldNamesFromPattern(String columnPattern) {
        Matcher matcher = fieldPatternMatcher(columnPattern);
        List<String> fieldNames = new ArrayList<>();
        while (matcher.find()) {
            String fieldName = takeFieldNameFromMatchedSequence(matcher);
            fieldNames.add(fieldName);
        }
        return fieldNames;
    }

    private void formatFields(FieldFormatter fieldFormatter, Map<String, String> formattedFields, String formatPattern,
                              FieldPathsConfig fieldsPathConfig) {
        for (FieldPathConfig fieldPathConfig : fieldsPathConfig
                .getFieldPathConfigsList()) {
            String fieldName = fieldPathConfig.getValue();
            if (formatPattern != null) {
                formattedFields.put(fieldName, fieldFormatter.format(fieldName, formatPattern));
            }
        }
    }

    private String applyColumnPattern(String columnPattern, Map<String, String> formattedFields) {
        Matcher matcher = fieldPatternMatcher(columnPattern);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String fieldName = takeFieldNameFromMatchedSequence(matcher);
            String replacement = formattedFields.get(fieldName);
            if (replacement != null) {
                matcher.appendReplacement(result, replacement);
            }
        }

        matcher.appendTail(result);
        return result.toString();
    }

    private String takeFieldNameFromMatchedSequence(Matcher matcher) {
        String group = matcher.group();
        return group.substring(1, group.length() - 1);
    }

    private Matcher fieldPatternMatcher(String pattern) {
        Pattern fieldPlaceholderPattern = Pattern.compile(FormatHandler.FIELD_PLACEHOLDER_PATTERN);
        return fieldPlaceholderPattern.matcher(pattern);
    }

}

