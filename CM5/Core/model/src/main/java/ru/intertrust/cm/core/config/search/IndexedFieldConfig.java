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

    @Attribute(name = "solr-prefix", required = false)
    private String solrPrefix;

    @Element(required = false)
    private String doel;

    @Element(required = false)
    private String script;

    @Attribute(name = "show-in-results", required = false)
    private Boolean showInResults;

    @Attribute(name = "multi-valued", required = false)
    private Boolean multiValued;

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

    public String getSolrPrefix() {
        return solrPrefix;
    }

    public boolean getShowInResults() {
        return showInResults != null ? showInResults.booleanValue() : false;
    }

    public boolean getMultiValued() {
        return multiValued != null ? multiValued.booleanValue() : false;
    }

    @Override
    public int hashCode() {
        int hash = name.hashCode();
        hash = hash * 31 ^ (language != null ? language.hashCode() : 0);
        hash = hash * 31 ^ (doel != null ? doel.hashCode() : 0);
        hash = hash * 31 ^ (script != null ? script.hashCode() : 0);
        hash = hash * 31 ^ (solrPrefix != null ? solrPrefix.hashCode() : 0);
        hash = hash * 31 ^ (showInResults != null ? showInResults.hashCode() : 0);
        hash = hash * 31 ^ (multiValued != null ? multiValued.hashCode() : 0);
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
                && (language == null ? other.language == null : language.equals(other.language))
                && (doel == null ? other.doel == null : doel.equals(other.doel))
                && (script == null ? other.script == null : script.equals(other.script))
                && (solrPrefix == null ? other.solrPrefix == null : solrPrefix.equals(other.solrPrefix))
                && (showInResults == null ? other.showInResults == null : showInResults.equals(other.showInResults))
                && (multiValued == null ? other.multiValued == null : multiValued.equals(other.multiValued));
    }
}
