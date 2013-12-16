package ru.intertrust.cm.core.business.load;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import ru.intertrust.cm.core.business.api.ImportDataService;
import ru.intertrust.cm.core.business.api.dto.DateTimeWithTimeZone;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.FieldType;
import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.config.ReferenceFieldConfig;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.CollectionsDao;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.model.FatalException;

/**
 * Класс импорртирования одного файла
 * @author larin
 *
 */
public class ImportData {

    private String typeName;
    private String[] keys;
    private String[] fields;
    private CollectionsDao collectionsDao;
    private ConfigurationExplorer configurationExplorer;
    private DomainObjectDao domainObjectDao;
    private SimpleDateFormat dateFofmat = new SimpleDateFormat("dd.MM.yyyy");
    private SimpleDateFormat timeFofmat = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss");
    private AccessControlService accessService;
    private String login;
    private Hashtable<String, Integer> fieldIndex;

    /**
     * Конструктор. Инициалитзирует класс нужными сервисами
     * @param collectionsDao
     * @param configurationExplorer
     * @param domainObjectDao
     * @param accessService
     * @param login
     */
    public ImportData(CollectionsDao collectionsDao, ConfigurationExplorer configurationExplorer,
            DomainObjectDao domainObjectDao, AccessControlService accessService, String login) {
        this.collectionsDao = collectionsDao;
        this.configurationExplorer = configurationExplorer;
        this.domainObjectDao = domainObjectDao;
        this.accessService = accessService;
        this.login = login;
    }

    /**
     * Загрузка одного файла с данными
     * @param loadFileAsByteArray
     */
    public void importData(byte[] loadFileAsByteArray) {
        try {
            ByteArrayInputStream input = new ByteArrayInputStream(loadFileAsByteArray);
            BufferedReader reader = new BufferedReader(new InputStreamReader(input, Charset.forName("ANSI-1251")));
            String readLine = null;
            int lineNum = 0;

            //имя типа доменного объекта который будет создаваться
            typeName = null;
            //Ключевые поля, которые используются для поиска импортируемой записи при обновление, и для исключения дублирования при создание
            keys = null;
            //Имена полей
            fields = null;

            //итератор по строкам
            while ((readLine = reader.readLine()) != null) {
                String line = readLine.replaceAll("\t", "");

                //Первые две строки это метаданные
                if (lineNum == 0) {
                    //Метаданные
                    String[] metaDatas = line.split(";");
                    for (String metaData : metaDatas) {
                        String normalMetaData = getNormalizationField(metaData);
                        String[] metaItem = normalMetaData.split("=");
                        if (metaItem[0].equalsIgnoreCase(ImportDataService.TYPE_NAME)) {
                            typeName = metaItem[1];
                        } else if (metaItem[0].equalsIgnoreCase(ImportDataService.KEYS)) {
                            keys = metaItem[1].split(",");
                        }
                    }
                } else if (lineNum == 1) {
                    //Имена полей
                    fields = line.split(";");
                    for (int i = 0; i < fields.length; i++) {
                        fields[i] = getNormalizationField(fields[i]);
                    }
                } else {
                    //Импорт одной строки
                    importLine(line);
                }

                lineNum++;
            }
        } catch (Exception ex) {
            throw new FatalException("Error load data", ex);
        }
    }

    /**
     * Избавляемся от лишних кавычек
     * @param value
     * @return
     */
    private String getNormalizationField(String value) {
        String result = null;
        if (value.startsWith("\"") && value.endsWith("\"")) {
            result = value.substring(1, value.length() - 1);
            result = result.replaceAll("\"\"", "\"");
        } else {
            result = value.trim();
        }
        return result;
    }

