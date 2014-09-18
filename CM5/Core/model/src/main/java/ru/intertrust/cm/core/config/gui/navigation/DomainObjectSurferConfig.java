package ru.intertrust.cm.core.config.gui.navigation;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 10/9/13
 *         Time: 12:05 PM
 */
@Root(name = "domain-object-surfer")
public class DomainObjectSurferConfig extends PluginConfig {
    @Element(name = "collection-viewer")
    private CollectionViewerConfig collectionViewerConfig;

    @Element(name = "form-viewer", required = false)
    private FormViewerConfig formViewerConfig;

    @Element(name = "toggle-edit", required = false)
    private boolean toggleEdit = true;

    @Attribute(name = "domain-object-type-to-create", required = false)
    private String domainObjectTypeToCreate;

    public CollectionViewerConfig getCollectionViewerConfig() {
        return collectionViewerConfig;
    }

    public void setCollectionViewerConfig(CollectionViewerConfig collectionViewerConfig) {
        this.collectionViewerConfig = collectionViewerConfig;
    }

    public FormViewerConfig getFormViewerConfig() {
        return formViewerConfig;
    }

    public void setFormViewerConfig(FormViewerConfig formViewerConfig) {
        this.formViewerConfig = formViewerConfig;
    }

    public boolean isToggleEdit() {
        return toggleEdit;
    }

    public String getDomainObjectTypeToCreate() {
        return domainObjectTypeToCreate;
    }

    public void setDomainObjectTypeToCreate(String domainObjectTypeToCreate) {
        this.domainObjectTypeToCreate = domainObjectTypeToCreate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DomainObjectSurferConfig that = (DomainObjectSurferConfig) o;
        if (collectionViewerConfig != null
                ? !collectionViewerConfig.equals(that.collectionViewerConfig)
                : that.collectionViewerConfig != null) {
            return false;
        }
        if (formViewerConfig != null
                ? !formViewerConfig.equals(that.formViewerConfig)
                : that.formViewerConfig != null) {
            return false;
        }
        if (domainObjectTypeToCreate != null
                ? !domainObjectTypeToCreate.equals(that.domainObjectTypeToCreate)
                : that.domainObjectTypeToCreate != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = collectionViewerConfig != null ? collectionViewerConfig.hashCode() : 31;
        result = 31 * result + (formViewerConfig != null ? formViewerConfig.hashCode() : 31);
        result = 31 * result + (domainObjectTypeToCreate != null ? domainObjectTypeToCreate.hashCode() : 31);
        return result;
    }

    @Override
    public String getComponentName() {
        return "domain.object.surfer.plugin";
    }
}

