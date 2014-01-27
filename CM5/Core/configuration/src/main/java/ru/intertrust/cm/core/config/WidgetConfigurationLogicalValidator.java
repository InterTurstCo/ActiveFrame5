package ru.intertrust.cm.core.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.intertrust.cm.core.config.base.CollectionConfig;
import ru.intertrust.cm.core.config.base.CollectionFilterConfig;
import ru.intertrust.cm.core.config.base.TopLevelConfig;
import ru.intertrust.cm.core.config.gui.collection.view.CollectionViewConfig;
import ru.intertrust.cm.core.config.gui.form.widget.*;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 17/12/13
 *         Time: 15:05 PM
 */
public class WidgetConfigurationLogicalValidator {
    private static final String FIELD_TYPE_BOOLEAN = "BOOLEAN";
    private static final String WIDGET_CHECK_BOX = "check-box";
    private static final String WIDGET_SUGGEST_BOX = "suggest-box";
    private static final String WIDGET_TABLE_BROWSER = "table-browser";
    private static final String WIDGET_HIERARCHY_BROWSER = "hierarchy-browser";
    private static final String WIDGET_RADIO_BUTTON = "radio-button";
    private static final String WIDGET_LABEL = "label";
    private static final String REFERENCE_FIELD_CONFIG_FULL_QUALIFIED_NAME =
            "ru.intertrust.cm.core.config.ReferenceFieldConfig";

    private final static Logger logger = LoggerFactory.getLogger(WidgetConfigurationLogicalValidator.class);
    private static final Pattern PATTERN_VALIDATE = Pattern.compile("^[^{}]*(\\{[^{}]+\\}[^{}]*)*$");
    private static final Pattern PATTERN_FIND_PLACEHOLDERS = Pattern.compile("\\{([^{}]+)\\}");

    private ConfigurationExplorer configurationExplorer;

    public WidgetConfigurationLogicalValidator(ConfigurationExplorer configurationExplorer) {
        this.configurationExplorer = configurationExplorer;
    }

    public void validate(FormToValidate data, LogicalErrors logicalErrors) {

        List<WidgetConfig> widgetConfigs = data.getWidgetConfigs();

        for (WidgetConfig widgetConfig : widgetConfigs) {
            validateWidgetConfiguration(data, widgetConfig, logicalErrors);
        }
    }

    private void validateWidgetConfiguration(FormToValidate data, WidgetConfig widgetConfig, LogicalErrors logicalErrors) {
        FieldPathConfig fieldPath = widgetConfig.getFieldPathConfig();

        if (fieldPath == null) {
            return;
        }
        String fieldPathValue = fieldPath.getValue();

        if (fieldPathValue == null) {
            return;
        }
        WidgetConfigurationToValidate widgetConfiguration = prepareWidgetConfigurationForValidation(widgetConfig);
        if (fieldPathValue.contains(",")) {
            String[] split = fieldPathValue.split(",");
            for (int count = 0; count < split.length; count++) {
                String partOfFieldPathValues = split[count].trim();
                widgetConfiguration.setCurrentFieldPathValue(partOfFieldPathValues);
                prepareAndValidateWidgetConfigurationDependingOnFieldPath(data, widgetConfiguration, logicalErrors);
            }
        } else {
            widgetConfiguration.setCurrentFieldPathValue(fieldPathValue);
            prepareAndValidateWidgetConfigurationDependingOnFieldPath(data, widgetConfiguration, logicalErrors);
        }
    }

    public void prepareAndValidateWidgetConfigurationDependingOnFieldPath(FormToValidate data,
                                                                          WidgetConfigurationToValidate widget, LogicalErrors logicalErrors) {
        String[] pathParts = widget.getCurrentFieldPathValue().split("\\.");
        int numberOfParts = pathParts.length;

        String domainObjectType = data.getDomainObjectType();
        widget.setDomainObjectTypeToValidate(domainObjectType);
        widget.setNumberOfParts(numberOfParts);
        for (String pathPart : pathParts) {
            widget.decrementNumberOfNotYetValidatedParts();

            if (widget.isCurrentFieldPathValidated()) {
                return;
            }

            if (pathPart.contains("^")) {
                widget.setDomainObjectFieldToValidate(pathPart);
                widgetHasBackReferenceLink(widget);
                validateLogicDependingOnFieldPath(widget, logicalErrors);
            } else {
                widget.setDomainObjectFieldToValidate(pathPart);
                validateLogicDependingOnFieldPath(widget, logicalErrors);

            }
        }
    }

