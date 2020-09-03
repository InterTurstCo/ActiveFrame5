package ru.intertrust.cm.core.business.impl.search;

import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.config.search.IndexedFieldConfig;

import java.util.*;

public class TextSearchFieldType implements SearchFieldType {// extends SimpleSearchFieldType {

    private Set<String> languages;
    private boolean multiValued;
    private IndexedFieldConfig.SearchBy searchBy;

    public TextSearchFieldType(Collection<String> languages) {
        this(languages, false);
    }

    public TextSearchFieldType(Collection<String> languages, boolean multiValued) {
        this(languages, multiValued, IndexedFieldConfig.SearchBy.WORDS);
    }

    public TextSearchFieldType(Collection<String> languages, boolean multiValued, IndexedFieldConfig.SearchBy searchBy) {
        //super(Type.TEXT, multiValued);
        this.languages = new HashSet<>();
        this.languages.addAll(languages);
        this.multiValued = multiValued;
        this.searchBy = searchBy;
    }

    @Override
    public boolean supportsFilter(SearchFilter filter) {
        return filter instanceof TextSearchFilter || filter instanceof EmptyValueFilter;
    }

    @Override
    public Collection<String> getSolrFieldNames(String field) {
        ArrayList<String> result = new ArrayList<>(languages.size());
        for (String langId : languages) {
            if (!"".equals(langId)) {
                if (searchBy == IndexedFieldConfig.SearchBy.EXACTMATCH) {
                    result.add(new StringBuilder()
                            .append(SolrFields.EXACT_MATCH_FIELD)
                            .append(multiValued ? "s_" : "_")
                            .append(langId)
                            .append("_")
                            .append(Case.toLower(field))
                            .toString());
                } else {
                    result.add(new StringBuilder()
                            .append(SolrFields.FIELD_PREFIX)
                            .append(langId)
                            .append(multiValued ? "s_" : "_")
                            .append(Case.toLower(field))
                            .toString());
                }
            }
        }
        if (searchBy == IndexedFieldConfig.SearchBy.EXACTMATCH) {
            result.add(new StringBuilder()
                    .append(SolrFields.EXACT_MATCH_FIELD)
                    .append(multiValued ? "s_" : "_")
                    .append("_")
                    .append(Case.toLower(field))
                    .toString());
        } else {
            result.add(new StringBuilder()
                    .append(SolrFields.FIELD_PREFIX)
                    .append(multiValued ? "ts_" : "t_")
                    .append(Case.toLower(field))
                    .toString());
        }
        return result;
    }

    @Override
    public FieldType getDataFieldType() {
        return FieldType.STRING;
    }

    @Override
    public boolean isQuote() {
        return (searchBy == IndexedFieldConfig.SearchBy.SUBSTRING)
                || (searchBy == IndexedFieldConfig.SearchBy.EXACTMATCH);
    }

    @Override
    public boolean isTextType() {
        return true;
    }

    public Set<String> getLanguages() {
        return Collections.unmodifiableSet(languages);
    }

    public boolean isMultiValued() {
        return multiValued;
    }

    public IndexedFieldConfig.SearchBy getSearchBy() {
        return searchBy;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != getClass()) return false;
        TextSearchFieldType that = (TextSearchFieldType) obj;
        return this.languages.equals(that.languages)
                && this.multiValued == that.multiValued
                && this.searchBy == that.searchBy;
    }

    @Override
    public int hashCode() {
        int hash = languages.hashCode();
        if (multiValued) {
            hash ^= 0x5A5A5A5A;
        }
        hash *= 31 ^ (searchBy != null ? searchBy.hashCode() : 0);
        return hash;
    }

}
