package ru.intertrust.cm.core.business.impl.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import ru.intertrust.cm.core.business.api.dto.EmptyValueFilter;
import ru.intertrust.cm.core.business.api.dto.SearchFilter;
import ru.intertrust.cm.core.business.api.dto.TextSearchFilter;

public class TextSearchFieldType implements SearchFieldType {// extends SimpleSearchFieldType {

    private Set<String> languages;
    private boolean multiValued;
    private boolean searchBySubstring;

    public TextSearchFieldType(Collection<String> languages, boolean multiValued, boolean searchBySubstring) {
        //super(Type.TEXT, multiValued);
        this.languages = new HashSet<>();
        this.languages.addAll(languages);
        this.multiValued = multiValued;
        this.searchBySubstring = searchBySubstring;
    }

    public void addLanguage(String langId) {
        this.languages.add(langId);
    }

    @Override
    public boolean supportsFilter(SearchFilter filter) {
        return filter instanceof TextSearchFilter || filter instanceof EmptyValueFilter;
    }

    @Override
    public Collection<String> getSolrFieldNames(String field, boolean strict) {
        ArrayList<String> result = new ArrayList<>(languages.size());
        for (String langId : languages) {
            if (langId != "") {
                result.add(new StringBuilder()
                        .append(SolrFields.FIELD_PREFIX)
                        .append(langId)
                        .append(multiValued ? "s_" : "_")
                        .append(field.toLowerCase())
                        .toString());
            }
        }
        if (!strict || languages.contains("")) {
            result.add(new StringBuilder()
                    .append(SolrFields.FIELD_PREFIX)
                    .append(multiValued ? "ts_" : "t_")
                    .append(field.toLowerCase())
                    .toString());
        }
        return result;
    }

    public Set<String> getLanguages() {
        return Collections.unmodifiableSet(languages);
    }

    public boolean isMultiValued() {
        return multiValued;
    }

    public boolean isSearchBySubstring() {
        return searchBySubstring;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != getClass()) return false;
        TextSearchFieldType that = (TextSearchFieldType) obj;
        return this.languages.equals(that.languages)
                && this.multiValued == that.multiValued
                && this.searchBySubstring == that.searchBySubstring;
    }

    @Override
    public int hashCode() {
        int hash = languages.hashCode();
        if (multiValued) {
            hash ^= 0x5A5A5A5A;
        }
        if (searchBySubstring) {
            hash ^= 0xA5A5A5A5;
        }
        return hash;
    }

}
