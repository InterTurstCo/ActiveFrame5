package ru.intertrust.cm.performance.dataset.generatefields;

import java.util.List;
import java.util.Random;

import ru.intertrust.cm.performance.dataset.xmltypes.FieldType;
import ru.intertrust.cm.performance.dataset.xmltypes.LongType;
import ru.intertrust.cm.performance.dataset.xmltypes.ObjectType;
import ru.intertrust.cm.performance.dataset.xmltypes.StringType;
import ru.intertrust.cm.performance.dataset.xmltypes.TemplateType;

public class GenerateLongField {
    protected String field;
    protected Long value;

    /**
     * метод возвращает вычисленное наименование поля
     * */
    public String getField() {
        return field;
    }

    /**
     * метод возвращает вычисленное значение аргумента
     * */
    public Long getValue() {
        return value;
    }

    /**
     * метод генерирует строковое поле доменного объекта, путем анализа
     * инструкции
     * 
     * */
    public void generateField(LongType longType, List<TemplateType> templateList, String type) {
        if (longType.getName() != null) {
            field = longType.getName();
        } else {
            field = getNameFromTemplate(templateList, type);
        }

        if ((Long) longType.getValue() != null) {
            value = longType.getValue();
        } else {
            Long minValue = -1L;
            Long maxValue = -1L;

            if ((Long) longType.getMinValue() != null) {
                minValue = longType.getMinValue();
            }
            if ((Long) longType.getMaxValue() != null) {
                maxValue = longType.getMaxValue();
            }

            value = getNumberOfRange(minValue, maxValue);
        }

    }

    private Long getNumberOfRange(Long minValue, Long maxValue) {
        Long lgth = 0L;
        
        // Инициализируем генератор
        Random rnd = new Random(System.currentTimeMillis());
        long longRand = rnd.nextLong();
        // Получаем случайное число в диапазоне от min до max (включительно)
        lgth = (Math.abs(longRand % (maxValue - minValue)) + minValue);

        return lgth;
    }

    private String getNameFromTemplate(List<TemplateType> templateList, String type) {
        String name = "";
        boolean resultOk = false;
        for (TemplateType template : templateList) {
            // получаем объект из шаблона
            ObjectType object = template.getObject();
            // получим тип объекта
            String objectTemplateType = object.getType();
            // если тип у объекта есть
            if (objectTemplateType != null) {
                // сравниваем тип объекта из шаблона с типом объекта из сета
                if (objectTemplateType.equals(type)) {
                    // если типы совпали, то получим список полей объекта из
                    // шаблона
                    List<FieldType> fieldTypeList = object.getStringOrDateTimeOrReference();
                    for (FieldType field : fieldTypeList) {
                        // найдем поле с инструкциями для строки
                        if (field instanceof LongType) {
                            LongType longType = (LongType) field;
                            name = longType.getName();
                            resultOk = true;
                        }
                    }
                }
            }
        }

        // если не нашли шаблона удовлетворяющего типу
        // переходим к поиску шаблона без типа
        if (!resultOk) {
            for (TemplateType template : templateList) {
                // получаем объект из шаблона
                ObjectType object = template.getObject();
                // получим тип объекта
                String objectTemplateType = object.getType();
                // если типа у объекта нету
                if (objectTemplateType == null) {
                    List<FieldType> fieldTypeList = object.getStringOrDateTimeOrReference();
                    for (FieldType field : fieldTypeList) {
                        // найдем поле с инструкциями для строки
                        if (field instanceof LongType) {
                            LongType longType = (LongType) field;
                            name = longType.getName();
                        }
                    }
                }
            }
        }

        return name;
    }
}
