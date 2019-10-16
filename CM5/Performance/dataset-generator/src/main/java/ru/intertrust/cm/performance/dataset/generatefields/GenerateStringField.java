package ru.intertrust.cm.performance.dataset.generatefields;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.List;
import java.util.Random;

import ru.intertrust.cm.performance.dataset.DatasetGenerationServiceImpl;
import ru.intertrust.cm.performance.dataset.RandomGenerators;
import ru.intertrust.cm.performance.dataset.xmltypes.FieldType;
import ru.intertrust.cm.performance.dataset.xmltypes.ObjectType;
import ru.intertrust.cm.performance.dataset.xmltypes.StringType;
import ru.intertrust.cm.performance.dataset.xmltypes.TemplateType;

/**
 * Класс предназначен для формирования строкового поля в доменном объекте
 *
 * */
public class GenerateStringField {
    protected String field;
    protected String value;

    /**
     * метод возвращает вычисленное наименование поля
     * */
    public String getField() {
        return field;
    }

    /**
     * метод возвращает вычисленное значение аргумента
     * */
    public String getValue() {
        return value;
    }

    /**
     * метод генерирует строковое поле доменного объекта, путем анализа
     * инструкции
     *
     * */
    public void generateField(StringType stringType, List<TemplateType> templateList, String type,
            DatasetGenerationServiceImpl dgsi) throws IOException, NoSuchAlgorithmException {
        if (stringType.getName() != null) {
            field = stringType.getName();
        } else {
            field = getNameFromTemplate(templateList, type);
        }
        if (stringType.getValue() != null) {
            value = stringType.getValue();
        } else {
            if (dgsi.getUniqueInfo(type, field)) {
                int length = -1;
                int minLength = -1;
                int maxLength = -1;
                if (stringType.getLength() != null) {
                    length = stringType.getLength();
                }
                if (stringType.getMinLength() != null) {
                    minLength = stringType.getMinLength();
                }
                if (stringType.getMaxLength() != null) {
                    maxLength = stringType.getMaxLength();
                }

                int len = getLengthOfString(length, minLength, maxLength);
                value = randomString(len);
            } else {
                RandomGenerators randomGenerators = RandomGenerators.getInstance();
                if (stringType.getLength() != null) {
                    value = randomGenerators.getText(stringType.getLength(), stringType.getLength());
                }
                if (stringType.getMinLength() != null && stringType.getMaxLength() != null) {
                    value = randomGenerators.getText(stringType.getMinLength(), stringType.getMaxLength());
                }
            }
        }
    }

    public void generateField(String stringFieldName, int length) throws IOException, NoSuchAlgorithmException {

        field = stringFieldName;

        value = randomString(length);
    }

    private int getLengthOfString(int length, int minLength, int maxLength) {
        int lgth = 0;
        if (length != -1) {
            lgth = length;
        } else {
            // Инициализируем генератор
            Random rnd = new Random(System.currentTimeMillis());
            // Получаем случайное число в диапазоне от min до max (включительно)
            lgth = minLength + rnd.nextInt(maxLength - minLength + 1);
        }
        return lgth;
    }

    private String randomString(int length) throws NoSuchAlgorithmException {

        String tempString = String.valueOf(System.nanoTime());
        if (tempString.length() > length) {
            // return tempString.substring(0, length);
            return tempString.substring(tempString.length() - length);
        } else {

            char[] signsSet = { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q',
                    'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };

            int rightLimit = signsSet.length;

            StringBuilder randomName = new StringBuilder("");

            Random random = SecureRandom.getInstanceStrong();

            for (int i = tempString.length(); i < length; i++) {
                randomName.append(signsSet[random.nextInt(rightLimit)]);
            }
            randomName.append(tempString);

            return randomName.toString();
        }
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
                        if (field instanceof StringType) {
                            StringType stringType = (StringType) field;
                            name = stringType.getName();
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
                        if (field instanceof StringType) {
                            StringType stringType = (StringType) field;
                            name = stringType.getName();
                        }
                    }
                }
            }
        }

        return name;
    }
}
