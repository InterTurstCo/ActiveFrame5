package ru.intertrust.cm.core.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import ru.intertrust.cm.core.config.form.widget.AbstractWidgetLogicalValidator;
import ru.intertrust.cm.core.config.form.widget.WidgetLogicalValidatorHelper;
import ru.intertrust.cm.core.config.gui.NotNullLogicalValidation;
import ru.intertrust.cm.core.config.gui.form.widget.FieldPathConfig;
import ru.intertrust.cm.core.config.gui.form.widget.WidgetConfig;
import ru.intertrust.cm.core.util.ReflectionUtil;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 16.08.2015
 *         Time: 20:37
 */
public class WidgetConfigurationLogicalValidatorImpl implements WidgetConfigurationLogicalValidator {

    private final static Logger logger = LoggerFactory.getLogger(WidgetConfigurationLogicalValidator.class);

    private ConfigurationExplorer configurationExplorer;

    private ApplicationContext applicationContext;

    public WidgetConfigurationLogicalValidatorImpl() {
    }

    public WidgetConfigurationLogicalValidatorImpl(ConfigurationExplorer configurationExplorer) {
        setConfigurationExplorer(configurationExplorer);
    }

    public void setConfigurationExplorer(ConfigurationExplorer configurationExplorer) {
        this.configurationExplorer = configurationExplorer;
        this.applicationContext = ((ConfigurationExplorerImpl) configurationExplorer).getContext();
    }

    public void validate(FormToValidate data, LogicalErrors logicalErrors) {

        List<WidgetConfig> widgetConfigs = data.getWidgetConfigs();

        for (WidgetConfig widgetConfig : widgetConfigs) {
            validateWidgetConfiguration(data, widgetConfig, logicalErrors);
        }
    }

    private void validateWidgetConfiguration(FormToValidate data, WidgetConfig widgetConfig, LogicalErrors logicalErrors) {
        validateNotNulls(widgetConfig, logicalErrors);

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

    private void validateNotNulls(WidgetConfig widgetConfig, LogicalErrors logicalErrors){

        List<Field> fields = new ArrayList<>();
        Class clazz = widgetConfig.getClass();
        ReflectionUtil.fillAllFields(fields, clazz);
        for (Field field : fields) {
            if(field.isAnnotationPresent(NotNullLogicalValidation.class)){
                validateNotNull(widgetConfig, field, logicalErrors);
            }
        }
    }

    private void validateNotNull(WidgetConfig widgetConfig, Field field, LogicalErrors logicalErrors){
        NotNullLogicalValidation annotation = field.getAnnotation(NotNullLogicalValidation.class);
        String[] skippedComponentNames = annotation.skippedComponentNames();
        if(Arrays.asList(skippedComponentNames).contains(widgetConfig.getComponentName())){
            return; //skip validation
        }
        field.setAccessible(true);
        try {
            Object value = field.get(widgetConfig);
            if(value == null){
                String error = String.format("There is no mandatory field '%s' for widget '%s' with id '%s'",
                        field.getName(), widgetConfig.getComponentName(), widgetConfig.getId());
                logicalErrors.addError(error);
            }
        } catch (IllegalAccessException e) {
            //
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
                widgetHasBackReferenceLink(widget, "\\^");
                validateLogicDependingOnFieldPath(widget, logicalErrors);
            } else if (pathPart.contains("|")) {
                widget.setDomainObjectFieldToValidate(pathPart);
                widgetHasBackReferenceLink(widget, "\\|");
                validateLogicDependingOnFieldPath(widget, logicalErrors);
            } else {
                widget.setDomainObjectFieldToValidate(pathPart);
                validateLogicDependingOnFieldPath(widget, logicalErrors);
            }
        }
    }

    private WidgetConfigurationToValidate prepareWidgetConfigurationForValidation(WidgetConfig widgetConfig) {

        WidgetConfigurationToValidate widgetConfiguration = new WidgetConfigurationToValidate();
        widgetConfiguration.setWidgetConfig(widgetConfig);
        return widgetConfiguration;
    }

    private void validateLogicDependingOnFieldPath(WidgetConfigurationToValidate widget, LogicalErrors logicalErrors) {

        FieldConfig fieldConfig = findRequiredFieldConfig(widget, logicalErrors);
        if (fieldConfig == null) {
            if (widget.getWidgetConfig().getHandler() != null) { // custom widget handler allows using fake field-paths
                return;
            }
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
        if (WidgetLogicalValidatorHelper.fieldTypeIsReference(className)) {

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
            widget.setDomainObjectTypeForManyToMany(null);
            return findRequiredFieldConfig(widget, logicalErrors);
        }
        return fieldConfig;
    }

    private void widgetHasBackReferenceLink(WidgetConfigurationToValidate widget, String delimeter) {
        String[] domainObjectTypeAndField = widget.getDomainObjectFieldToValidate().split(delimeter);
        String domainObjectType = domainObjectTypeAndField[0];
        String domainObjectField = domainObjectTypeAndField[1];
        widget.setDomainObjectTypeToValidate(domainObjectType);
        widget.setDomainObjectFieldToValidate(domainObjectField);

    }

    private void validateWidgetDependingOnType(WidgetConfigurationToValidate widget, LogicalErrors logicalErrors) {
        String widgetValidatorName = widget.getWidgetConfig().getLogicalValidatorComponentName();
        if (widgetValidatorName != null) {
            AbstractWidgetLogicalValidator widgetLogicalValidator =
                    applicationContext.getBean(widgetValidatorName, AbstractWidgetLogicalValidator.class);
            widgetLogicalValidator.setConfigurationExplorer(configurationExplorer);
            widgetLogicalValidator.validate(widget, logicalErrors);
        }

    }

    private boolean fieldPathPartsAreEnded(int numberOfParts) {
        return numberOfParts == 0;
    }

}