    private WidgetConfigurationToValidate prepareWidgetConfigurationForValidation(WidgetConfig widgetConfig){

        WidgetConfigurationToValidate widgetConfiguration = new WidgetConfigurationToValidate();
        widgetConfiguration.setWidgetConfig(widgetConfig);
        return widgetConfiguration;
    }

    private void validateLogicDependingOnFieldPath(WidgetConfigurationToValidate widget, LogicalErrors logicalErrors) {

        FieldConfig fieldConfig = findRequiredFieldConfig(widget, logicalErrors);
        if (fieldConfig == null) {
            String error = String.format("Could not find field '%s'  in path '%s'",
                    widget.getDomainObjectFieldToValidate(), widget.getCurrentFieldPathValue());
            logger.error(error);
            logicalErrors.addError(error);
            widget.setCurrentFieldPathBeenValidated();
            return;
        }
        widget.setFieldConfigToValidate(fieldConfig);
        validateWidgetDependingOnType(widget, logicalErrors);
        int numberOfParts = widget.getNumberOfParts();
        if (fieldPathPartsAreEnded(numberOfParts)) {
            return;
        }
        String className = fieldConfig.getClass().getCanonicalName();
        if (fieldTypeIsReference(className)) {
            widget.setDomainObjectTypeForManyToMany(widget.getDomainObjectTypeToValidate());
            widget.setDomainObjectTypeToValidate(((ReferenceFieldConfig) fieldConfig).getType());
            return;

        }
        String error = String.format("Path part '%s' in  '%s' isn't a reference type",
                widget.getDomainObjectFieldToValidate(), widget.getCurrentFieldPathValue());
        logger.error(error);
        logicalErrors.addError(error);
        widget.setCurrentFieldPathBeenValidated();

    }

    private FieldConfig findRequiredFieldConfig(WidgetConfigurationToValidate widget, LogicalErrors logicalErrors) {
        FieldConfig requiredFieldConfig = findRequiredFieldConfigOneToMany(widget, logicalErrors);
        if (requiredFieldConfig == null) {
            return findRequiredFieldConfigManyToMany(widget, logicalErrors);
        }
        return requiredFieldConfig;
    }

    private FieldConfig findRequiredFieldConfigOneToMany(WidgetConfigurationToValidate widget,
                                                         LogicalErrors logicalErrors) {
        FieldConfig fieldConfig = configurationExplorer.getFieldConfig(widget.getDomainObjectTypeToValidate(),
                widget.getDomainObjectFieldToValidate());

        if (fieldConfig != null) {
            return fieldConfig;
        }
        DomainObjectTypeConfig config = configurationExplorer.getConfig(DomainObjectTypeConfig.class,
                widget.getDomainObjectTypeToValidate());
        if (config == null) {
            return null;
        }
        String parent = config.getExtendsAttribute();
        if (parent != null) {
            widget.setDomainObjectTypeToValidate(parent);
            return findRequiredFieldConfig(widget, logicalErrors);
        }
        return null;
    }

    private FieldConfig findRequiredFieldConfigManyToMany(WidgetConfigurationToValidate widget,
                                                          LogicalErrors logicalErrors) {
        String domainObjectTypeManyToMany = widget.getDomainObjectTypeForManyToMany();
        if (domainObjectTypeManyToMany == null) {
            return null;
        }
        DomainObjectTypeConfig config = configurationExplorer.
                getConfig(DomainObjectTypeConfig.class, domainObjectTypeManyToMany);
        if (config == null) {
            return null;
        }
        String parent = config.getExtendsAttribute();

        FieldConfig fieldConfig = configurationExplorer.getFieldConfig(widget.getDomainObjectTypeForManyToMany(),
                widget.getDomainObjectFieldToValidate());

        if (fieldConfig == null && parent != null) {
            widget.setDomainObjectTypeToValidate(parent);
            return findRequiredFieldConfig(widget, logicalErrors);
        }
        return fieldConfig;
    }

