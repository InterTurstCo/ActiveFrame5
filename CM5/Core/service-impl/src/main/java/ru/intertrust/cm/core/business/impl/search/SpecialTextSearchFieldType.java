package ru.intertrust.cm.core.business.impl.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import ru.intertrust.cm.core.business.api.dto.FieldType;
import ru.intertrust.cm.core.business.api.dto.SearchFilter;
import ru.intertrust.cm.core.config.search.IndexedFieldConfig;

public class SpecialTextSearchFieldType implements SearchFieldType {

    private Set<String> languages;
    private IndexedFieldConfig.SearchBy searchBy;

    public SpecialTextSearchFieldType(Collection<String> languages) {
        this(languages, IndexedFieldConfig.SearchBy.SUBSTRING);
    }

    public SpecialTextSearchFieldType(Collection<String> languages, IndexedFieldConfig.SearchBy searchBy) {
        this.languages = new HashSet<>(languages.size());
        this.languages.addAll(languages);
        this.searchBy = searchBy;
        // this.languages.add("");
    }

    @Override
    public boolean supportsFilter(SearchFilter filter) {
        return SearchFilter.EVERYWHERE.equals(filter.getFieldName())
                || SearchFilter.CONTENT.equals(filter.getFieldName());
    }

    @Override
    public Collection<String> getSolrFieldNames(String field) {
        String baseName;
        if (SearchFilter.EVERYWHERE.equals(field)) {
            baseName = SolrFields.EVERYTHING;
        } else if (SearchFilter.CONTENT.equals(field)) {
            baseName = SolrFields.CONTENT;
        } else {
            throw new IllegalArgumentException("Not a special field: " + field);
        }
        ArrayList<String> result = new ArrayList<>(languages.size() + 1);
        for (String langId : languages) {
            if (!"".equals(langId)) {
                result.add(new StringBuilder()
                        .append(baseName)
                        .append("_")
                        .append(langId)
                        .toString());
            }
        }
        result.add(baseName);
        return result;
    }

    @Override
    public FieldType getDataFieldType() {
        // Highlighting
        return FieldType.LIST;
    }

    @Override
    public boolean isQuote() {
        return IndexedFieldConfig.SearchBy.SUBSTRING.equals(searchBy);
    }

    @Override
    public boolean isTextType() {
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != getClass()) return false;
        SpecialTextSearchFieldType that = (SpecialTextSearchFieldType) obj;
        return this.languages.equals(that.languages)
                && (this.searchBy == null ? that.searchBy == null : this.searchBy.equals(that.searchBy));
    }

    @Override
    public int hashCode() {
        int hash = languages.hashCode();
        hash *= 31 ^ (searchBy != null ? searchBy.hashCode() : 0);
        return hash;
    }

}
