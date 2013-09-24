package ru.intertrust.cm.performance.dataset.generatefields;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Random;

import ru.intertrust.cm.performance.dataset.xmltypes.*;

public class GenerateDateTimeField {
    protected String field;
    protected Date value;
    protected boolean ignore;

    public String getField() {
        return field;
    }

    public Date getValue() {
        return value;
    }

    public boolean ignoreField() {
        return ignore;
    }

    public void generateField(DateTimeType dateTimeType, List<TemplateType> templateList, String type) {
        if (dateTimeType.getName() != null) {
            field = dateTimeType.getName();
        } else {
            field = getNameFromTemplate(templateList, type);
        }
        try {

            if ((Date) dateTimeType.getValue() != null) {
                value = dateTimeType.getValue();
            } else {
                Date minValue = null;
                Date maxValue = null;

                if ((Date) dateTimeType.getMinValue() != null) {
                    minValue = dateTimeType.getMinValue();
                }
                if ((Date) dateTimeType.getMaxValue() != null) {
                    maxValue = dateTimeType.getMaxValue();
                }

                value = getDateOfRange(minValue, maxValue);
                ignore = false;
            }

        } catch (Exception e) {
            e.printStackTrace();
            ignore = true;
        }

    }

    private Date getDateOfRange(Date minValue, Date maxValue) {

        long min = minValue.getTime();
        long max = maxValue.getTime();

        Date date = new Date(min + (long) (Math.random() * (max - min)));

        return date;
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
                        if (field instanceof DateTimeType) {
                            DateTimeType dateTimeType = (DateTimeType) field;
                            name = dateTimeType.getName();
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
                        if (field instanceof DateTimeType) {
                            DateTimeType dateTimeType = (DateTimeType) field;
                            name = dateTimeType.getName();
                        }
                    }
                }
            }
        }

        return name;
    }
}