    private void widgetHasBackReferenceLink(WidgetConfigurationToValidate widget) {
        String[] domainObjectTypeAndField = widget.getDomainObjectFieldToValidate().split("\\^");
        String domainObjectType = domainObjectTypeAndField[0];
        String domainObjectField = domainObjectTypeAndField[1];
        widget.setDomainObjectTypeToValidate(domainObjectType);
        widget.setDomainObjectFieldToValidate(domainObjectField);

    }

    private void validateWidgetDependingOnType(WidgetConfigurationToValidate widget, LogicalErrors logicalErrors) {
        String componentName = widget.getWidgetConfig().getComponentName();
        if (thisIsCheckBoxWidget(componentName)) {
            validateCheckBoxWidget(widget, logicalErrors);
        } else if (thisIsSuggestBoxWidget(componentName)) {
            validateSuggestBoxWidget(widget, logicalErrors);
        } else if (thisIsTableBrowserWidget(componentName)) {
            validateTableBrowserWidget(widget, logicalErrors);
        } else if (thisIsHierarchyBrowserWidget(componentName)) {
            validateHierarchyBrowserWidget(widget, logicalErrors);
        } else if (thisIsRadioButtonWidget(componentName)) {
            validateRadioButtonWidget(widget, logicalErrors);
        }  else if (thisIsLabelWidget(componentName)) {
            validateLabelWidget(widget, logicalErrors);
        }

    }

    private void validateCheckBoxWidget(WidgetConfigurationToValidate widget, LogicalErrors logicalErrors) {
        String fieldType = widget.getFieldConfigToValidate().getFieldType().name();
        if (fieldTypeIsBoolean(fieldType)) {
            return;
        }
        String error = String.format("Field '%s' in  domain object '%s' isn't a boolean type",
                widget.getFieldConfigToValidate().getName(), widget.getDomainObjectTypeToValidate());
        logger.error(error);
        logicalErrors.addError(error);
    }

    private void validateSuggestBoxWidget(WidgetConfigurationToValidate widget, LogicalErrors logicalErrors) {
        SuggestBoxConfig config = (SuggestBoxConfig) widget.getWidgetConfig();
        String collectionName = config.getCollectionRefConfig().getName();
        validateIfCollectionExists(widget, collectionName, logicalErrors);

    }

    private void validateLabelWidget(WidgetConfigurationToValidate widget, LogicalErrors logicalErrors) {
        validateAllowedCombinationOfTags(widget, logicalErrors);
    }

    private void validateHierarchyBrowserWidget(WidgetConfigurationToValidate widget, LogicalErrors logicalErrors) {
        HierarchyBrowserConfig config = (HierarchyBrowserConfig) widget.getWidgetConfig();
        NodeCollectionDefConfig nodeConfig = config.getNodeCollectionDefConfig();
        if (!widget.isMethodValidated("validateNode")) {
        validateHierarchyBrowserNode(widget, nodeConfig, logicalErrors);
        widget.addValidatedMethod("validateNode");
        }

    }

    private void validateRadioButtonWidget(WidgetConfigurationToValidate widget, LogicalErrors logicalErrors) {
        validatePattern(widget, logicalErrors);
    }

