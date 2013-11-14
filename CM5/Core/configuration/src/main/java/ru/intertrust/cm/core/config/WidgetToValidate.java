package ru.intertrust.cm.core.config;

import ru.intertrust.cm.core.config.model.FieldConfig;
import ru.intertrust.cm.core.config.model.gui.form.widget.WidgetConfig;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 13/11/13
 *         Time: 13:05 PM
 */
public class WidgetToValidate {
    private String componentName;
    private String fieldPathValue;
    private int numberOfParts;
    private String domainObjectFieldToValidate;
    private String domainObjectTypeToValidate;
    private boolean stopValidating;
    private FieldConfig fieldConfigToValidate;
    private WidgetConfig widgetConfig;

    public WidgetConfig getWidgetConfig() {
        return widgetConfig;
    }

    public void setWidgetConfig(WidgetConfig widgetConfig) {
        this.widgetConfig = widgetConfig;
    }

    public FieldConfig getFieldConfigToValidate() {
        return fieldConfigToValidate;
    }

    public void setFieldConfigToValidate(FieldConfig fieldConfigToValidate) {
        this.fieldConfigToValidate = fieldConfigToValidate;
    }

    public void decrementNumberOfNotYetValidatedParts() {
        numberOfParts--;
    }

    public boolean isStopValidating() {
        return stopValidating;
    }

    public void setStopValidating(boolean stopValidating) {
        this.stopValidating = stopValidating;
    }

    public String getDomainObjectTypeToValidate() {
        return domainObjectTypeToValidate;
    }

    public void setDomainObjectTypeToValidate(String domainObjectTypeToValidate) {
        this.domainObjectTypeToValidate = domainObjectTypeToValidate;
    }

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public String getFieldPathValue() {
        return fieldPathValue;
    }

    public void setFieldPathValue(String fieldPathValue) {
        this.fieldPathValue = fieldPathValue;
    }

    public int getNumberOfParts() {
        return numberOfParts;
    }

    public void setNumberOfParts(int numberOfParts) {
        this.numberOfParts = numberOfParts;
    }

    public String getDomainObjectFieldToValidate() {
        return domainObjectFieldToValidate;
    }

    public void setDomainObjectFieldToValidate(String domainObjectFieldToValidate) {
        this.domainObjectFieldToValidate = domainObjectFieldToValidate;
    }

}
