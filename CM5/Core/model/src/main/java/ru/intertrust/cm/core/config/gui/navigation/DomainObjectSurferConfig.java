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

    @Element(name = "toggle-edit", required = false)
    private boolean toggleEdit;

    @Attribute(name = "domain-object-type-to-create", required = false)
    private String domainObjectTypeToCreate;

    public CollectionViewerConfig getCollectionViewerConfig() {
        return collectionViewerConfig;
    }

    public void setCollectionViewerConfig(CollectionViewerConfig collectionViewerConfig) {
        this.collectionViewerConfig = collectionViewerConfig;
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

        if (collectionViewerConfig != null ? !collectionViewerConfig.equals(that.collectionViewerConfig) : that.collectionViewerConfig != null) {
            return false;
        }
        if (domainObjectTypeToCreate != null ? !domainObjectTypeToCreate.equals(that.domainObjectTypeToCreate) : that.domainObjectTypeToCreate != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = collectionViewerConfig != null ? collectionViewerConfig.hashCode() : 0;
        result = 31 * result + (domainObjectTypeToCreate != null ? domainObjectTypeToCreate.hashCode() : 0);
        return result;
    }

    @Override
    public String getComponentName() {
        return "domain.object.surfer.plugin";
    }
}

