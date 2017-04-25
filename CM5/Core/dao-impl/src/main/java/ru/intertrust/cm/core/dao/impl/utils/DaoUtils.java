package ru.intertrust.cm.core.dao.impl.utils;

import static ru.intertrust.cm.core.dao.api.DomainObjectDao.REFERENCE_TYPE_POSTFIX;
import static ru.intertrust.cm.core.dao.impl.DataStructureNamingHelper.getTimeZoneIdColumnName;
import static ru.intertrust.cm.core.dao.impl.utils.DateUtils.getGMTDate;
import static ru.intertrust.cm.core.dao.impl.utils.DateUtils.getTimeZoneId;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import ru.intertrust.cm.core.business.api.dto.BooleanValue;
import ru.intertrust.cm.core.business.api.dto.DateTimeValue;
import ru.intertrust.cm.core.business.api.dto.DateTimeWithTimeZone;
import ru.intertrust.cm.core.business.api.dto.DateTimeWithTimeZoneValue;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.TimelessDate;
import ru.intertrust.cm.core.business.api.dto.TimelessDateValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.business.api.dto.util.ListValue;

/**
 * Представляет набор функций для работы со колонками доменного объекта
 * 
 * @author skashanski
 * 
 */
public class DaoUtils {

    /**
     * Формирует строку параметров вида :param1, param2,.. из списка переданных
     * имен колонок
     * 
     * @param columns
     *            список колонок
     * @return строку параметров разделенных запятой
     */
    public static String generateCommaSeparatedParameters(Collection<String> columns) {

        if (columns.size() == 0) {
            return "";
        }

        StringBuilder buidler = new StringBuilder();
        String firstColumn = columns.iterator().next();
        for (String column : columns) {
            if (!firstColumn.equals(column)) {
                buidler.append(",");
            }

            buidler.append(":");
            buidler.append(column.toLowerCase());

        }

        return buidler.toString();

    }

    public static String generateParameter(String columnName) {
        return columnName.toLowerCase();

    }

    public static String generateReferenceTypeParameter(String referenceParameterName) {
        return referenceParameterName + REFERENCE_TYPE_POSTFIX;
    }

    /**
     * Формирует строку состоящую из списка переданных колонок разделенных
     * запятой. В результате получаем строку ввида : column1=:param1,
     * column2=:param2
     * 
     * @param columns
     *            список колонок
     * @return возвращает строку состоящую из списка значений
     */
    public static String generateCommaSeparatedListWithParams(Collection<String> columns) {

        if (columns.size() == 0) {
            return "";
        }

        StringBuilder builder = new StringBuilder();

        String firstColumn = columns.iterator().next();
        for (String column : columns) {
            if (!firstColumn.equals(column)) {
                builder.append(", ");
            }
            builder.append(wrap(column));
            builder.append("=?");

        }

        return builder.toString();

    }

    public static void setParameter(String parameterName, Value value, Map<String, Object> parameters, boolean ignoreReferenceParameters) {
        if (value instanceof ReferenceValue) {
            if (!ignoreReferenceParameters) {
                RdbmsId rdbmsId = (RdbmsId) value.get();
                parameters.put(parameterName, rdbmsId.getId());
                parameters.put(generateReferenceTypeParameter(parameterName), rdbmsId.getTypeId());
            }
        } else if (value instanceof DateTimeValue) {
            parameters.put(parameterName, getGMTDate((Date) value.get()));
        } else if (value instanceof DateTimeWithTimeZoneValue) {
            parameters.put(parameterName, getGMTDate((DateTimeWithTimeZone) value.get()));
            parameterName = generateParameter(getTimeZoneIdColumnName(parameterName));
            parameters.put(parameterName, getTimeZoneId((DateTimeWithTimeZone) value.get()));
        } else if (value instanceof TimelessDateValue) {
            parameters.put(parameterName, getGMTDate((TimelessDate) value.get()));
        } else if (value instanceof BooleanValue) {
            Boolean parameterValue = (Boolean) value.get();
            parameters.put(parameterName, parameterValue ? 1 : 0);
        } else if (value instanceof ListValue) {
            ListValue listValue = (ListValue) value;
            if (!ignoreReferenceParameters || doesNotContainReferenceValues(listValue)) {
                List<Serializable> jdbcCompliantValues = createJdbcCompliantValues(listValue);
                parameters.put(parameterName, jdbcCompliantValues);
            }
        } else {
            parameters.put(parameterName, value.get());
        }
    }

    private static List<Serializable> createJdbcCompliantValues(ListValue value) {
        List<Serializable> jdbcCompliantValues = new ArrayList<>();
        if (value.get() == null) {
            return null;
        }
        for (Serializable singleValue : value.get()) {

            if (singleValue instanceof Id) {
                jdbcCompliantValues.add(((RdbmsId) singleValue).getId());
            } else if (singleValue instanceof DateTimeWithTimeZone) {
                jdbcCompliantValues.add(getGMTDate((DateTimeWithTimeZone) singleValue));
                // TODO create mechanism for passing timeZone. in() expression
                // in SQL should be replaced by set of OR
                // exressions
            } else if (singleValue instanceof Date) {
                jdbcCompliantValues.add(getGMTDate((Date) singleValue));
            } else if (singleValue instanceof TimelessDate) {
                jdbcCompliantValues.add(getGMTDate((TimelessDate) singleValue));
            } else {
                jdbcCompliantValues.add(singleValue);
            }
        }
        return jdbcCompliantValues;
    }

    public static String wrap(String string) {
        if (string == null || string.startsWith("\"") || string.equalsIgnoreCase("rownum")) {
            return string;
        } else {
            return "\"" + string + "\"";
        }
    }

    public static String unwrap(String string) {
        if (string == null || !string.startsWith("\"")) {
            return string;
        } else {
            return string.substring(1, string.length() - 1);
        }
    }

    /**
     * Добавляет фильтры на количество возвращаемых записей и смещение.
     * @param query
     * @param offset
     *            смещение. Если равно 0, то не создается фильтр по смещению.
     * @param limit
     *            количество. Если равно 0, то не создается фильтр по
     *            количеству.
     */
    public static void applyOffsetAndLimit(StringBuilder query, int offset, int limit) {
        if (limit != 0) {
            query.append(" limit ").append(limit);
        }
        if (offset != 0) {
            query.append(" OFFSET ").append(offset);
        }
    }

    private static boolean doesNotContainReferenceValues(ListValue value) {
        boolean result = true;
        for (Value<?> v : value.getUnmodifiableValuesList()) {
            if (v instanceof ReferenceValue) {
                result = false;
            }
        }
        return result;
    }
}
