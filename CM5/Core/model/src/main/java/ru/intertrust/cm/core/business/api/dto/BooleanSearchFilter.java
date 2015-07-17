package ru.intertrust.cm.core.business.api.dto;

public class BooleanSearchFilter extends SearchFilterBase {

    /**
     * 
     */
    private static final long serialVersionUID = 8054924485939957273L;

    private boolean value = false;

    public BooleanSearchFilter(String fieldPath, boolean value) {
        setFieldName(fieldPath);
        this.value = value;
    }

    public boolean getValue() {
        return value;
    }

}
