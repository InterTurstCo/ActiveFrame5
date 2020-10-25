package ru.intertrust.cm.core.config.search;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;

import java.io.Serializable;

public class IndexedFieldConfig implements Serializable {

    public enum SearchBy {
        WORDS("words"),
        SUBSTRING("substring"),
        EXACTMATCH("exactmatch");

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

    @Element(name = "script", required = false)
    private IndexedFieldScriptConfig scriptConfig;

    @Attribute(name = "show-in-results", required = false)
    private Boolean showInResults;

    @Attribute(name = "multi-valued", required = false)
    private Boolean multiValued;

    @Attribute(name = "target-field-name", required = false)
    private String targetFieldName;

    public String getTargetFieldName() {
        return targetFieldName != null && !targetFieldName.isEmpty() ?  targetFieldName : name;
    }

    public String getName() {
        return name;
    }

    public String getLanguage() {
        return language;
    }

    public String getDoel() {
        return doel;
    }
    
    public IndexedFieldScriptConfig getScriptConfig() {
        return scriptConfig;
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
        hash = hash * 31 ^ (scriptConfig != null ? scriptConfig.hashCode() : 0);
        hash = hash * 31 ^ (solrPrefix != null ? solrPrefix.hashCode() : 0);
        hash = hash * 31 ^ (showInResults != null ? showInResults.hashCode() : 0);
        hash = hash * 31 ^ (multiValued != null ? multiValued.hashCode() : 0);
        hash = hash * 31 ^ (targetFieldName != null ? targetFieldName.hashCode() : 0);
        hash = hash * 31 ^ (searchBy != null ? searchBy.hashCode() : 0);
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
                && (scriptConfig == null ? other.scriptConfig == null : scriptConfig.equals(other.scriptConfig))
                && (solrPrefix == null ? other.solrPrefix == null : solrPrefix.equals(other.solrPrefix))
                && (showInResults == null ? other.showInResults == null : showInResults.equals(other.showInResults))
                && (targetFieldName == null ? other.targetFieldName == null : targetFieldName.equals(other.targetFieldName))
                && (multiValued == null ? other.multiValued == null : multiValued.equals(other.multiValued))
                && (searchBy == null ? other.searchBy == null : searchBy.equals(other.searchBy));
    }
}
