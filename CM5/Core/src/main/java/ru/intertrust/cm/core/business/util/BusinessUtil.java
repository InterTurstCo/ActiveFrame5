package ru.intertrust.cm.core.business.util;

import ru.intertrust.cm.core.business.api.dto.IdentifiableObject;

import java.util.ArrayList;

/**
 * Author: Denis Mitavskiy
 * Date: 23.05.13
 * Time: 17:42
 */
public class BusinessUtil {
    public static String getDetailedDescription(IdentifiableObject obj) {
        final String TABULATOR = "    ";
        ArrayList<String> fields = obj.getFields();
        StringBuilder result = new StringBuilder();
        result.append("Id = ").append(obj.getId()).append('\n');
        result.append("Fields: [").append('\n');
        for (String field : fields) {
            result.append(TABULATOR).append(field).append(" = ").append(obj.getValue(field)).append('\n');
        }
        result.append(']').append('\n');
        return result.toString();
    }

    public static String getTableRowDescription(IdentifiableObject obj) {
        ArrayList<String> fields = obj.getFields();
        StringBuilder result = new StringBuilder();
        result.append(obj.getId()).append('\t');
        for (String field : fields) {
            result.append(obj.getValue(field)).append('\t');
        }
        return result.toString();
    }
}
