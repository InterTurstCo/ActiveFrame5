package ru.intertrust.cm.core.business.impl.search;

import ru.intertrust.cm.core.business.api.dto.Case;
import ru.intertrust.cm.core.business.api.dto.FieldType;
import ru.intertrust.cm.core.business.api.dto.SearchFilter;

import java.util.Collection;
import java.util.Collections;

public class CustomSearchFieldType implements SearchFieldType {

    private String solrPrefix;

    public CustomSearchFieldType(String solrPrefix) {
        if (!solrPrefix.endsWith("_")) {
            solrPrefix += "_";
        }
        this.solrPrefix = solrPrefix;
    }

    @Override
    public boolean supportsFilter(SearchFilter filter) {
        return true;    // Have no real information about supported filters; try to use with any
    }

    @Override
    public Collection<String> getSolrFieldNames(String field) {
        return Collections.singleton(new StringBuilder()
                .append(solrPrefix)
                .append(Case.toLower(field))
                .toString());
    }

    @Override
    public FieldType getDataFieldType() {
        return FieldType.STRING;
    }

    @Override
    public boolean isQuote() {
        return false;
    }

    @Override
    public boolean isTextType() {
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != getClass()) return false;
        CustomSearchFieldType that = (CustomSearchFieldType) obj;
        return this.solrPrefix.equals(that.solrPrefix);
    }

    @Override
    public int hashCode() {
        return solrPrefix.hashCode() ^ 0x5A693C24;
    }

}
