package ru.intertrust.cm.core.config.search;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;

import java.io.Serializable;

public class IndexedFieldConfig implements Serializable {

    public enum SearchBy {
        WORDS("words"),
        SUBSTRING("substring");

        public final String xmlValue;
        private SearchBy(String xmlValue) {
            this.xmlValue = xmlValue;
        }
        static SearchBy fromXmlValue(String xmlValue) {
            for (SearchBy value : values()) {
                if (value.xmlValue.equalsIgnoreCase(xmlValue)) {
                    return value;
                }
            }
            throw new IllegalArgumentException("Unknown search-by value: " + xmlValue);
        }
    }

    @Attribute(required = true)
    private String name;

    @Attribute(required = false)
    private String language;

    //@Attribute(name = "search-by", required = false) - declared in getter/setter
    private SearchBy searchBy;

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

    @Attribute(name = "search-by", required = false)
    public String getSearchByString() {
        return searchBy == null ? null : searchBy.xmlValue;
    }

    public SearchBy getSearchBy() {
        return searchBy == null ? SearchBy.WORDS : searchBy;
    }

    @Attribute(name = "search-by", required = false)
    public void setSearchByString(String searchBy) {
        this.searchBy = SearchBy.fromXmlValue(searchBy);
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