    private void validateAllowedCombinationOfTags(WidgetConfigurationToValidate widget, LogicalErrors logicalErrors){
        LabelConfig labelConfig = (LabelConfig) widget.getWidgetConfig();
        String text = labelConfig.getText();
        PatternConfig patternConfig = labelConfig.getPattern();
        RendererConfig rendererConfig = labelConfig.getRenderer();
        if (text != null && patternConfig != null){
            String error = String.format("Widget with id '%s' has redundant tag <pattern>",
                    labelConfig.getId());
            logger.error(error);
            logicalErrors.addError(error);
        }
        if (text != null && rendererConfig != null){
            String error = String.format("Widget with id '%s' has redundant tag <renderer>",
                    labelConfig.getId());
            logger.error(error);
            logicalErrors.addError(error);
        }
        if (text == null && rendererConfig != null && patternConfig != null){
            String error = String.format("Widget with id '%s' has redundant tag <pattern>",
                    labelConfig.getId());
            logger.error(error);
            logicalErrors.addError(error);
        }

    }

    private void validatePattern(WidgetConfigurationToValidate widget, LogicalErrors logicalErrors) {
        if (widget.getNumberOfParts() > 0) {
            return;
        }
        RadioButtonConfig config = (RadioButtonConfig) widget.getWidgetConfig();
        PatternConfig patternConfig = config.getPatternConfig();
        String pattern = patternConfig.getValue();

        String widgetName = widget.getWidgetConfig().getComponentName();
        String widgetId = widget.getWidgetConfig().getId();
        if (pattern.isEmpty()) {
            String error = String.format("Pattern is empty for %s with id '%s'", widgetName, widgetId);
            logger.error(error);
            logicalErrors.addError(error);
        } else if (!patternFormatIsValid(pattern)) {
            String error = String.format("Pattern format is not valid for %s with id '%s'", widgetName, widgetId);
            logger.error(error);
            logicalErrors.addError(error);
        } else {
            ReferenceFieldConfig fieldConfig = (ReferenceFieldConfig)widget.getFieldConfigToValidate();
            String domainObjectType = fieldConfig.getType();

            List<String> fieldNames = getFieldNames(domainObjectType);

            List<String> placeholders = findPatternPlaceholders(pattern);
            for (String placeholder : placeholders) {
                if (!fieldNames.contains(placeholder)) {
                    String error = String.format("Incorrect pattern placeholder '%s' found for domain object '%s' in %s with id '%s'", placeholder, domainObjectType,  widgetName, widgetId);
                    logger.error(error);
                    logicalErrors.addError(error);
                }
            }
        }
    }

    private boolean patternFormatIsValid(String patternString) {
        return PATTERN_VALIDATE.matcher(patternString).matches();
    }

    private List<String> findPatternPlaceholders(String patternString) {
        Matcher m = PATTERN_FIND_PLACEHOLDERS.matcher(patternString);
        List<String> placeholders = new ArrayList<String>();
        while (m.find()) {
            placeholders.add(m.group(1));
        }
        return placeholders;
    }

    private List<String> getFieldNames(String domainObjectType) {
        DomainObjectTypeConfig domainObjectConfig = configurationExplorer.getConfig(DomainObjectTypeConfig.class,
                domainObjectType);
        List<FieldConfig> fieldsConfigs = domainObjectConfig.getFieldConfigs();

        List<String> fieldNames = new ArrayList<>(fieldsConfigs.size());
        for (FieldConfig fieldConfig : fieldsConfigs) {
            fieldNames.add(fieldConfig.getName());
        }
        return fieldNames;
    }

    private void validateTableBrowserWidget(WidgetConfigurationToValidate widget, LogicalErrors logicalErrors) {
        TableBrowserConfig config = (TableBrowserConfig) widget.getWidgetConfig();
        String collectionName = config.getCollectionRefConfig().getName();
        validateIfCollectionExists(widget, collectionName, logicalErrors);
        String collectionViewName = config.getCollectionViewRefConfig().getName();
        validateIfCollectionViewExists(widget, collectionViewName, logicalErrors);
    }

    private void validateIfCollectionViewExists(WidgetConfigurationToValidate widget,
                                                String collectionViewName, LogicalErrors logicalErrors) {
        TopLevelConfig config = findRequiredConfigByClassAndName(CollectionViewConfig.class, collectionViewName);
        if (config == null) {
            String error = String.format("Collection view '%s' for %s with id '%s' wasn't found",
                    collectionViewName, widget.getWidgetConfig().getComponentName(), widget.getWidgetConfig().getId());
            logger.error(error);
            logicalErrors.addError(error);
        }

    }

