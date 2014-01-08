package ru.intertrust.cm.core.config;

import ru.intertrust.cm.core.config.gui.form.widget.WidgetConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 13/11/13
 *         Time: 13:05 PM
 */
public class WidgetConfigurationToValidate {

    private String currentFieldPathValue;
    private int numberOfParts;
    private String domainObjectFieldToValidate;
    private String domainObjectTypeToValidate;
    private FieldConfig fieldConfigToValidate;
    private WidgetConfig widgetConfig;
    private String domainObjectTypeForManyToMany;
    private HashMap<String, Boolean> fieldPathValidatedMap = new HashMap<String, Boolean>();
    private List<String> validatedMethods = new ArrayList<String>();
    public WidgetConfig getWidgetConfig() {
        return widgetConfig;
    }

    public void setWidgetConfig(WidgetConfig widgetConfig) {
        this.widgetConfig = widgetConfig;
    }

    public FieldConfig getFieldConfigToValidate() {
        return fieldConfigToValidate;
    }

    public void setFieldConfigToValidate(FieldConfig fieldConfigsToValidate) {
        this.fieldConfigToValidate = fieldConfigsToValidate;
    }

    public void decrementNumberOfNotYetValidatedParts() {
        numberOfParts--;
    }

    public Boolean isCurrentFieldPathValidated() {
        return fieldPathValidatedMap.get(currentFieldPathValue);
    }
    public void setCurrentFieldPathBeenValidated (){
      fieldPathValidatedMap.put(currentFieldPathValue, true);
 }

    public String getDomainObjectTypeToValidate() {
        return domainObjectTypeToValidate;
    }

    public String getDomainObjectTypeForManyToMany() {
        return domainObjectTypeForManyToMany;
    }

    public void setDomainObjectTypeForManyToMany(String domainObjectTypeForManyToMany) {
        this.domainObjectTypeForManyToMany = domainObjectTypeForManyToMany;
    }

    public void setDomainObjectTypeToValidate(String domainObjectTypeToValidate) {
        this.domainObjectTypeToValidate = domainObjectTypeToValidate;
    }

    public String getCurrentFieldPathValue() {
        return currentFieldPathValue;
    }

    public void setCurrentFieldPathValue(String currentFieldPathValue) {
        this.currentFieldPathValue = currentFieldPathValue;
        fieldPathValidatedMap.put(currentFieldPathValue, false);
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

   public void addValidatedMethod(String method) {
       validatedMethods.add(method);
   }
    public boolean isMethodValidated(String method){
        return validatedMethods.contains(method);
    }
}
