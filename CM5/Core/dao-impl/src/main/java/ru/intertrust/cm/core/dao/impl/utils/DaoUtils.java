package ru.intertrust.cm.core.dao.impl.utils;

import java.util.Collection;

/**
 * Представляет набор функций для работы со колонками бизнес-объекта
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
     * @param columnNames
     *            список колонок
     * @return возвращает строку состоящую из списка значений
     */
    public static String generateCommaSeparatedListWithParams(Collection<String> columns) {

        if (columns.size() == 0) {
            return "";
        }

        StringBuilder buidler = new StringBuilder();

        String firstColumn = columns.iterator().next();
        for (String column : columns) {
            if (!firstColumn.equals(column)) {
                buidler.append(",");
            }
            buidler.append(column);
            buidler.append("=");
            buidler.append(":");
            buidler.append(column.toLowerCase());

        }

        return buidler.toString();

    }

}
