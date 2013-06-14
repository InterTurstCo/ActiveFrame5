package ru.intertrust.cm.core.dao.impl;

import ru.intertrust.cm.core.config.model.BusinessObjectConfig;
import ru.intertrust.cm.core.config.model.FieldConfig;
import ru.intertrust.cm.core.config.model.ReferenceFieldConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper для отображения имен конфигурации бизнес-объектов на базу данных
 * @author vmatsukevich
 *         Date: 5/16/13
 *         Time: 11:27 AM
 */
public class DataStructureNamingHelper {

    /**
     * Возвращает имя бизнес-объекта в sql-виде
     * @param businessObjectConfig конфигурация бизнес-объекта
     * @return имя бизнес-объекта в sql-виде
     */
    public static String getSqlName(BusinessObjectConfig businessObjectConfig) {
        return convertToSqlFormat(businessObjectConfig.getName());
    }

    /**
     * Возвращает имя последовательности(сиквенса) бизнес-объекта в sql-виде
     * @param businessObjectConfig конфигурация бизнес-объекта
     * @return имя последовательности для бизнес-объекта в sql-виде
     */
    public static String getSqlSequenceName(BusinessObjectConfig businessObjectConfig) {

        return convertToSqlFormat(businessObjectConfig.getName()) + "_SEQ";
    }


    /**
     * Возвращает имя поля бизнес-объекта в sql-виде
     * @param fieldConfig конфигурация поля бизнес-объекта
     * @return имя поля бизнес-объекта в sql-виде
     */
    public static String getSqlName(FieldConfig fieldConfig) {
        return convertToSqlFormat(fieldConfig.getName());
    }

    /**
     * Возвращает список имен полей бизнес-объектов в sql-виде
     * @param fieldConfigs список конфигураций полей бизнес-объектов
     * @return список имен полей бизнес-объектов в sql-виде
     */
    public static List<String> getSqlName(List<FieldConfig> fieldConfigs) {

        List<String> columnNames = new ArrayList<String>();
        for (FieldConfig fieldConfig : fieldConfigs) {
            columnNames.add(getSqlName(fieldConfig));

        }

        return columnNames;
    }

    /**
     * Возвращает имя бизнес-объекта, на который ссылается поле, в sql-виде
     * @param referenceFieldConfig конфигурация поля-ссылки бизнес-объекта
     * @return имя бизнес-объекта, на который ссылается поле, в sql-виде
     */
    public static String getReferencedTypeSqlName(ReferenceFieldConfig referenceFieldConfig) {
        return convertToSqlFormat(referenceFieldConfig.getType());
    }

    /**
     * Возвращает имя в sql-виде
     * @param name имя
     * @return имя в sql-виде
     */
    public static String getSqlName(String name) {
        return convertToSqlFormat(name);
    }

    private static String convertToSqlFormat(String name) {
        if(name == null) {
            throw new IllegalArgumentException("Name is null");
        }

        name = name.trim();

        if(name.isEmpty()) {
            throw new IllegalArgumentException("Name is empty");
        }

        return name.replace(' ', '_').toUpperCase();
    }
}
