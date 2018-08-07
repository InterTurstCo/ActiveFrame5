package ru.intertrust.cm.core.dao.impl.utils;

import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.business.api.dto.util.ListValue;

import java.io.Serializable;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ru.intertrust.cm.core.dao.api.DomainObjectDao.REFERENCE_TYPE_POSTFIX;
import static ru.intertrust.cm.core.dao.impl.DataStructureNamingHelper.getTimeZoneIdColumnName;
import static ru.intertrust.cm.core.dao.impl.utils.DateUtils.getGMTDate;
import static ru.intertrust.cm.core.dao.impl.utils.DateUtils.getTimeZoneId;

/**
 * Представляет набор функций для работы со колонками доменного объекта
 * 
 * @author skashanski
 * 
 */
public class DaoUtils {

    public enum ParamPatternConverter {
        // Паттерн поиска параметров вида ":0", ":p1"
        COLON("(\\:[\\w]+)", new MatchConverter() {
            @Override
            public String convert(String src) {
                return src != null && !src.isEmpty() ? src.substring(1) : "";
            }
        }),

        // Паттерн поиска параметров вида "{0}", "{p1}"
        BRACE("(\\{[\\w]+\\})", new MatchConverter() {
            @Override
            public String convert(String src) {
                return src != null && src.length() > 2 ? src.substring(1, src.length() - 1) : "";
            }
        });
        
        private String pattern;
        private MatchConverter matchConverter;
        private ParamPatternConverter(String pattern, MatchConverter matchConverter) {
            this.pattern = pattern;
            this.matchConverter = matchConverter;
        }
        
        public String getPattern () {
            return pattern;
        }
        public MatchConverter getMatchConverter() {
            return matchConverter;
        }
    }
    
    /**
     * Преобразование найденного фрагмента
     * @author mike
     *
     */
    private interface MatchConverter {
        /**
         * Преобразование фрагмента.
         * @param src
         *            исходный фрагмент
         * @return преобразованный фрагмент
         */
        String convert(String src);
    }    

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
            buidler.append(Case.toLower(column));

        }

        return buidler.toString();

    }

    public static String generateParameter(String columnName) {
        return Case.toLower(columnName);

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
    
    /**
     * Обработка имен параметров в тексте sql-запроса за исключением фрагментов
     * в апострофахю
     * @param srcSql
     *            исходный текст запроса
     * @param prefix
     *            префикс для постановки
     * @param suffix
     *            суффикс для подстановки
     * @param matchConverters
     *            массив паттернов и конвертеров
     * @return преобразованный запрос
     */
    public static String adjustParameterNamesBeforePreProcessing(String srcSql, String prefix, String suffix, 
            ParamPatternConverter ... paramPatternConverters) {
        if (paramPatternConverters == null || paramPatternConverters.length == 0) {
            return srcSql;
        }
        // поиск фрагментов в апострофах, чтобы оставить их без изменения
        Pattern p = Pattern.compile("(\\'.*?\\')");
        Matcher m = p.matcher(srcSql);
        StringBuffer sb = new StringBuffer();
        String res = "";
        String tmp = null;
        // Цикл по фрагментам в апострофах
        while (m.find()) {
            m.appendReplacement(sb, "");
            tmp = sb.toString();
            sb.setLength(0);
            String grp = m.group(1);
            // преобразование фрагмента вне апострофов и конкатенация результата
            for (ParamPatternConverter ppc : paramPatternConverters) {
                tmp = adjustSqlParamNames(tmp, ppc.getPattern(), ppc.getMatchConverter(), prefix, suffix);
            }
            res += tmp + grp;
        }
        m.appendTail(sb);
        tmp = sb.toString();
        // преобразование фрагмента вне апострофов и конкатенация результата
        for (ParamPatternConverter ppc : paramPatternConverters) {
            tmp = adjustSqlParamNames(tmp, ppc.getPattern(), ppc.getMatchConverter(), prefix, suffix);
        }
        res += tmp;
        return res;
    }

    /**
     * Обработка имен параметров в фрагменте текста sql-запроса вне апострофов
     * @param src
     *            фрагмент текста
     * @param regExp
     *            паттерн поиска
     * @param matchConverter
     *            конвертер фрагмента, найденного по паттерну
     * @param prefix
     *            префикс
     * @param suffix
     *            суффикс
     * @return преобразованный фрагмент
     */
    private static String adjustSqlParamNames(String src, String regExp, 
            MatchConverter matchConverter, String prefix, String suffix) {
        if (matchConverter == null) {
            return src;
        }
        Pattern p = Pattern.compile(regExp);
        Matcher m = p.matcher(src);
        StringBuffer sb = new StringBuffer();
        String res = "";
        while (m.find()) {
            m.appendReplacement(sb, prefix);
            String gr = m.group(1);
            res += sb.toString() + matchConverter.convert(gr) + suffix;
            sb.setLength(0);
        }
        m.appendTail(sb);
        res += sb;
        return res;
    }

}
