package ru.intertrust.cm.core.dao.impl.sqlparser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FieldDataHelper {

    public static void addFieldData(Map<String, List<FieldData>> columnToConfigMapping, FieldData fieldData) {
        final List<FieldData> fieldDataList = columnToConfigMapping.computeIfAbsent(fieldData.getColumnName(), k -> new ArrayList<>());
        if (fieldDataList.isEmpty()) {
            fieldDataList.add(fieldData);
        } else {
            // Проверим, совпадает ли тип ДО, если да, то значит уже добавили, если нет, то добавляем новый элемент
            if (fieldDataList.stream().noneMatch(it -> it.getDoTypeName().equals(fieldData.getDoTypeName()))) {
                fieldDataList.add(fieldData);
            }
        }
    }
}
