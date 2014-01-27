package ru.intertrust.cm.core.config.search;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.config.base.TopLevelConfig;

import java.util.List;

@Root(name = "search-area")
public class SearchAreaConfig implements TopLevelConfig {

    @Attribute(required = true)
    private String name;

    @ElementList(entry = "target-domain-object", inline = true)
    private List<TargetDomainObjectConfig> targetObjects;

    @Override
    public String getName() {
        return name;
    }

    public List<TargetDomainObjectConfig> getTargetObjects() {
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
