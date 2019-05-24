package ru.intertrust.cm.core.gui.model.util;

import com.google.gwt.i18n.client.NumberFormat;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;

import java.util.ArrayList;
import java.util.List;

import static ru.intertrust.cm.core.gui.model.util.UserSettingsHelper.ARRAY_DELIMITER;

/**
 * @author Lesia Puhova
 *         Date: 17.02.14
 *         Time: 17:14
 */
public class StringUtil {

   private StringUtil() {} // un-instantiable

    public static String join(List<?> list, String delimiter) {
        StringBuilder sb = new StringBuilder();
        if (list == null || list.isEmpty()) {
            return "";
        }
        for (Object obj : list) {
            sb.append(obj).append(delimiter);
        }
        sb.delete(sb.length() - delimiter.length(), sb.length());
        return sb.toString();
    }

    public static Integer integerFromString(final String intAsStr, final Integer defaultValue) {
        Integer result = defaultValue;
        if (intAsStr != null && !intAsStr.isEmpty()) {
            try {
                result = Integer.valueOf(intAsStr.replaceAll("\\D+", ""));
            } catch (Exception ignored) {
            }
        }
        return result;
    }

    public static Id idFromString(final String idAsStr) {
        Id result = null;
        if (idAsStr != null && !idAsStr.trim().isEmpty()) {
            try {
                result = new RdbmsId(idAsStr.trim());
            } catch (Exception ignored) {}
        }
        return result;
    }

    public static Boolean booleanFromString(final String booleanAsStr, final Boolean defaultValue) {
        Boolean result = defaultValue;
        try {
            result = Boolean.valueOf(booleanAsStr);
        } catch (Exception ignored) {}
        return result;
    }

    public static List<Id> idsStrToList(final String arrayIdStr) throws Exception {
        final List<Id> result = new ArrayList<>();
        if (arrayIdStr != null && !arrayIdStr.isEmpty()) {
            final String[] idStrArray = arrayIdStr.split(ARRAY_DELIMITER);
            for (String idStr : idStrArray) {
                final Id id = StringUtil.idFromString(idStr);
                if (id != null) {
                    result.add(id);
                } else {
                    throw new Exception("Неправильный формат Id " + idStr);
                }
            }
        }
        return result;
    }

    public static String idArrayToStr(Id... ids) {
        final StringBuilder builder = new StringBuilder();
        if (ids != null && ids.length > 0) {
            boolean isFirst = true;
            for (Id id : ids) {
                if (id != null) {
                    if (!isFirst) {
                        builder.append(ARRAY_DELIMITER);
                    }
                    isFirst = false;
                    builder.append(id.toStringRepresentation());
                }
            }
        }
        return builder.length() == 0 ? null : builder.toString();
    }
    public static int getIntValue(String sizeString){
        String raw = sizeString.replaceAll("px", "").trim();
        return Integer.parseInt(raw);
    }

    /**
     * Преобразует размер файла к читаемому виду:<br>
     * <ul>
     * <li>2 знака после запятой (кроме размера в байтах)</li>
     * <li>единицы измерения в зависимости от размера : байты, килобайты, мегабайты, гигабайты</li>
     * </ul>
     *
     * @param size размер файла в байтах
     * @return отформатированную строку размера файла
     */
    public static String getFormattedFileSize(long size) {
        float sizeKb = 1024.0f;
        if (size < sizeKb) {
            return size + " b";
        }

        float sizeMb = sizeKb * sizeKb;
        if (size < sizeMb) {
            return formatFloatWithTwoDecimals(size / sizeKb) + " Kb";
        }

        float sizeGb = sizeMb * sizeKb;
        if (size < sizeGb) {
            return formatFloatWithTwoDecimals(size / sizeMb) + " Mb";
        }

        float sizeTerra = sizeGb * sizeKb;
        if (size < sizeTerra) {
            return formatFloatWithTwoDecimals(size / sizeGb) + " Gb";
        }

        return "";
    }

    /**
     * Приводит значение с дробной частью к строке с двумя знаками после запятой.
     *
     * @param floatValue значение с дробной частью
     * @return отформатированная строка со значением
     */
    private static String formatFloatWithTwoDecimals(float floatValue) {
        NumberFormat decimalFormat = NumberFormat.getFormat(".##");
        final String formattedStrValue = decimalFormat.format(floatValue);

        return formattedStrValue;
    }

    /**
     * Проверяет, что объект строки null, путой ("") или содержит только пробелы<br>
     * <br>
     * StringUtil.isNullOrBlank(null)      = true<br>
     * StringUtil.isNullOrBlank("")        = true<br>
     * StringUtil.isNullOrBlank(" ")       = true<br>
     * StringUtil.isNullOrBlank("abc")     = false<br>
     * StringUtil.isNullOrBlank("  abc  ") = false<br>
     *
     * @param string строка для проверки, может быть null
     * @return true, если строка null, путая ("") или содержит только пробелы;<br>
     * false - в противном случае
     */
    public static boolean isNullOrBlank(String string) {
        return (string == null) || string.trim().isEmpty();
    }

}
