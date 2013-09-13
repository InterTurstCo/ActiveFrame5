package ru.intertrust.cm.core.dao.impl.utils;

import ru.intertrust.cm.core.config.model.ReferenceFieldConfig;
import ru.intertrust.cm.core.config.model.ReferenceFieldTypeConfig;
import ru.intertrust.cm.core.dao.exception.DaoException;

import java.util.Collection;

import static ru.intertrust.cm.core.dao.impl.DataStructureNamingHelper.getSqlName;

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

    public static String generateParameter(ReferenceFieldConfig fieldConfig, String type) {
        for (ReferenceFieldTypeConfig typeConfig : fieldConfig.getTypes()) {
            if (type.equals(typeConfig.getName())) {
                return generateParameter(getSqlName(fieldConfig, typeConfig));
            }
        }

        throw new DaoException("Type '" + type + "' is not found in field configuration '" + fieldConfig.getName() +
                "'");
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
            builder.append(column);
            builder.append("=");
            builder.append(":");
            builder.append(column.toLowerCase());

        }

        return builder.toString();

    }

}