    /**
     * Импорт одной строки
     * @param line
     * @param typeName
     * @param importMode
     * @param keys
     * @param fields
     * @throws ParseException
     */
    private void importLine(String line) throws ParseException {
        AccessToken accessToken = null;
        if (login == null){
            accessToken = accessService.createSystemAccessToken(this.getClass().getName());
        }else{
            accessToken = accessService.createCollectionAccessToken(login);
        }
        //Разделяем строку на значения
        String[] fieldValues = line.split(";");
        List<String> fieldValuesList = new ArrayList<String>();

        //Избавляемся от кавычек и обрабатываем пустые значения в конце строки
        for (int i = 0; i < fields.length; i++) {
            if (fieldValues.length < i + 1) {
                fieldValuesList.add("");
            } else {
                fieldValuesList.add(getNormalizationField(fieldValues[i]));
            }
        }
        fieldValues = fieldValuesList.toArray(new String[fieldValuesList.size()]);

        //Получение доменного объекта по ключевым полям
        DomainObject domainObject = findDomainObject(fieldValues);
        if (domainObject == null) {
            //Создание доменного объекта
            domainObject = createDomainObject(typeName);
        }
        //Установка полей
        for (int i = 0; i < fields.length; i++) {
            String fieldName = fields[i];
            FieldConfig fieldConfig = configurationExplorer.getFieldConfig(typeName, fieldName);
            if (fieldConfig != null) {
                if (fieldConfig.getFieldType() == FieldType.BOOLEAN) {
                    if (fieldValues[i].length() == 0){
                        domainObject.setBoolean(fieldName, null);
                    }else{
                        domainObject.setBoolean(fieldName, Boolean.valueOf(fieldValues[i]));
                    }
                } else if (fieldConfig.getFieldType() == FieldType.DATETIME) {
                    if (fieldValues[i].length() == 0){
                        domainObject.setTimestamp(fieldName, null);
                    }else{
                        domainObject.setTimestamp(fieldName, timeFofmat.parse(fieldValues[i]));
                    }
                } else if (fieldConfig.getFieldType() == FieldType.DATETIMEWITHTIMEZONE) {
                    if (fieldValues[i].length() == 0){
                        domainObject.setDateTimeWithTimeZone(fieldName, null);
                    }else{
                        Date date = timeFofmat.parse(fieldValues[i]);
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(date);
                        DateTimeWithTimeZone dateTimeWithTimeZone = new DateTimeWithTimeZone(
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH),
                                calendar.get(Calendar.HOUR),
                                calendar.get(Calendar.MINUTE),
                                calendar.get(Calendar.SECOND)
                                );
                        domainObject.setDateTimeWithTimeZone(fieldName, dateTimeWithTimeZone);
                    }
                } else if (fieldConfig.getFieldType() == FieldType.DECIMAL) {
                    if (fieldValues[i].length() == 0){
                        domainObject.setDecimal(fieldName, null);
                    }else{
                        domainObject.setDecimal(fieldName, new BigDecimal(fieldValues[i]));
                    }
                } else if (fieldConfig.getFieldType() == FieldType.LONG) {
                    if (fieldValues[i].length() == 0){
                        domainObject.setLong(fieldName, null);
                    }else{
                        domainObject.setLong(fieldName, Long.parseLong(fieldValues[i]));
                    }
                } else if (fieldConfig.getFieldType() == FieldType.REFERENCE) {
                    //Здесь будут выражения в формате type.field="Значение поля" или field="Значение поля" или запрос
                    domainObject.setReference(fieldName, getReference(fieldName, fieldValues[i]));
                } else if (fieldConfig.getFieldType() == FieldType.TIMELESSDATE) {
                    if (fieldValues[i].length() == 0){
                        domainObject.setTimestamp(fieldName, null);
                    }else{
                        domainObject.setTimestamp(fieldName, dateFofmat.parse(fieldValues[i]));
                    }
                } else {
                    //В остальных случаях считаем строкой
                    if (fieldValues[i].length() == 0){
                        domainObject.setString(fieldName, null);
                    }else{
                        domainObject.setString(fieldName, fieldValues[i]);
                    }
                }
            } else {
                throw new FatalException("Fileld " + fieldName + " not found in type " + typeName);
            }
        }
        domainObjectDao.save(domainObject, accessToken);
    }

    /**
     * Возможные варианты:
     * <ol>
     * type.field="Значение поля"
     * <ol>
     * field="Значение поля"
     * <ol>
     * запрос который выдаст в результате поле типа reference
     * @param string
     * @return
     */
    private Id getReference(String fieldName, String referenceValueAsString) {
        Id result = null;
        if (referenceValueAsString != null && referenceValueAsString.length() > 0) {
            if (referenceValueAsString.toLowerCase().startsWith("select")) {
                result = getReferenceFromSelect(referenceValueAsString);
            } else {
                result = getReferenceFromExpression(fieldName, referenceValueAsString);
            }
        }
        return result;
    }

    /**
     * Получение ссылки из выражения
     * @param refFieldName
     * @param referenceValueAsString
     * @return
     */
    private Id getReferenceFromExpression(String refFieldName, String referenceValueAsString) {
        String type = null;
        String value = null;
        String fieldName = null;
        FieldConfig fieldConfig = null;
        FieldConfig refFieldConfig = configurationExplorer.getFieldConfig(typeName, refFieldName);
        String[] field = null;
        if (referenceValueAsString.indexOf(".") > -1) {
            //Есть точка, значит в имени поля есть тип, получаем его
            String[] typeAndField = referenceValueAsString.split("\\.");
            type = typeAndField[0];
            field = typeAndField[1].split("=");
        } else {
            //Точки нет, значит берем тип из конфигурации поля
            field = referenceValueAsString.split("=");
            type = ((ReferenceFieldConfig) refFieldConfig).getType();
        }
        fieldConfig = configurationExplorer.getFieldConfig(type, field[0]);

        fieldName = field[0];

        if (fieldConfig.getFieldType() == FieldType.LONG) {
            value = getNormalizationField(field[1]);
        } else {
            value = "'" + getNormalizationField(field[1]) + "'";
        }

        String query = getQuery(type, new String[]{fieldName}, new String[]{value}) ;
        return getReferenceFromSelect(query);
    }

    /**
     * Получение ссылки с помощью запроса
     * @param query
     * @return
     */
    private Id getReferenceFromSelect(String query) {
        AccessToken accessToken = null;
        if (login == null){
            accessToken = accessService.createSystemAccessToken(this.getClass().getName());
        }else{
            accessToken = accessService.createCollectionAccessToken(login);
        }
        IdentifiableObjectCollection collection = collectionsDao.findCollectionByQuery(query, 0, 1000, accessToken);
        Id result = null;
        if (collection.size() > 0) {
            result = collection.get(0).getId();
        }
        return result;
    }

    /**
     * Поиск доменного объекта
     * @param fieldValues
     * @return
     */
    private DomainObject findDomainObject(String[] fieldValues) {
        String[] values = new String[keys.length];
        for (int i = 0; i < keys.length; i++) {
            values[i] = getFieldQueryFragment(fieldValues, keys[i]);
        }

        String query = getQuery(typeName, keys, values);
        AccessToken accessToken = null;
        if (login == null){
            accessToken = accessService.createSystemAccessToken(this.getClass().getName());
        }else{
            accessToken = accessService.createCollectionAccessToken(login);
        }
        IdentifiableObjectCollection collection = collectionsDao.findCollectionByQuery(query, 0, 1000, accessToken);
        DomainObject result = null;
        if (collection.size() > 0) {
            result = domainObjectDao.find(collection.get(0).getId(), accessToken);
        }
        return result;
    }

    /**
     * Формирование запроса с учетом иерархии типов
     * @param typeName
     * @param confitionFields
     * @param conditionValues
     * @return
     */
    private String getQuery(String typeName, String[] confitionFields, String[] conditionValues) {
        //Получение конфигурации типа
        DomainObjectTypeConfig config = configurationExplorer.getConfig(DomainObjectTypeConfig.class, typeName);
        String result = null;
        int typeNo = 0;
        result = "select t0.id ";
        String from = null;
        String where = null;
        //Формируем запрос
        do {
            //Заполняем where
            for (FieldConfig fieldConfig : config.getFieldConfigs()) {
                for (int i = 0; i < confitionFields.length; i++) {
                    if (confitionFields[i].equalsIgnoreCase(fieldConfig.getName())) {
                        if (where == null) {
                            where = " where t" + typeNo + "." + fieldConfig.getName() + " = " + conditionValues[i];
                        } else {
                            where += " and t" + typeNo + "." + fieldConfig.getName() + " = " + conditionValues[i];
                        }
                    }
                }
            }
            //Заполяем типы
            if (typeNo == 0) {
                from = " from " + config.getName() + " t" + typeNo + " ";
            } else {
                from +=
                        "inner join " + config.getName() + " t" + typeNo + " on (t" + typeNo + ".id = t" + (typeNo - 1)
                                + ".id) ";
            }
            //Проверка на наличие родителя
            if (config.getExtendsAttribute() != null && config.getExtendsAttribute().length() > 0) {
                config = configurationExplorer.getConfig(DomainObjectTypeConfig.class, config.getExtendsAttribute());
            } else {
                config = null;
            }
            typeNo++;
        } while (config != null);
        result += from;
        result += where;
        return result;
    }

    /**
     * Получение значение поля, для подстановки в запрос, с учетом типа поля
     * @param fieldValues
     * @param fieldName
     * @return
     */
    private String getFieldQueryFragment(String[] fieldValues, String fieldName) {
        //Строим таблицу соответствия имени поля и его индекса, для оптимизации
        if (fieldIndex == null) {
            fieldIndex = new Hashtable<String, Integer>();
            for (int i = 0; i < fields.length; i++) {
                fieldIndex.put(fields[i], i);
            }
        }

        //Получение значения поля
        String valueAsString = fieldValues[fieldIndex.get(fieldName)];
        //Получение конфигурации поля
        FieldConfig fieldConfig = configurationExplorer.getFieldConfig(typeName, fieldName);
        //Пока поддерживаем только строки и Long Вряд ли что то другое будет ключем
        String result = null;
        if (fieldConfig.getFieldType() == FieldType.LONG) {
            result = valueAsString;
        } else {
            result = "'" + valueAsString + "'";
        }
        return result;
    }

    /**
     * Создание доменного объекта
     * @param typeName
     * @return
     */
    private DomainObject createDomainObject(String typeName) {
        GenericDomainObject domainObject = new GenericDomainObject();
        domainObject.setTypeName(typeName);
        Date currentDate = new Date();
        domainObject.setCreatedDate(currentDate);
        domainObject.setModifiedDate(currentDate);
        return domainObject;
    }

}
