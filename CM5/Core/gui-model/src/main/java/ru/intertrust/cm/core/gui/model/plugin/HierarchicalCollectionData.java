package ru.intertrust.cm.core.gui.model.plugin;

import ru.intertrust.cm.core.config.gui.navigation.DomainObjectSurferConfig;
import ru.intertrust.cm.core.config.gui.navigation.LinkConfig;

/**
 * @author Lesia Puhova
 *         Date: 23.09.14
 *         Time: 13:48
 */
public class HierarchicalCollectionData extends PluginData {

    private DomainObjectSurferConfig domainObjectSurferConfig;
    private LinkConfig hierarchicalLink;

    public DomainObjectSurferConfig getDomainObjectSurferConfig() {
        return domainObjectSurferConfig;
    }

    public void setDomainObjectSurferConfig(DomainObjectSurferConfig domainObjectSurferConfig) {
        this.domainObjectSurferConfig = domainObjectSurferConfig;
    }


    public LinkConfig getHierarchicalLink() {
        return hierarchicalLink;
    }

    public void setHierarchicalLink(LinkConfig hierarchicalLink) {
        this.hierarchicalLink = hierarchicalLink;
    }

}
