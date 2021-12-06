package ru.intertrust.cm.core.business.api.dto;

import ru.intertrust.cm.core.config.FieldConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

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

    // TODO Удалить после иcправления JdbcDatabaseMetaData.java
    @Deprecated
    // todo: drop in next release
    void setFields(List<String> fields);
    
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

    /**
     * Сортирует данную коллекцию согласно заданному порядку сортировки
     * @param sortOrder порядок сортировки
     */
    void sort(SortOrder sortOrder);

    void append(IdentifiableObjectCollection collection);

    int getFieldIndex(String field);

    List<FieldConfig> getFieldsConfiguration();

    int size();
    
    Stream<IdentifiableObject> stream();

    /**
     * Truncates the collection. At the result, we will have the collection only between indexes from and to.
     */
    void cut(int fromIndex, int toIndex);

}
