package ru.intertrust.cm.core.business.api.dto;

import ru.intertrust.cm.core.config.FieldConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Denis Mitavskiy
 * Date: 24.05.13
 * Time: 13:44
 */
public interface IdentifiableObjectCollection extends Iterable<IdentifiableObject>, Dto {

    void setFieldsConfiguration(List<FieldConfig> fieldConfigs);

    Id getId(int row);

    void setId(int row, Id id);

    void set(int fieldIndex, int row, Value value);

    /**
     * @deprecated todo review
     * @param field
     * @param row
     * @param value
     */
    void set(String field, int row, Value value);

    IdentifiableObject get(int row);

    /**
     * @deprecated todo review
     * @param field
     * @param row
     */
    Value get(String field, int row);

    Value get(int fieldIndex, int row);

    int getFieldIndex(String field);

    ArrayList<String> getFields();

    ArrayList<FieldConfig> getFieldsConfiguration();

    int size();
}
