package ru.intertrust.cm.core.business.impl.search;

import ru.intertrust.cm.core.business.api.dto.FieldType;

import java.util.LinkedHashSet;
import java.util.Set;

public class TargetResultField {
    private final String indexedFieldName;
    private final String resultFieldName;
    private final FieldType fieldType;
    private final Set<String> solrFieldNames = new LinkedHashSet<>();
    private final boolean isHighlighting;

    public TargetResultField(String indexedFieldName, String resultFieldName, FieldType fieldType) {
        this(indexedFieldName, resultFieldName, fieldType, false);
    }

    public TargetResultField(String indexedFieldName, String resultFieldName, FieldType fieldType, boolean isHighlighting) {
        this.indexedFieldName = indexedFieldName;
        this.resultFieldName = resultFieldName;
        this.fieldType = fieldType;
        this.isHighlighting = isHighlighting;
    }

    public String getIndexedFieldName() {
        return indexedFieldName;
    }

    public String getResultFieldName() {
        return resultFieldName;
    }

    public FieldType getDataFieldType() {
        return fieldType;
    }

    Set<String> getSolrFieldNames() {
        return solrFieldNames;
    }

    public boolean isHighlighting() {
        return isHighlighting;
    }
}


