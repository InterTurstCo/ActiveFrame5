package ru.intertrust.cm.core.config.search;

import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import ru.intertrust.cm.core.config.base.TopLevelConfig;

@Root(name = "search-area")
public class SearchAreaConfig implements TopLevelConfig {

    @Attribute(required = true)
    private String name;

    @ElementList(entry = "target-domain-object", inline = true)
    private List<IndexedDomainObjectConfig> targetObjects;

    @Override
    public String getName() {
        return name;
    }

    public List<IndexedDomainObjectConfig> getTargetObjects() {
        return targetObjects;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || !getClass().equals(obj.getClass())) {
            return false;
        }
        return name.equals(((SearchAreaConfig) obj).name);
    }
}
