package ru.intertrust.cm.core.config.model.gui.navigation;

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

    public CollectionViewerConfig getCollectionViewerConfig() {
        return collectionViewerConfig;
    }

    public void setCollectionViewerConfig(CollectionViewerConfig collectionViewerConfig) {
        this.collectionViewerConfig = collectionViewerConfig;
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

        if (collectionViewerConfig != null ? !collectionViewerConfig.equals(that.collectionViewerConfig) : that.
                collectionViewerConfig != null)  {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return collectionViewerConfig != null ? collectionViewerConfig.hashCode() : 0;
    }

    @Override
    public String getComponentName() {
        return "domain.object.surfer";
    }
}

