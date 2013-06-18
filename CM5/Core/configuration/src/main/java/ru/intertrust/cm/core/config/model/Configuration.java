package ru.intertrust.cm.core.config.model;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.ElementListUnion;
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
public class Configuration implements Serializable {
    @ElementListUnion({
            @ElementList(entry="domain-object", type=DomainObjectConfig.class, inline=true),
            @ElementList(entry="collection", type=CollectionConfig.class, inline=true)
    })
    private List configurationList;

    public List getConfigurationList() {
        if(configurationList == null) {
            configurationList = new ArrayList();
        }
        return configurationList;
    }

    public void setConfigurationList(List configurationList) {
        this.configurationList = configurationList;
    }
}
