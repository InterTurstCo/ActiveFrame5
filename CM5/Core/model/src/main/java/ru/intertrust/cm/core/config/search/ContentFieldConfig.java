package ru.intertrust.cm.core.config.search;

import org.simpleframework.xml.Attribute;
import ru.intertrust.cm.core.business.api.dto.SearchFilter;

import java.io.Serializable;

public class ContentFieldConfig implements Serializable {

    public enum Type {
        REFID("refid", null),
        MIMETYPE("mimetype", "cntx_mime_type"),
        LENGTH("length", "cntx_length"),
        NAME("name", "cntx_fname"),
        PATH("path", "cntx_fpath"),
        DESCRIPTION("description", "cntx_descr"),
        HIGHLIGHTING("highlighting", SearchFilter.CONTENT);

        private final String fieldType;
        private final String solrFieldName;

        private Type(String fieldType, String solrFieldName) {
            this.fieldType = fieldType;
            this.solrFieldName = solrFieldName;
        }

        public String getFieldType() {
            return fieldType;
        }

        public String getSolrFieldName() {
            return solrFieldName;
        }

        public static Type fromStrTypeValue(String fieldType) {
            for (Type value : values()) {
                if (value.fieldType.equalsIgnoreCase(fieldType)) {
                    return value;
                }
            }
            throw new IllegalArgumentException("Unknown type value: " + fieldType);
        }
    }

    //V@Attribute(required = true)
    private Type type;

    @Attribute(name = "show-in-results", required = false)
    private Boolean showInResults;

    @Attribute(name = "target-field-name", required = true)
    private String targetFieldName;

    @Attribute(name = "type", required = true)
    public String getTypeString() {
        return type.getFieldType();
    }

    @Attribute(name = "type", required = true)
    public void setTypeString(String strType) {
        this.type = Type.fromStrTypeValue(strType);
    }

    public Type getType() {
        return  this.type;
    }

    public String getTargetFieldName() {
        return targetFieldName;
    }

    public boolean getShowInResults () {
        return showInResults != null ? showInResults.booleanValue() : false;
    }

    @Override
    public int hashCode() {
        int hash = type.hashCode();
        hash = hash * 31 ^ (showInResults != null ? showInResults.hashCode() : 1);
        hash = hash * 31 ^ targetFieldName.hashCode();
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || !getClass().equals(obj.getClass())) {
            return false;
        }
        ContentFieldConfig other = (ContentFieldConfig) obj;

        return type.equals(other.type)
                && (targetFieldName.equals(other.targetFieldName))
                && (showInResults == null ? other.showInResults == null : showInResults.equals(other.showInResults));
    }

}

