package ru.intertrust.cm.core.config.model;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Denis Mitavskiy
 *         Date: 5/2/13
 *         Time: 12:05 PM
 */
@Root
public class DomainObjectsConfiguration implements Serializable {
    @ElementList(inline = true)
    private List<DomainObjectConfig> domainObjectConfigs;

    public List<DomainObjectConfig> getDomainObjectConfigs() {
        if(domainObjectConfigs == null) {
            domainObjectConfigs = new ArrayList<DomainObjectConfig>();
        }
        return domainObjectConfigs;
    }

    public void setDomainObjectConfigs(List<DomainObjectConfig> domainObjectConfigs) {
        this.domainObjectConfigs = domainObjectConfigs;
    }




}
