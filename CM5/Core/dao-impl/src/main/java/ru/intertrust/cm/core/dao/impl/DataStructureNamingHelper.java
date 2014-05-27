package ru.intertrust.cm.core.dao.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ru.intertrust.cm.core.config.DateTimeWithTimeZoneFieldConfig;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.config.IndexFieldConfig;
import ru.intertrust.cm.core.config.ReferenceFieldConfig;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.model.FatalException;

/**
 * Helper для отображения имен конфигурации доменных объектов на базу данных
 * @author vmatsukevich
 *         Date: 5/16/13
 *         Time: 11:27 AM
 */
public class DataStructureNamingHelper {

    private static Map<String, String> sqlNameCache = new ConcurrentHashMap<>();
    private static Map<ServiceColumnKey, String> serviceColumnNameCache = new ConcurrentHashMap<>();
    
    public static final int MAX_NAME_LENGTH = 30;

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
     * @param doTypeId идентификатор конфигурации доменного объекта
     * @return имя последовательности для доменного объекта в sql-виде
     */
    public static String getSqlSequenceName(Integer doTypeId) {

        return getSqlSequenceName(doTypeId.toString());
    }

    /**
     * Возвращает имя последовательности(сиквенса) sql-виде
     * @param name имя
     * @return имя последовательности в sql-виде
     */
    public static String getSqlSequenceName(String name) {
        return convertToSqlFormat(name) + "_sq";
    }

    /**
     * Возвращает имя последовательности(сиквенса) лога доменного объекта в sql-виде
     * @param doTypeId идентификатор конфигурация доменного объекта
     * @return имя последовательности для доменного объекта в sql-виде
     */
    public static String getSqlAuditSequenceName(Integer doTypeId) {
        return convertToSqlFormat(getName(doTypeId.toString(), true)) + "_sq";
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
     * Возвращает имя поля, участвующего в индексе, в sql-виде
     * @param indexFieldConfig конфигурация индексного поля 
     * @return имя поля, участвующего в индексе, в sql-виде
     */
    public static String getSqlName(IndexFieldConfig indexFieldConfig) {
        return convertToSqlFormat(indexFieldConfig.getName());
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
                columnNames.add(getReferenceTypeColumnName(fieldConfig.getName()));
            } else if (fieldConfig instanceof DateTimeWithTimeZoneFieldConfig) {
                columnNames.add(getTimeZoneIdColumnName(fieldConfig.getName()));
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
        return getSqlName(name, false);
    }

    public static String getSqlName(String name, boolean isAl) {
        return convertToSqlFormat(getName(name, isAl));
    }

    /**
     * Возвращает имя audit log таблицы
     * @param name имя
     * @return имя audit log таблицы
     */
    public static String getALTableSqlName(String name) {
        return getSqlName(getName(name, true));
    }

    public static String getName(String name, boolean isAl) {
        return isAl ? name + "_al" : name;
    }

    public static String getReferenceTypeColumnName(String columnName) {
        return getServiceColumnName(columnName, DomainObjectDao.REFERENCE_TYPE_POSTFIX);
    }

    public static String getTimeZoneIdColumnName(String columnName) {
        return getServiceColumnName(columnName, DomainObjectDao.TIME_ID_ZONE_POSTFIX);
    }

    public static String getServiceColumnName(String columnName, String postfix) {
        
        ServiceColumnKey serviceColumnKey = new ServiceColumnKey(columnName, postfix);
        
        if (serviceColumnNameCache.get(serviceColumnKey) != null) {
            return serviceColumnNameCache.get(serviceColumnKey);
        } else {
            String processedName = generateServiceColumnName(columnName, postfix);
            serviceColumnNameCache.put(serviceColumnKey, processedName);
            return processedName;
        }
    }

    private static String generateServiceColumnName(String columnName, String postfix) {
        if (columnName.length() + postfix.length() > MAX_NAME_LENGTH) {
            throw new FatalException();
        }

        String resultName = columnName + postfix;
        return getSqlName(resultName);
    }

    
    public static String getSqlAlias(String name) {
        return convertToSqlFormat(name);
    }

    public static String getFilterParameterPrefix(String filterName) {
        return CollectionsDaoImpl.PARAM_NAME_PREFIX + filterName;
    }

    private static String convertToSqlFormat(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Name is null");
        }

        if (sqlNameCache.get(name) != null) {
            return sqlNameCache.get(name);
        } else {
            String result = getConvertedValue(name);
            sqlNameCache.put(name, result);
            return result;
        }
    }

    private static String getConvertedValue(String name) {
        String trimmedName = name.trim();
        if (trimmedName.isEmpty()) {
            throw new IllegalArgumentException("Name is empty");
        }

        String result = trimmedName.toLowerCase();
        return result;
    }

    /**
     * Ключ, используемый в кеше названий колонок.
     * @author atsvetkov
     *
     */
    private static class ServiceColumnKey {
        private String columnName;
        private String postfix;

        public ServiceColumnKey(String name, String prefix) {
            this.columnName = name;
            this.postfix = prefix;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((columnName == null) ? 0 : columnName.hashCode());
            result = prime * result + ((postfix == null) ? 0 : postfix.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            ServiceColumnKey other = (ServiceColumnKey) obj;

            if (columnName == null) {
                if (other.columnName != null) {
                    return false;
                }
            } else if (!columnName.equals(other.columnName)) {
                return false;
            }
            if (postfix == null) {
                if (other.postfix != null) {
                    return false;
                }
            } else if (!postfix.equals(other.postfix)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "ServiceColumnKey [columnName=" + columnName + ", postfix=" + postfix + "]";
        }
        
    }
}
