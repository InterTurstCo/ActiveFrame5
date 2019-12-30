package ru.intertrust.cm.core.business.impl.search;

import ru.intertrust.cm.core.business.api.dto.Case;
import ru.intertrust.cm.core.business.api.dto.EmptyValueFilter;
import ru.intertrust.cm.core.business.api.dto.SearchFilter;
import ru.intertrust.cm.core.business.api.dto.TextSearchFilter;
import ru.intertrust.cm.core.config.search.IndexedFieldConfig;

import java.util.*;

public class TextSearchFieldType implements SearchFieldType {// extends SimpleSearchFieldType {

    private Set<String> languages;
    private boolean multiValued;
    private boolean searchBySubstring;
    private boolean searchByExactMatch; // флаг того, что также будет производиться поиск по полному совпадению значения

    
    public TextSearchFieldType(Collection<String> languages, boolean multiValued, boolean searchBySubstring) {
        //super(Type.TEXT, multiValued);
        this.languages = new HashSet<>();
        this.languages.addAll(languages);
        this.multiValued = multiValued;
        this.searchBySubstring = searchBySubstring;
    }

    public TextSearchFieldType(Collection<String> languages, boolean multiValued, IndexedFieldConfig.SearchBy searchBy) {
        this.languages = new HashSet<>();
        this.languages.addAll(languages);
        this.multiValued = multiValued;
        this.searchBySubstring = IndexedFieldConfig.SearchBy.SUBSTRING.equals(searchBy);
        this.searchByExactMatch = IndexedFieldConfig.SearchBy.EXACT_MATCH.equals(searchBy);
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
            if (!"".equals(langId)) {
                result.add(new StringBuilder()
                        .append(SolrFields.FIELD_PREFIX)
                        .append(langId)
                        .append(multiValued ? "s_" : "_")
                        .append(Case.toLower(field))
                        .toString());
            }
        }
        if (!strict || languages.contains("")) {
            result.add(new StringBuilder()
                    .append(SolrFields.FIELD_PREFIX)
                    .append(multiValued ? "ts_" : "t_")
                    .append(Case.toLower(field))
                    .toString());
        }
        if (searchByExactMatch) {
            result.add(new StringBuilder()
                    .append(Case.toLower(field))
                    .append("_s")
                    .toString());
        }

        return result;
    }

    public Collection<String> getSolrSearchFieldNames(String field, boolean strict) {
        Collection<String> result;
        if (searchByExactMatch) {
            result = new ArrayList<>(languages.size());
            result.add(new StringBuilder()
                    .append(Case.toLower(field))
                    .append("_s")
                    .toString());
        } else {
            result = getSolrFieldNames(field, strict);
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

    public boolean isSearchByExactMatch() {
        return searchByExactMatch;
    }

    public void setSearchByExactMatch(boolean searchByExactMatch) {
        this.searchByExactMatch = searchByExactMatch;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != getClass()) return false;
        TextSearchFieldType that = (TextSearchFieldType) obj;
        return this.languages.equals(that.languages)
                && this.multiValued == that.multiValued
                && this.searchBySubstring == that.searchBySubstring
                && this.searchByExactMatch == that.searchByExactMatch;
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
