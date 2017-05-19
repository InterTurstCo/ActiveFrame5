package ru.intertrust.cm.core.business.impl.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import ru.intertrust.cm.core.business.api.dto.SearchFilter;

public class SpecialTextSearchFieldType implements SearchFieldType {

    private Set<String> languages;

    public SpecialTextSearchFieldType(Collection<String> languages) {
        this.languages = new HashSet<>(languages.size());
        this.languages.addAll(languages);
        this.languages.add("");
    }

    @Override
    public boolean supportsFilter(SearchFilter filter) {
        return SearchFilter.EVERYWHERE.equals(filter.getFieldName())
                || SearchFilter.CONTENT.equals(filter.getFieldName());
    }

    @Override
    public Collection<String> getSolrFieldNames(String field, boolean strict) {
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
            if (langId != "") {
                result.add(new StringBuilder()
                        .append(baseName)
                        .append("_")
                        .append(langId)
                        .toString());
            }
        }
        if (!strict || languages.contains("")) {
            result.add(baseName);
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != getClass()) return false;
        SpecialTextSearchFieldType that = (SpecialTextSearchFieldType) obj;
        return this.languages.equals(that.languages);
    }

    @Override
    public int hashCode() {
        return languages.hashCode();
    }

}