    private void validateHierarchyBrowserNode(WidgetConfigurationToValidate widget,
                                              NodeCollectionDefConfig nodeConfig, LogicalErrors logicalErrors) {
        String collectionName = nodeConfig.getCollection();
        CollectionConfig collectionConfig = validateIfCollectionExists(widget, collectionName, logicalErrors);
        if( collectionConfig != null) {
            validateIfFiltersExist(collectionConfig, nodeConfig, logicalErrors);
        }
        NodeCollectionDefConfig childNodeConfig = nodeConfig.getNodeCollectionDefConfig();
        if (childNodeConfig != null) {
            validateHierarchyBrowserNode(widget, childNodeConfig, logicalErrors);
        }
    }

    private CollectionConfig validateIfCollectionExists(WidgetConfigurationToValidate widget,
                                                                 String collectionName, LogicalErrors logicalErrors) {
        CollectionConfig config = (CollectionConfig) findRequiredConfigByClassAndName(CollectionConfig.class, collectionName);
        if (config == null) {
            String error = String.format("Collection '%s' for %s with id '%s' wasn't found",
                    collectionName, widget.getWidgetConfig().getComponentName(), widget.getWidgetConfig().getId());
            logger.error(error);
            logicalErrors.addError(error);

        }
           return  config;
    }

    private void validateIfFiltersExist(CollectionConfig collectionConfig,
                                        NodeCollectionDefConfig nodeConfig, LogicalErrors logicalErrors){
        String filterInWidget = nodeConfig.getParentFilter();
        if (filterInWidget == null) {
            return;
        }
        List<String> filtersFromCollectionConfig = getFiltersFromCollectionConfig(collectionConfig);
            if (!filtersFromCollectionConfig.contains(filterInWidget)){
                String error = String.format("Collection '%s' has no filter '%s'", collectionConfig.getName(), filterInWidget);
                logger.error(error);
                logicalErrors.addError(error);
            }
    }

    private List<String> getFiltersFromCollectionConfig(CollectionConfig config){
        List<String> filtersFromCollectionConfig = new ArrayList<String>();
        List<CollectionFilterConfig> filterConfigs = config.getFilters();
        for (CollectionFilterConfig filterConfig : filterConfigs) {
                filtersFromCollectionConfig.add(filterConfig.getName());
            }
        return  filtersFromCollectionConfig;
    }

    private TopLevelConfig findRequiredConfigByClassAndName(Class classOfConfig, String name) {
        return (TopLevelConfig) configurationExplorer.getConfig(classOfConfig, name);
    }

    private boolean fieldPathPartsAreEnded(int numberOfParts) {
        return numberOfParts == 0;
    }

    private boolean fieldTypeIsReference(String className) {
        return REFERENCE_FIELD_CONFIG_FULL_QUALIFIED_NAME.equalsIgnoreCase(className);
    }

    private boolean fieldTypeIsBoolean(String fieldType) {
        return FIELD_TYPE_BOOLEAN.equalsIgnoreCase(fieldType);
    }

    private boolean thisIsCheckBoxWidget(String componentName) {
        return WIDGET_CHECK_BOX.equalsIgnoreCase(componentName);
    }

    private boolean thisIsSuggestBoxWidget(String componentName) {
        return WIDGET_SUGGEST_BOX.equalsIgnoreCase(componentName);
    }

    private boolean thisIsTableBrowserWidget(String componentName) {
        return WIDGET_TABLE_BROWSER.equalsIgnoreCase(componentName);
    }

    private boolean thisIsHierarchyBrowserWidget(String componentName) {
        return WIDGET_HIERARCHY_BROWSER.equalsIgnoreCase(componentName);
    }

    private boolean thisIsRadioButtonWidget(String componentName) {
        return WIDGET_RADIO_BUTTON.equalsIgnoreCase(componentName);
    }
    private boolean thisIsLabelWidget(String componentName) {
        return WIDGET_LABEL.equalsIgnoreCase(componentName);
    }

}
