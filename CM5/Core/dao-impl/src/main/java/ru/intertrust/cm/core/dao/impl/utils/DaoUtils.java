package ru.intertrust.cm.core.dao.impl.utils;

import ru.intertrust.cm.core.business.api.dto.*;

import java.util.Collection;
import java.util.Date;
import java.util.Map;

import static ru.intertrust.cm.core.dao.impl.DataStructureNamingHelper.getReferenceTypeColumnName;
import static ru.intertrust.cm.core.dao.impl.DataStructureNamingHelper.getTimeZoneIdColumnName;
import static ru.intertrust.cm.core.dao.impl.PostgreSqlQueryHelper.wrap;
import static ru.intertrust.cm.core.dao.impl.utils.DateUtils.getGMTDate;
import static ru.intertrust.cm.core.dao.impl.utils.DateUtils.getTimeZoneId;

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

    /**
     * Формирует строку состоящую из списка переданных колонок разделенных
     * запятой. В результате получаем строку ввида : column1=:param1,
     * column2=:param2
     *
     * @param columns список колонок
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
            builder.append("=:");
            builder.append(column.toLowerCase());

        }

        return builder.toString();

    }

    public static void setParameters(String parameterName, Value value, Map<String, Object> parameters) {
        if (value instanceof ReferenceValue) {
            RdbmsId rdbmsId = (RdbmsId) value.get();
            parameters.put(parameterName, rdbmsId.getId());
            parameterName = generateParameter(getReferenceTypeColumnName(parameterName));
            parameters.put(parameterName, rdbmsId.getTypeId());
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
        } else {
            parameters.put(parameterName, value.get());
        }
    }

}
