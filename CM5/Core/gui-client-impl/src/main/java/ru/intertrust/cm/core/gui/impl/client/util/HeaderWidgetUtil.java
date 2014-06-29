package ru.intertrust.cm.core.gui.impl.client.util;

import java.util.ArrayList;
import java.util.List;

import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.EMPTY_VALUE;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 25.06.2014
 *         Time: 23:30
 */
public class HeaderWidgetUtil {

    public static String initFilterValuesRepresentation(String valueSeparator, List<String> initialFilterValues) {
        if (initialFilterValues == null) {
            return EMPTY_VALUE;
        }

        StringBuilder filterValueBuilder = new StringBuilder();
        int size = initialFilterValues.size();
        int lastElementIndex = size - 1;
        for (int i = 0; i < size; i++) {
            String partOfValue = initialFilterValues.get(i);
            filterValueBuilder.append(partOfValue.replaceAll("%", EMPTY_VALUE));
            if (i != lastElementIndex && valueSeparator != null) {
                filterValueBuilder.append(valueSeparator);
            }

        }
        return filterValueBuilder.toString();

    }

    public static List<String> getFilterValues(String valueSeparator, String filterValuesRepresentation) {
        List<String> filterValues = new ArrayList<String>();
        if(valueSeparator == null){
            filterValues.add(filterValuesRepresentation);
            return filterValues;
        }
        String[] splitValues = filterValuesRepresentation.split(valueSeparator);
        for (String splitValue : splitValues) {
            filterValues.add(splitValue);

        }
        return filterValues;
    }
}
