package ru.intertrust.cm.core.dao.impl;

import ru.intertrust.cm.core.config.model.DomainObjectTypeConfig;
import ru.intertrust.cm.core.config.model.FieldConfig;
import ru.intertrust.cm.core.config.model.ReferenceFieldConfig;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;

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
     * Возвращает имя последовательности(сиквенса) лога доменного объекта в sql-виде
     * @param domainObjectTypeConfig конфигурация доменного объекта
     * @return имя последовательности для доменного объекта в sql-виде
     */
    public static String getSqlAuditSequenceName(DomainObjectTypeConfig domainObjectTypeConfig) {

        return convertToSqlFormat(domainObjectTypeConfig.getName()) + "_LOG_SEQ";
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
    public static List<String> getColumnNames(List<FieldConfig> fieldConfigs) {

        List<String> columnNames = new ArrayList<String>();
        for (FieldConfig fieldConfig : fieldConfigs) {
            columnNames.add(getSqlName(fieldConfig));
            if (fieldConfig instanceof ReferenceFieldConfig) {
                columnNames.add(getReferenceTypeColumnName((ReferenceFieldConfig) fieldConfig));
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

    public static String getReferenceTypeColumnName(ReferenceFieldConfig fieldConfig) {
        String name = fieldConfig.getName();

        final String postfix = DomainObjectDao.REFERENCE_TYPE_POSTFIX;
        String resultName;
        if (name.length() + postfix.length() > MAX_NAME_LENGTH) {
            resultName = name.substring(0, MAX_NAME_LENGTH - postfix.length() - 1) + postfix;
        } else {
            resultName = name + postfix;
        }

        return getSqlName(resultName);
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
