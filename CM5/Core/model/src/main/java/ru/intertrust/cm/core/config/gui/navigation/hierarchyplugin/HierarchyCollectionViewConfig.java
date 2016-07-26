package ru.intertrust.cm.core.config.gui.navigation.hierarchyplugin;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * Created by IntelliJ IDEA.
 * Developer: Ravil Abdulkhairov
 * Date: 26.07.2016
 * Time: 9:23
 * To change this template use File | Settings | File and Code Templates.
 */
@Root(name = "hierarchy-collection-view")
public class HierarchyCollectionViewConfig implements Dto {

    @Attribute(name = "widget-implementation", required = false)
    private String widgetImplementation;

    @Attribute(name = "collection-view", required = true)
    private String collectionView;

    public HierarchyCollectionViewConfig(){}

    public String getWidgetImplementation() {
        return widgetImplementation;
    }

    public void setWidgetImplementation(String widgetImplementation) {
        this.widgetImplementation = widgetImplementation;
    }

    public String getCollectionView() {
        return collectionView;
    }

    public void setCollectionView(String collectionView) {
        this.collectionView = collectionView;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        HierarchyCollectionViewConfig that = (HierarchyCollectionViewConfig) o;
        if (widgetImplementation != null
                ? !widgetImplementation.equals(that.widgetImplementation)
                : that.widgetImplementation != null) {
            return false;
        }
        if (collectionView != null
                ? !collectionView.equals(that.collectionView)
                : that.collectionView != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = widgetImplementation != null ? widgetImplementation.hashCode() : 31;
        result = 31 * result + (collectionView != null ? collectionView.hashCode() : 31);
        return result;
    }
}
