package ru.intertrust.cm.performance.dataset.generatefields;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;
import java.util.Random;

import ru.intertrust.cm.performance.dataset.RandomGenerators;
import ru.intertrust.cm.performance.dataset.xmltypes.DecimalType;
import ru.intertrust.cm.performance.dataset.xmltypes.FieldType;
import ru.intertrust.cm.performance.dataset.xmltypes.ObjectType;
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

    public void generateField(DecimalType decimalType, List<TemplateType> templateList, String type) throws IOException {
        if (decimalType.getName() != null) {
            field = decimalType.getName();
        } else {
            field = getNameFromTemplate(templateList, type);
        }

        if (decimalType.getValue() != null) {
            value = decimalType.getValue();
        } else {
            /*
             * BigDecimal minValue = new BigDecimal(-1.0); BigDecimal maxValue =
             * new BigDecimal(-1.0);
             *
             * if ((BigDecimal) decimalType.getMinValue() != null) { minValue =
             * decimalType.getMinValue(); } if ((BigDecimal)
             * decimalType.getMaxValue() != null) { maxValue =
             * decimalType.getMaxValue(); }
             *
             * value = getRandomValue(new Random(), minValue.doubleValue(),
             * maxValue.doubleValue(), 3);
             */
            RandomGenerators randomGenerators = RandomGenerators.getInstance();
            value = randomGenerators.getUniform(decimalType.getMinValue(), decimalType.getMaxValue());
        }

    }

    public void generateField(String fieldName, int precision, int scale) throws IOException {

        field = fieldName;

        BigDecimal maxValue = BigDecimal.valueOf(100000000, scale);
        BigDecimal minValue = BigDecimal.valueOf(0, scale);

        RandomGenerators randomGenerators = RandomGenerators.getInstance();
        value = randomGenerators.getUniform(minValue, maxValue);

    }

    public static BigDecimal getRandomValue(final Random random, final Double lowerBound, final Double upperBound,
            final int decimalPlaces) {

        if (lowerBound < 0.0 || upperBound <= lowerBound || decimalPlaces < 0) {
            throw new IllegalArgumentException("Put error message here");
        }

        final double dbl = ((random == null ? new Random() : random).nextDouble() * (upperBound - lowerBound))
                + lowerBound;

        BigDecimal randFromDouble = BigDecimal.valueOf (dbl);
        return randFromDouble;

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
