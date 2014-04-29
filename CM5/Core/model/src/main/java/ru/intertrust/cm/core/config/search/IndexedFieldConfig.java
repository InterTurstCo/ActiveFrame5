package ru.intertrust.cm.core.config.search;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;

import java.io.Serializable;

public class IndexedFieldConfig implements Serializable {

    @Attribute(required = true)
    private String name;

    @Attribute(required = false)
    private String language;

    @Element(required = false)
    private String doel;

    @Element(required = false)
    private String script;

    public String getName() {
        return name;
    }

    public String getLanguage() {
        return language;
    }

    public String getDoel() {
        return doel;
    }
    
    public String getScript() {
        return script;
    }

    @Override
    public int hashCode() {
        int hash = name.hashCode();
        hash = hash * 31 ^ (language != null ? language.hashCode() : 0);
        hash = hash * 31 ^ (doel != null ? doel.hashCode() : 0);
        hash = hash * 31 ^ (script != null ? script.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || !getClass().equals(obj.getClass())) {
            return false;
        }
        IndexedFieldConfig other = (IndexedFieldConfig) obj;
        return name.equals(other.name)
                && (language == null ? other.language == null : other.language.equals(language))
                && (doel == null ? other.doel == null : doel.equals(other.doel))
                && (script == null ? other.script == null : script.equals(other.script));
    }
}
