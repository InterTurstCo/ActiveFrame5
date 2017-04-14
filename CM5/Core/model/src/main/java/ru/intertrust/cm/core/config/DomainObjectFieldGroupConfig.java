package ru.intertrust.cm.core.config;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.config.base.TopLevelConfig;

/**
 * 
 * @author atsvetkov
 *
 */
@Root(name = "field-group")
public class DomainObjectFieldGroupConfig extends DomainObjectFieldsConfig implements TopLevelConfig {

    @Attribute(required = true)
    private String name;

    @Attribute(name = "replace", required = false)
    private String replacementPolicy;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ExtensionPolicy getReplacementPolicy() {
        return ExtensionPolicy.None;
    }

    @Override
    public ExtensionPolicy getCreationPolicy() {
        return ExtensionPolicy.None;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        DomainObjectFieldGroupConfig that = (DomainObjectFieldGroupConfig) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (replacementPolicy != null ? !replacementPolicy.equals(that.replacementPolicy) : that.replacementPolicy != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}
