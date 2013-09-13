package ru.intertrust.cm.core.dao.impl;

import ru.intertrust.cm.core.config.model.DomainObjectTypeConfig;
import ru.intertrust.cm.core.config.model.FieldConfig;
import ru.intertrust.cm.core.config.model.ReferenceFieldConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper для отображения имен конфигурации доменных объектов на базу данных
 * @author vmatsukevich
 *         Date: 5/16/13
 *         Time: 11:27 AM
 */
public class DataStructureNamingHelper {

    public static final int MAX_NAME_LENGTH = 25;

    /**
     * Возвращает имя доменного объекта в sql-виде
     * @param domainObjectTypeConfig конфигурация доменного объекта
     * @return имя доменного объекта в sql-виде
     */
    public static String getSqlName(DomainObjectTypeConfig domainObjectTypeConfig) {
        return convertToSqlFormat(domainObjectTypeConfig.getName());
    }

    /**
     * Возвращает имя последовательности(сиквенса) доменного объекта в sql-виде
     * @param domainObjectTypeConfig конфигурация доменного объекта
     * @return имя последовательности для доменного объекта в sql-виде
     */
    public static String getSqlSequenceName(DomainObjectTypeConfig domainObjectTypeConfig) {

        return convertToSqlFormat(domainObjectTypeConfig.getName()) + "_SEQ";
    }


    /**
     * Возвращает имя поля доменного объекта в sql-виде
     * @param fieldConfig конфигурация поля доменного объекта
     * @return имя поля доменного объекта в sql-виде
     */
    public static String getSqlName(FieldConfig fieldConfig) {
        return convertToSqlFormat(fieldConfig.getName());
    }

    /**
     * Возвращает список имен полей доменных объектов в sql-виде
     * @param fieldConfigs список конфигураций полей доменных объектов
     * @return список имен полей доменных объектов в sql-виде
     */
    public static List<String> getSqlName(List<FieldConfig> fieldConfigs) {

        List<String> columnNames = new ArrayList<String>();
        for (FieldConfig fieldConfig : fieldConfigs) {
            if (fieldConfig instanceof ReferenceFieldConfig) {
                //TODO: Обрабатывать множественные типы ссылок
                columnNames.add(getSqlName(getIndexedName(fieldConfig.getName(), 1)));
            } else {
                columnNames.add(getSqlName(fieldConfig));
            }

        }

        return columnNames;
    }

    /**
     * Возвращает имя в sql-виде
     * @param name имя
     * @return имя в sql-виде
     */
    public static String getSqlName(String name) {
        return convertToSqlFormat(name);
    }

    public static String getIndexedName(String name1, Integer index) {
        if (name1 == null || name1.isEmpty()) {
            throw new IllegalArgumentException("Name is null or empty");
        }

        String indexString = index.toString();

        if (name1.length() + indexString.length() > MAX_NAME_LENGTH) {
            return name1.substring(0, MAX_NAME_LENGTH - indexString.length() - 1) + indexString;
        } else {
            return name1 + indexString;
        }
    }

    public static String getSqlAlias(String name) {
        if(name == null) {
            throw new IllegalArgumentException("Name is null");
        }

        name = name.trim();

        if(name.isEmpty()) {
            throw new IllegalArgumentException("Name is empty");
        }

        return name.toLowerCase();
    }

    private static String convertToSqlFormat(String name) {
        if(name == null) {
            throw new IllegalArgumentException("Name is null");
        }

        name = name.trim();

        if(name.isEmpty()) {
            throw new IllegalArgumentException("Name is empty");
        }

        return name.toUpperCase();
    }
}
