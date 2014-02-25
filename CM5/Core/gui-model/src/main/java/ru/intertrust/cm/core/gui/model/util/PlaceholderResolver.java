package ru.intertrust.cm.core.gui.model.util;

import java.util.Map;
/*
 * @author Lesia Puhova
 *         Date: 07.02.2014
 *         Time: 05:22:04
 */
public class PlaceholderResolver {

    public static String substitute(String string, Map<String, Object> properties) {
        StringBuilder sb = new StringBuilder(string);
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            String key = parenthesize(entry.getKey());
            String value = entry.getValue() != null ? entry.getValue().toString() : "";
            replaceAll(sb, key, value);
         }
        return sb.toString();
    }

    private static String parenthesize(String string) {
       return new StringBuilder("${").append(string).append("}").toString();
    }

    private static void replaceAll(StringBuilder sb, String from, String to) {
        int index = sb.indexOf(from);
        while (index != -1) {
            sb.replace(index, index + from.length(), to);
            index += to.length();
            index = sb.indexOf(from, index);
        }
    }
}