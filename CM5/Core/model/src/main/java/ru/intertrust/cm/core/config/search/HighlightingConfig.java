package ru.intertrust.cm.core.config.search;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class HighlightingConfig implements Serializable {
    @Attribute(name = "enable", required = false)
    private Boolean isEnabled;

    @Attribute(name = "hl-require-match", required = false)
    private Boolean highlightRequireMatch;

    @Attribute(name = "hl-phrase", required = false)
    private Boolean highlightPhrase;

    @Attribute(name = "hl-multiterm", required = false)
    private Boolean highlightMultiTerm;

    @Attribute(name = "hl-pre-tag", required = false)
    private String preTag;

    @Attribute(name = "hl-post-tag", required = false)
    private String postTag;

    @Attribute(name = "hl-snippet-count", required = false)
    private Integer snippetCount;

    @Attribute(name = "hl-frag-size", required = false)
    private Integer fragmentSize;

    @ElementList(entry = "hl-raw-param", inline = true, required = false)
    private List<HighlightingRawParam> rawParams = new ArrayList<>();

    public HighlightingConfig() {
    }

    public HighlightingConfig(boolean isEnabled) {
        this.isEnabled = isEnabled;
        highlightMultiTerm = true;
        highlightPhrase = true;
        highlightRequireMatch = true;
        preTag = "<hl>";
        postTag = "</hl>";
        snippetCount = 5;
        fragmentSize = 30;
    }

    public boolean getEnabled() {
        return isEnabled != null ? isEnabled.booleanValue() : true;
    }

    public void setEnabled(Boolean enabled) {
        isEnabled = enabled;
    }

    public boolean getHighlightRequireMatch() {
        return highlightRequireMatch != null ? highlightRequireMatch.booleanValue() : false;
    }

    public void setHighlightRequireMatch(Boolean highlightRequireMatch) {
        this.highlightRequireMatch = highlightRequireMatch;
    }

    public boolean getHighlightPhrase() {
        return highlightPhrase != null ? highlightPhrase.booleanValue() : false;
    }

    public void setHighlightPhrase(Boolean highlightPhrase) {
        this.highlightPhrase = highlightPhrase;
    }

    public boolean getHighlightMultiTerm() {
        return highlightMultiTerm != null ? highlightMultiTerm.booleanValue() : false;
    }

    public void setHighlightMultiTerm(Boolean highlightMultiTerm) {
        this.highlightMultiTerm = highlightMultiTerm;
    }

    public String getPreTag() {
        return preTag;
    }

    public void setPreTag(String preTag) {
        this.preTag = preTag;
    }

    public String getPostTag() {
        return postTag;
    }

    public void setPostTag(String postTag) {
        this.postTag = postTag;
    }

    public Integer getSnippetCount() {
        return snippetCount;
    }

    public void setSnippetCount(Integer snippetCount) {
        this.snippetCount = snippetCount;
    }

    public Integer getFragmentSize() {
        return fragmentSize;
    }

    public void setFragmentSize(Integer fragmentSize) {
        this.fragmentSize = fragmentSize;
    }

    public List<HighlightingRawParam> getRawParams() {
        return rawParams;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof HighlightingConfig)) {
            return false;
        }
        HighlightingConfig other = (HighlightingConfig) obj;
        return (getEnabled() == other.getEnabled())
                && (this.getHighlightMultiTerm() == other.getHighlightMultiTerm())
                && (this.getHighlightPhrase() == other.getHighlightPhrase())
                && (this.getHighlightRequireMatch() == other.getHighlightRequireMatch())
                && (this.preTag == null ? other.preTag == null : this.preTag.equals(other.preTag))
                && (this.postTag == null ? other.postTag == null : this.postTag.equals(other.postTag))
                && (this.snippetCount == null ? other.snippetCount == null : this.snippetCount.equals(other.snippetCount))
                && (this.fragmentSize == null ? other.fragmentSize == null : this.fragmentSize.equals(other.fragmentSize))
                && (this.rawParams == null ? other.rawParams == null : this.rawParams.equals(other.rawParams));
    }

    @Override
    public int hashCode() {
        int hash = getEnabled() ? 1234 : 5678;
        hash *= 31 ^ (getHighlightRequireMatch() ? 1 : 2);
        hash *= 31 ^ (getHighlightMultiTerm() ? 1 : 2);
        hash *= 31 ^ (getHighlightPhrase() ? 1 : 2);
        hash *= 31 ^ (preTag != null ? preTag.hashCode() : 1);
        hash *= 31 ^ (postTag != null ? postTag.hashCode() : 1);
        hash *= 31 ^ (snippetCount != null ? snippetCount.hashCode() : 1);
        hash *= 31 ^ (fragmentSize != null ? fragmentSize.hashCode() : 1);
        hash *= 31 ^ (rawParams != null ? rawParams.hashCode() : 1);
        return hash;
    }

}
