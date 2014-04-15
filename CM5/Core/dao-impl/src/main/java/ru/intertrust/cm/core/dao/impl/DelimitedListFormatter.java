package ru.intertrust.cm.core.dao.impl;

import java.util.Arrays;

/**
* @author vmatsukevich
*         Date: 11/28/13
*         Time: 10:41 AM
*/
class DelimitedListFormatter<T> {

    public String formatAsDelimitedList(Iterable<T> iterable, String delimiter, String wrapper) {
        if (!iterable.iterator().hasNext()) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        boolean delimiterNeed = false;
        for (T item : iterable) {
            if (delimiterNeed) {
                result.append(delimiter);
            } else {
                delimiterNeed = true;
            }

            if (wrapper != null) {
                result.append(wrapper);
            }

            result.append(format(item));

            if (wrapper != null) {
                result.append(wrapper);
            }
        }

        return result.toString();
    }

    public String formatAsDelimitedList(T[] items, String delimiter, String wrapper) {
        return formatAsDelimitedList(Arrays.asList(items), delimiter, wrapper);
    }

    protected String format(T item) {
        return item.toString();
    }
}
