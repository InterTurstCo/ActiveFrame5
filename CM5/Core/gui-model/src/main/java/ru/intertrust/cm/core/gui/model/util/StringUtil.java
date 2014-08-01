package ru.intertrust.cm.core.gui.model.util;

import java.util.ArrayList;
import java.util.List;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;

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
}
