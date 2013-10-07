package ru.intertrust.cm.performance.dataset.generatefields;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import ru.intertrust.cm.performance.dataset.RandomGenerators;
import ru.intertrust.cm.performance.dataset.xmltypes.DateTimeType;
import ru.intertrust.cm.performance.dataset.xmltypes.FieldType;
import ru.intertrust.cm.performance.dataset.xmltypes.ObjectType;
import ru.intertrust.cm.performance.dataset.xmltypes.TemplateType;

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

    public void generateField(DateTimeType dateTimeType, List<TemplateType> templateList, String type)
            throws IOException {
        if (dateTimeType.getName() != null) {
            field = dateTimeType.getName();
        } else {
            field = getNameFromTemplate(templateList, type);
        }

        if (dateTimeType.getValue() != null) {
            value = dateTimeType.getValue();
        } else {
            Date minValue = null;
            Date maxValue = null;

            if (dateTimeType.getMinValue() != null) {
                minValue = dateTimeType.getMinValue();
            }
            if (dateTimeType.getMaxValue() != null) {
                maxValue = dateTimeType.getMaxValue();
            }
            RandomGenerators randomGenerators = RandomGenerators.getInstance();
            value = randomGenerators.getUniform(minValue, maxValue);
            // value = getDateOfRange(minValue, maxValue);
            ignore = false;
        }
    }

    public void generateField(String nameField) throws IOException {
        Calendar c1 = Calendar.getInstance();
        c1.set(2010, 0, 01);
        Date minValue = new Date(c1.getTimeInMillis());
        c1.set(2014, 11, 31);
        Date maxValue = new Date(c1.getTimeInMillis());

        field = nameField;

        value = getDateOfRange(minValue, maxValue);
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
