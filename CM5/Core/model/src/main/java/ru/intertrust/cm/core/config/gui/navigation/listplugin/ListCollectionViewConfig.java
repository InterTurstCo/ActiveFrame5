package ru.intertrust.cm.core.config.gui.navigation.listplugin;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.gui.navigation.hierarchyplugin.HierarchyCollectionViewConfig;

/**
 * Created by Ravil on 10.04.2017.
 */
@Root(name = "list-collection-view")
public class ListCollectionViewConfig implements Dto {
    @Attribute(name = "widget-implementation", required = false)
    private String widgetImplementation;

    @Attribute(name = "view-name", required = true)
    private String viewName;

    public ListCollectionViewConfig(){}

    public String getWidgetImplementation() {
        return widgetImplementation;
    }

    public void setWidgetImplementation(String widgetImplementation) {
        this.widgetImplementation = widgetImplementation;
    }

    public String getViewName() {
        return viewName;
    }

    public void setViewName(String viewName) {
        this.viewName = viewName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ListCollectionViewConfig that = (ListCollectionViewConfig) o;
        if (widgetImplementation != null
                ? !widgetImplementation.equals(that.widgetImplementation)
                : that.widgetImplementation != null) {
            return false;
        }
        if (viewName != null
                ? !viewName.equals(that.viewName)
                : that.viewName != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = widgetImplementation != null ? widgetImplementation.hashCode() : 31;
        result = 31 * result + (viewName != null ? viewName.hashCode() : 31);
        return result;
    }
}
