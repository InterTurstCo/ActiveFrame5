package ru.intertrust.cm.core.config;

import org.simpleframework.xml.Attribute;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * Developer: Ravil Abdulkhairov
 * Date: 27.04.2016
 * Time: 10:34
 * To change this template use File | Settings | File and Code Templates.
 */

public class CollectionRetrieverConfig implements Serializable {

    @Attribute(name = "widget-id", required = true)
    private String widgetId;

    @Attribute(name = "componnet-name", required = true)
    private String componentName;

    public CollectionRetrieverConfig(){}

    public String getWidgetId() {
        return widgetId;
    }

    public void setWidgetId(String widgetId) {
        this.widgetId = widgetId;
    }

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CollectionRetrieverConfig that = (CollectionRetrieverConfig) o;

        if (widgetId != null ? !widgetId.equals(that.widgetId) : that.widgetId != null) {
            return false;
        }

        if (componentName != null ? !componentName.equals(that.componentName) : that.componentName != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (widgetId != null ? widgetId.hashCode() : 0);
        result = 31 * result + (componentName != null ? componentName.hashCode() : 0);
        return result;
    }
}
