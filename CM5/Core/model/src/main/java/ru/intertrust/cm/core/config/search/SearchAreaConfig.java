package ru.intertrust.cm.core.config.search;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.config.base.TopLevelConfig;

import java.util.List;
import java.util.Objects;

@Root(name = "search-area")
public class SearchAreaConfig implements TopLevelConfig {

    @Attribute(required = true)
    private String name;

    @Attribute(name = "replace", required = false)
    private String replacementPolicy;

    @ElementList(entry = "target-domain-object", inline = true)
    private List<TargetDomainObjectConfig> targetObjects;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ExtensionPolicy getReplacementPolicy() {
        return ExtensionPolicy.fromString(replacementPolicy);
    }

    @Override
    public ExtensionPolicy getCreationPolicy() {
        return ExtensionPolicy.Runtime;
    }

    public List<TargetDomainObjectConfig> getTargetObjects() {
        return targetObjects;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SearchAreaConfig that = (SearchAreaConfig) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(replacementPolicy, that.replacementPolicy) &&
                Objects.equals(targetObjects, that.targetObjects);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, replacementPolicy, targetObjects);
    }
}
