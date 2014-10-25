package ru.intertrust.cm.core.gui.impl.server.widget;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 03.05.14
 *         Time: 13:15
 */
//TODO rename class
public class PatternIterator {
    String lastElement;
    String fieldPath;
    int index = -1;
    Map<String, ReferenceType> fieldPathMap = new LinkedHashMap<>();
    List<String> parts;

    public enum ReferenceType {
        FIELD,
        DIRECT_REFERENCE,
        BACK_REFERENCE_ONE_TO_ONE
    }

    public PatternIterator(String fieldPath) {
        this.fieldPath = fieldPath;
        initLastElement();
        parseFieldPath();
    }

    private void initLastElement() {
        String[] splitByDotAndOr = fieldPath.split("\\.|\\|");
        int index = splitByDotAndOr.length - 1;
        lastElement = splitByDotAndOr[index];
    }

    private void parseFieldPath() {
        parts = new LinkedList<>();
        String[] splitByDot = fieldPath.split("\\.");

        for (String partOfDotSplit : splitByDot) {
            String[] splitByOr = partOfDotSplit.split("\\|");
            int length = splitByOr.length;
            if (length == 1) {
                fieldPathMap.put(partOfDotSplit, ReferenceType.DIRECT_REFERENCE);
                parts.add(partOfDotSplit);
            } else {

                for (int i = 0; i < length; i++) {
                    String partOfOrSplit = splitByOr[i];

                    fieldPathMap.put(partOfOrSplit, ReferenceType.BACK_REFERENCE_ONE_TO_ONE);

                    parts.add(partOfOrSplit);
                }
            }
        }

        fieldPathMap.put(lastElement, ReferenceType.FIELD);
    }

    public boolean moveToNext() {
        index++;
        if (index == parts.size()) {
            return false;
        }
        return true;
    }

    public String getValue() {
        return parts.get(index);

    }

    public ReferenceType getType() {
        return fieldPathMap.get(parts.get(index));
    }
}

