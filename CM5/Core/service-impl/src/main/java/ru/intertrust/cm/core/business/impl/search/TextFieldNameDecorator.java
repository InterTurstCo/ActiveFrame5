package ru.intertrust.cm.core.business.impl.search;

import ru.intertrust.cm.core.business.api.dto.Case;
import ru.intertrust.cm.core.tools.DelegatingIterator;

import java.util.Iterator;

public class TextFieldNameDecorator implements Iterable<String> {

    final Iterable<String> langIds;
    String solrField;
    String sourceField;
    //String typeInfix;
    boolean multiValued;

    TextFieldNameDecorator(String solrField, Iterable<String> langIds) {
        this.solrField = solrField;
        this.langIds = langIds;
    }

    TextFieldNameDecorator(Iterable<String> langIds, String sourceField, boolean multiValued) {
        this.langIds = langIds;
        this.sourceField = Case.toLower(sourceField);
        //this.typeInfix = SearchFieldType.getFieldType(FieldType.STRING, multiValued).getInfix();
        this.multiValued = multiValued;
    }

    @Override
    public Iterator<String> iterator() {
        return new DelegatingIterator<String>(langIds) {

            @Override
            public String next() {
                String langId = super.next();
                StringBuilder fieldName = new StringBuilder();
                if (solrField == null) {
                    fieldName.append(SolrFields.FIELD_PREFIX);
                    if (langId.isEmpty()) {
                        //fieldName.append(SearchFieldType.getFieldType(FieldType.STRING, multiValued).infix);
                    } else {
                        fieldName.append(langId);
                        if (multiValued) {
                            fieldName.append("s");
                        }
                        fieldName.append("_");
                    }
                    fieldName.append(sourceField);
                } else {
                    fieldName.append(solrField);
                    if (!langId.isEmpty()) {
                        fieldName.append("_").append(langId);
                    }
                }
                return fieldName.toString();
            }
        };
    }

}
