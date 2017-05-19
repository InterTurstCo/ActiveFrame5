package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.config.gui.form.widget.filter.extra.CollectionExtraFiltersConfig;
import ru.intertrust.cm.core.config.gui.navigation.CollectionRefConfig;
import ru.intertrust.cm.core.config.gui.navigation.CollectionViewRefConfig;
import ru.intertrust.cm.core.config.gui.navigation.DefaultSortCriteriaConfig;
import ru.intertrust.cm.core.config.gui.navigation.InitialFiltersConfig;

/**
 * Created by Ravil on 18.05.2017.
 */
@Root(name = "list-cell")
public class ListCellConfig extends WidgetConfig {

    @Attribute(name = "renderFactoryComponentName", required = true)
    protected String renderFactoryComponentName;

    @Attribute(name = "renderTypeName", required = true)
    protected String renderTypeName;

    @Attribute(name = "headerValue", required = true)
    protected String headerValue;

    @Attribute(name = "counterRequired", required = false)
    protected Boolean counterRequired;

    @Override
    public String getComponentName() {
        return "list-cell";
    }

    @Element(name = "collection-view-ref", required = false)
    private CollectionViewRefConfig collectionViewRefConfig;

    @Element(name = "collection-ref", required = false)
    private CollectionRefConfig collectionRefConfig;

    @Element(name = "default-sort-criteria", required = false)
    private DefaultSortCriteriaConfig defaultSortCriteriaConfig;

    @Element(name = "collection-extra-filters", required = false)
    private CollectionExtraFiltersConfig collectionExtraFiltersConfig;


    public String getRenderFactoryComponentName() {
        return renderFactoryComponentName;
    }

    public void setRenderFactoryComponentName(String renderFactoryComponentName) {
        this.renderFactoryComponentName = renderFactoryComponentName;
    }

    public String getRenderTypeName() {
        return renderTypeName;
    }

    public Boolean getCounterRequired() {
        return counterRequired;
    }

    public void setCounterRequired(Boolean counterRequired) {
        this.counterRequired = counterRequired;
    }

    public void setRenderTypeName(String renderTypeName) {
        this.renderTypeName = renderTypeName;
    }

    public CollectionViewRefConfig getCollectionViewRefConfig() {
        return collectionViewRefConfig;
    }

    public String getHeaderValue() {
        return headerValue;
    }

    public void setHeaderValue(String headerValue) {
        this.headerValue = headerValue;
    }

    public void setCollectionViewRefConfig(CollectionViewRefConfig collectionViewRefConfig) {
        this.collectionViewRefConfig = collectionViewRefConfig;
    }

    public CollectionRefConfig getCollectionRefConfig() {
        return collectionRefConfig;
    }

    public void setCollectionRefConfig(CollectionRefConfig collectionRefConfig) {
        this.collectionRefConfig = collectionRefConfig;
    }

    public DefaultSortCriteriaConfig getDefaultSortCriteriaConfig() {
        return defaultSortCriteriaConfig;
    }

    public void setDefaultSortCriteriaConfig(DefaultSortCriteriaConfig defaultSortCriteriaConfig) {
        this.defaultSortCriteriaConfig = defaultSortCriteriaConfig;
    }

    public CollectionExtraFiltersConfig getCollectionExtraFiltersConfig() {
        return collectionExtraFiltersConfig;
    }

    public void setCollectionExtraFiltersConfig(CollectionExtraFiltersConfig collectionExtraFiltersConfig) {
        this.collectionExtraFiltersConfig = collectionExtraFiltersConfig;
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        ListCellConfig that = (ListCellConfig) o;

        if (collectionExtraFiltersConfig != null ? !collectionExtraFiltersConfig.equals(that.collectionExtraFiltersConfig) : that.
                collectionExtraFiltersConfig != null) {
            return false;
        }
        if (defaultSortCriteriaConfig != null ? !defaultSortCriteriaConfig.equals(that.defaultSortCriteriaConfig) : that.
                defaultSortCriteriaConfig != null) {
            return false;
        }
        if (collectionRefConfig != null ? !collectionRefConfig.equals(that.collectionRefConfig) : that.
                collectionRefConfig != null) {
            return false;
        }
        if (collectionViewRefConfig != null ? !collectionViewRefConfig.equals(that.collectionViewRefConfig) : that.
                collectionViewRefConfig != null) {
            return false;
        }
        if (renderTypeName != null ? !renderTypeName.equals(that.renderTypeName) : that.renderTypeName != null) return false;
        if (renderFactoryComponentName != null ? !renderFactoryComponentName.equals(that.renderFactoryComponentName) : that.renderFactoryComponentName != null) return false;
        if (headerValue != null ? !headerValue.equals(that.headerValue) : that.headerValue != null) return false;
        if (counterRequired != null ? !counterRequired.equals(that.counterRequired) : that.counterRequired != null) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (collectionExtraFiltersConfig != null ? collectionExtraFiltersConfig.hashCode() : 0);
        result = 31 * result + (defaultSortCriteriaConfig != null ? defaultSortCriteriaConfig.hashCode() : 0);
        result = 31 * result + (collectionRefConfig != null ? collectionRefConfig.hashCode() : 0);
        result = 31 * result + (collectionViewRefConfig != null ? collectionViewRefConfig.hashCode() : 0);
        result = 31 * result + (renderTypeName != null ? renderTypeName.hashCode() : 0);
        result = 31 * result + (renderFactoryComponentName != null ? renderFactoryComponentName.hashCode() : 0);
        result = 31 * result + (headerValue != null ? headerValue.hashCode() : 0);
        result = 31 * result + (counterRequired != null ? counterRequired.hashCode() : 0);

        return result;
    }
}
