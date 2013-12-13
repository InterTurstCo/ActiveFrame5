package ru.intertrust.cm.core.business.load;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;

import ru.intertrust.cm.core.business.api.ImportDataService;
import ru.intertrust.cm.core.business.api.dto.DateTimeWithTimeZone;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.FieldType;
import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.config.ReferenceFieldConfig;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.CollectionsDao;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.model.FatalException;

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
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            String line = null;
            int lineNum = 0;

            //имя типа доменного объекта который будет создаваться
            typeName = null;
            //Ключевые поля, которые используются для поиска импортируемой записи при обновление, и для исключения дублирования при создание
            keys = null;
            //Имена полей
            fields = null;

            //итератор по строкам
            while ((line = reader.readLine()) != null) {
                //Первые две строки это метаданные
                if (lineNum == 0) {
                    //Метаданные
                    String[] metaDatas = line.split(";");
                    for (String metaData : metaDatas) {
                        String[] metaItem = metaData.split("=");
                        if (metaItem[0].equalsIgnoreCase(ImportDataService.TYPE_NAME)) {
                            typeName = metaItem[1];
                        } else if (metaItem[0].equalsIgnoreCase(ImportDataService.KEYS)) {
                            keys = metaItem[1].split(",");
                        }
                    }
                } else if (lineNum == 1) {
                    //Имена полей
                    fields = line.split(";");
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
     * Импорт одной строки
     * @param line
     * @param typeName
     * @param importMode
     * @param keys
     * @param fields
     * @throws ParseException
     */
    private void importLine(String line) throws ParseException {
        AccessToken accessToken = accessService.createCollectionAccessToken(login);
        //Разделяем строку на значения
        String[] fieldValues = line.split(";");
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

                } else if (fieldConfig.getFieldType() == FieldType.DATETIME) {
                    domainObject.setTimestamp(fieldName, timeFofmat.parse(fieldValues[i]));
                } else if (fieldConfig.getFieldType() == FieldType.DATETIMEWITHTIMEZONE) {
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
                } else if (fieldConfig.getFieldType() == FieldType.DECIMAL) {
                    domainObject.setDecimal(fieldName, new BigDecimal(fieldValues[i]));
                } else if (fieldConfig.getFieldType() == FieldType.LONG) {
                    domainObject.setLong(fieldName, Long.parseLong(fieldValues[i]));
                } else if (fieldConfig.getFieldType() == FieldType.REFERENCE) {
                    //Здесь будут выражения в формате type.field="Значение поля" или field="Значение поля"
                    domainObject.setReference(fieldName, getReference(fieldValues[i]));
                } else if (fieldConfig.getFieldType() == FieldType.TIMELESSDATE) {
                    domainObject.setTimestamp(fieldName, dateFofmat.parse(fieldValues[i]));
                } else {
                    //В остальных случаях считаем строкой
                    domainObject.setString(fieldName, fieldValues[i]);
                }
                domainObjectDao.save(domainObject, accessToken);
            } else {
                throw new FatalException("Fileld " + fieldName + " not found in type " + typeName);
            }
        }
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
    private DomainObject getReference(String referenceValueAsString) {
        if (referenceValueAsString.toLowerCase().startsWith("select")) {
            getReferenceFromSelect(referenceValueAsString);
        } else {
            getReferenceFromExpression(referenceValueAsString);
        }
        return null;
    }

    private Id getReferenceFromExpression(String referenceValueAsString) {
        String type = null;
        String value = null;
        String fieldName = null;
        FieldConfig fieldConfig = null;
        String[] field = null;
        if (referenceValueAsString.contains("\\.")){
            //Есть точка, значит в имени поля есть тип, получаем его
            String[] typeAndField = referenceValueAsString.split("\\.");
            type = typeAndField[0];            
            field = typeAndField[1].split("=");
            fieldConfig = configurationExplorer.getFieldConfig(type, field[0]);
        }else{
            //Точки нет, значит берем тип из конфигурации поля
            field = referenceValueAsString.split("=");
            fieldConfig = configurationExplorer.getFieldConfig(typeName, field[0]);
            type = ((ReferenceFieldConfig)fieldConfig).getType();
        }
        
        fieldName = field[0];
        
        if (fieldConfig.getFieldType() == FieldType.LONG){
            value = field[1];
        }else{
            value = "'" + field[1] + "'";
        }
        
        String query = "select t.id from " + type + ".t where ";
        query += "t." + fieldName + "=" + value;
        return getReferenceFromSelect(query);
    }

    private Id getReferenceFromSelect(String query) {
        AccessToken accessToken = accessService.createCollectionAccessToken(login);
        IdentifiableObjectCollection collection = collectionsDao.findCollectionByQuery(query, 0, 1000, accessToken);
        Id result = null;
        if (collection.size() > 0){
            result = collection.get(0).getId();
        }
        return result;
    }

    private DomainObject findDomainObject(String[] fieldValues) {
        String query = "select t.id from " + typeName + " ";
        query += "where ";
        for (int i=0; i<keys.length; i++) {
            if (i > 0){
                query += " and ";
            }
            query += "t." + keys[i] + "=" + getFieldQueryFragment(fieldValues, keys[i]);
        }
        AccessToken accessToken = accessService.createCollectionAccessToken(login);
        IdentifiableObjectCollection collection = collectionsDao.findCollectionByQuery(query, 0, 1000, accessToken);
        DomainObject result = null;
        if (collection.size() > 0){
            result = domainObjectDao.find(collection.get(0).getId(), accessToken);
        }
        return result;
    }

    private String getFieldQueryFragment(String[] fieldValues, String fieldName) {
        //Строим таблицу соответствия имени поля и его индекса, для оптимизации
        if (fieldIndex == null){
            fieldIndex = new Hashtable<String, Integer>();
            for (int i=0; i< fields.length; i++) {
                fieldIndex.put(fieldName, i);
            }
        }
        
        //Получение значения поля
        String valueAsString = fieldValues[fieldIndex.get(fieldName)];
        //Получение конфигурации поля
        FieldConfig fieldConfig = configurationExplorer.getFieldConfig(typeName, fieldName);
        //Пока поддерживаем только строки и Long Вряд ли что то другое будет ключем
        String result = null;
        if (fieldConfig.getFieldType() == FieldType.LONG){
            result = valueAsString;
        }else{
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
