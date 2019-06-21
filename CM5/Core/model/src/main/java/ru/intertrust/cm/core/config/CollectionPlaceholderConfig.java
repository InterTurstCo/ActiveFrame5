package ru.intertrust.cm.core.config;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Text;

import ru.intertrust.cm.core.config.base.TopLevelConfig;

/**
 * Модель именованных вставок в коллекции
 * @author larin
 *
 */
@Root(name = "collection-placeholder")
public class CollectionPlaceholderConfig implements TopLevelConfig{
    @Attribute(required = true)
    private String name;
    
    @Attribute(name = "replace", required = false)
    private String replacementPolicy;  
    
    @Text
    private String body;
    
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

    public String getBody() {
        return body;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setReplacementPolicy(String replacementPolicy) {
        this.replacementPolicy = replacementPolicy;
    }

    public void setBody(String body) {
        this.body = body;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((body == null) ? 0 : body.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((replacementPolicy == null) ? 0 : replacementPolicy.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CollectionPlaceholderConfig other = (CollectionPlaceholderConfig) obj;
        if (body == null) {
            if (other.body != null)
                return false;
        } else if (!body.equals(other.body))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (replacementPolicy == null) {
            if (other.replacementPolicy != null)
                return false;
        } else if (!replacementPolicy.equals(other.replacementPolicy))
            return false;
        return true;
    }
}
