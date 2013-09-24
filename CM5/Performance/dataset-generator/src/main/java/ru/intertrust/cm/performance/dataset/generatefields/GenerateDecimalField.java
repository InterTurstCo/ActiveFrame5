package ru.intertrust.cm.performance.dataset.generatefields;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;

import ru.intertrust.cm.performance.dataset.xmltypes.DecimalType;
import ru.intertrust.cm.performance.dataset.xmltypes.FieldType;
import ru.intertrust.cm.performance.dataset.xmltypes.LongType;
import ru.intertrust.cm.performance.dataset.xmltypes.ObjectType;
import ru.intertrust.cm.performance.dataset.xmltypes.StringType;
import ru.intertrust.cm.performance.dataset.xmltypes.TemplateType;

public class GenerateDecimalField {
    protected String field;
    protected BigDecimal value;

    public String getField() {
        return field;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void generateField(DecimalType decimalType, List<TemplateType> templateList, String type) {
        if (decimalType.getName() != null) {
            field = decimalType.getName();
        } else {
            field = getNameFromTemplate(templateList, type);
        }
        
        if ((BigDecimal) decimalType.getValue() != null) {
            value = decimalType.getValue();
        } else {
            BigDecimal minValue = new BigDecimal(-1.0);
            BigDecimal maxValue = new BigDecimal(-1.0);

            if ((BigDecimal) decimalType.getMinValue() != null) {
                minValue = decimalType.getMinValue();
            }
            if ((BigDecimal) decimalType.getMaxValue() != null) {
                maxValue = decimalType.getMaxValue();
            }

            value = getNumberOfRange(minValue, maxValue);
        }
        
    }
    
    private BigDecimal getNumberOfRange(BigDecimal minValue, BigDecimal maxValue) {
        BigDecimal lgth = new BigDecimal(0.0);
        
        BigDecimal randFromDouble = new BigDecimal(Math.random());
        lgth = randFromDouble.divide(maxValue,BigDecimal.ROUND_DOWN);

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
                        if (field instanceof DecimalType) {
                            DecimalType decimalType = (DecimalType) field;
                            name = decimalType.getName();
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
                        if (field instanceof DecimalType) {
                            DecimalType decimalType = (DecimalType) field;
                            name = decimalType.getName();
                        }
                    }
                }
            }
        }

        return name;
    }
}
