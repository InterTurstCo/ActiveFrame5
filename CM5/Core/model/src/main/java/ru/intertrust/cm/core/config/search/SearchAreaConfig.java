package ru.intertrust.cm.core.config.search;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
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

    @Element(name = "solr-server-url", required = false)
    private String solrServerKey;

    @Element(name = "target-filter-name", required = false)
    private String targetFilterName;

    @ElementList(entry = "target-domain-object", inline = true)
    private List<TargetDomainObjectConfig> targetObjects;

    @Element(name = "highlighting-config", required = false)
    private HighlightingConfig highlightingConfig;

    private IndexedFieldConfig.SearchBy contentSearchBy;

    @Element(name = "content-search-by", required = false)
    public String getContentSearchByString() {
        return contentSearchBy == null ? null : contentSearchBy.xmlValue;
    }

    @Element(name = "content-search-by", required = false)
    public void setContentSearchByString(String contentSearchBy) {
        this.contentSearchBy = IndexedFieldConfig.SearchBy.fromXmlValue(contentSearchBy);
    }

    public IndexedFieldConfig.SearchBy getContentSearchBy() {
        return contentSearchBy == null ? IndexedFieldConfig.SearchBy.SUBSTRING : contentSearchBy;
    }

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

    public String getSolrServerKey() {
        return solrServerKey;
    }

    public String getTargetFilterName() {
        return targetFilterName;
    }

    public HighlightingConfig getHighlightingConfig() {
        return highlightingConfig;
    }

    public void setHighlightingConfig(HighlightingConfig highlightingConfig) {
        this.highlightingConfig = highlightingConfig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SearchAreaConfig that = (SearchAreaConfig) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(replacementPolicy, that.replacementPolicy) &&
                Objects.equals(targetObjects, that.targetObjects) &&
                Objects.equals(solrServerKey, that.solrServerKey) &&
                Objects.equals(targetFilterName, that.targetFilterName) &&
                Objects.equals(highlightingConfig, that.highlightingConfig) &&
                Objects.equals(contentSearchBy, that.contentSearchBy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, replacementPolicy, targetObjects,
                (solrServerKey == null ? "" : solrServerKey),
                (targetFilterName == null ? "" : targetFilterName),
                (highlightingConfig != null ? "" : highlightingConfig),
                (contentSearchBy != null ? "" : contentSearchBy));
    }

}
