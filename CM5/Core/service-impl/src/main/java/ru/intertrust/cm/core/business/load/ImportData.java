package ru.intertrust.cm.core.business.load;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.TimeZone;

import ru.intertrust.cm.core.business.api.ImportDataService;
import ru.intertrust.cm.core.business.api.dto.BooleanValue;
import ru.intertrust.cm.core.business.api.dto.DateTimeValue;
import ru.intertrust.cm.core.business.api.dto.DateTimeWithTimeZone;
import ru.intertrust.cm.core.business.api.dto.DateTimeWithTimeZoneValue;
import ru.intertrust.cm.core.business.api.dto.DecimalValue;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.FieldType;
import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.LongValue;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.business.impl.BaseAttachmentServiceImpl;
import ru.intertrust.cm.core.config.AttachmentTypeConfig;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.config.ReferenceFieldConfig;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.access.DomainObjectAccessType;
import ru.intertrust.cm.core.dao.api.AttachmentContentDao;
import ru.intertrust.cm.core.dao.api.CollectionsDao;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.exception.DaoException;
import ru.intertrust.cm.core.model.FatalException;
import au.com.bytecode.opencsv.CSVReader;

/**
 * Класс импортирования одного файла
 * @author larin
 * 
 */
public class ImportData {

    public static final String ATTACHMENT_FIELD_NAME = "_ATTACHMENT_";
    public static final String DEFAULT_ENCODING = "ANSI-1251";
    private String typeName;
    private String[] keys;
    private String[] fields;
    private CollectionsDao collectionsDao;
    private ConfigurationExplorer configurationExplorer;
    private DomainObjectDao domainObjectDao;
    private AttachmentContentDao attachmentContentDao;
    private SimpleDateFormat dateFofmat = new SimpleDateFormat("dd.MM.yyyy");
    private SimpleDateFormat timeFofmat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    private AccessControlService accessService;
    private String login;
    private Hashtable<String, Integer> fieldIndex;
    private String emptyStringSymbol;

    /**
     * Конструктор. Инициалитзирует класс нужными сервисами
     * @param collectionsDao
     * @param configurationExplorer
     * @param domainObjectDao
     * @param accessService
     * @param login
     */
    public ImportData(CollectionsDao collectionsDao, ConfigurationExplorer configurationExplorer,
            DomainObjectDao domainObjectDao, AccessControlService accessService,
            AttachmentContentDao attachmentContentDao,
            String login) {
        this.collectionsDao = collectionsDao;
        this.configurationExplorer = configurationExplorer;
        this.domainObjectDao = domainObjectDao;
        this.accessService = accessService;
        this.attachmentContentDao = attachmentContentDao;
        this.login = login;
    }

    /**
     * Загрузка одного файла с данными
     * @param loadFileAsByteArray
     */
    public void importData(byte[] loadFileAsByteArray, String encoding, Boolean rewrite) {
        CSVReader reader = null;
        try {
            ByteArrayInputStream input = new ByteArrayInputStream(loadFileAsByteArray);
            reader =
                    new CSVReader(new InputStreamReader(input, encoding != null ? encoding : DEFAULT_ENCODING), ';',
                            '"');
            String[] readLine = null;
            int lineNum = 0;

            //имя типа доменного объекта который будет создаваться
            typeName = null;
            //Ключевые поля, которые используются для поиска импортируемой записи при обновление, и для исключения дублирования при создание
            keys = null;
            //Имена полей
            fields = null;
            //Символ означающий пустую строку
            emptyStringSymbol = null;

            //итератор по строкам
            while ((readLine = reader.readNext()) != null) {

                //Первые две строки это метаданные
                if (lineNum == 0) {
                    //Метаданные
                    for (String metaData : readLine) {
                        String normalMetaData = metaData.trim();
                        String[] metaItem = normalMetaData.split("=");
                        if (metaItem[0].equalsIgnoreCase(ImportDataService.TYPE_NAME)) {
                            typeName = metaItem[1];
                        } else if (metaItem[0].equalsIgnoreCase(ImportDataService.KEYS)) {
                            keys = metaItem[1].split(",");
                        } else if (metaItem[0].equalsIgnoreCase(ImportDataService.EMPTY_STRING_SYMBOL)) {
                            emptyStringSymbol = metaItem[1];
                        }
                    }
                } else if (lineNum == 1) {
                    //Имена полей
                    List<String> fieldList = new ArrayList<String>();
                    for (int i = 0; i < readLine.length; i++) {
                        if (readLine[i].trim().length() > 0) {
                            fieldList.add(readLine[i].trim());
                        }
                        fields = fieldList.toArray(new String[fieldList.size()]);
                    }
                } else {
                    //Импорт одной строки
                    importLine(readLine, rewrite);
                }

                lineNum++;
            }
        } catch (Exception ex) {
            throw new FatalException("Error load data. TypeName=" + typeName, ex);
        } finally {
            try {
                reader.close();
            } catch (Exception ignoreEx) {
            }
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
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    private void importLine(String[] line, Boolean rewrite) throws ParseException, IOException,
            NoSuchAlgorithmException {
        AccessToken accessToken = null;
        //Разделяем строку на значения
        String[] fieldValues = line;
        List<String> fieldValuesList = new ArrayList<String>();

        //тримим и обрабатываем пустые значения в конце строки
        for (int i = 0; i < fields.length; i++) {
            if (fieldValues.length < i + 1) {
                fieldValuesList.add("");
            } else {
                fieldValuesList.add(fieldValues[i].trim());
            }
        }
        fieldValues = fieldValuesList.toArray(new String[fieldValuesList.size()]);

        //Получение доменного объекта по ключевым полям
        DomainObject domainObject = findDomainObject(fieldValues);
        if (domainObject == null) {
            //Создание доменного объекта
            domainObject = createDomainObject(typeName);
        }

        //Если доменный объект новый или стоит флаг перезаписывать атрибуты то устанавливаем атрибуты
        if (rewrite || domainObject.isNew()) {

            String attachments = null;
            //Установка полей
            for (int i = 0; i < fields.length; i++) {
                String fieldName = fields[i];

                //Запоминаем старое значение
                Value oldValue = domainObject.getValue(fieldName);
                Value newValue = null;

                //Обрабатываем ключевое поле вложения
                if (ATTACHMENT_FIELD_NAME.equals(fieldName)) {
                    if (!fieldValues[i].isEmpty()) {
                        attachments = fieldValues[i];
                    }
                } else {

                    FieldConfig fieldConfig = configurationExplorer.getFieldConfig(typeName, fieldName);
                    if (fieldConfig != null) {
                        if (fieldConfig.getFieldType() == FieldType.BOOLEAN) {
                            if (fieldValues[i].length() != 0) {
                                newValue = new BooleanValue(Boolean.valueOf(fieldValues[i]));
                            }
                        } else if (fieldConfig.getFieldType() == FieldType.DATETIME) {
                            if (fieldValues[i].length() != 0) {
                                newValue = new DateTimeValue(timeFofmat.parse(fieldValues[i]));
                            }
                        } else if (fieldConfig.getFieldType() == FieldType.DATETIMEWITHTIMEZONE) {
                            if (fieldValues[i].length() != 0) {
                                Date date = timeFofmat.parse(fieldValues[i]);
                                Calendar calendar = Calendar.getInstance();
                                calendar.setTime(date);
                                DateTimeWithTimeZone dateTimeWithTimeZone = new DateTimeWithTimeZone(TimeZone.getDefault().getID(),
                                        calendar.get(Calendar.YEAR),
                                        calendar.get(Calendar.MONTH),
                                        calendar.get(Calendar.DAY_OF_MONTH),
                                        calendar.get(Calendar.HOUR),
                                        calendar.get(Calendar.MINUTE),
                                        calendar.get(Calendar.SECOND), 0
                                        );
                                newValue = new DateTimeWithTimeZoneValue(dateTimeWithTimeZone);
                            }
                        } else if (fieldConfig.getFieldType() == FieldType.DECIMAL) {
                            if (fieldValues[i].length() != 0) {
                                newValue = new DecimalValue(new BigDecimal(fieldValues[i]));
                            }
                        } else if (fieldConfig.getFieldType() == FieldType.LONG) {
                            if (fieldValues[i].length() != 0) {
                                newValue = new LongValue(Long.parseLong(fieldValues[i]));
                            }
                        } else if (fieldConfig.getFieldType() == FieldType.REFERENCE) {
                            //Здесь будут выражения в формате type.field="Значение поля" или field="Значение поля" или запрос
                            newValue = new ReferenceValue(getReference(fieldName, fieldValues[i]));
                        } else if (fieldConfig.getFieldType() == FieldType.TIMELESSDATE) {
                            if (fieldValues[i].length() != 0) {
                                newValue = new DateTimeValue(dateFofmat.parse(fieldValues[i]));
                            }
                        } else {
                            //В остальных случаях считаем строкой
                            if (fieldValues[i].length() == 0) {
                                newValue = null;
                            } else if (isEmptySimvol(fieldValues[i])) {
                                //Символ "_" строки означает у нас пустую строку если не указано конкретное значение символа пустой строки в метаинформации файла в ключе EMPTY_STRING_SYMBOL
                                newValue = new StringValue("");
                            } else {
                                newValue = new StringValue(fieldValues[i]);
                            }
                        }
                    } else {
                        throw new FatalException("Fileld " + fieldName + " not found in type " + typeName);
                    }

                    //Сравниваем изменения. Если значение поменялось, тогда пишем в доменный объект
                    if ((oldValue != null && !oldValue.equals(newValue)) || (oldValue == null && newValue != null)) {
                        domainObject.setValue(fieldName, newValue);
                    }
                }
            }
            if (login == null || domainObject.isNew()) {
                accessToken = accessService.createSystemAccessToken(this.getClass().getName());
            } else {
                accessToken =
                        accessService.createAccessToken(login, domainObject.getId(), DomainObjectAccessType.WRITE);
            }

            //Доменный объект сохраняем только если он изменился
            if (domainObject.isNew() || domainObject.isDirty()) {
                domainObject = domainObjectDao.save(domainObject, accessToken);
            }

            //Создаем вложения если необходимо
            if (attachments != null) {
                String[] attachementsList = attachments.split(",");
                for (int j = 0; j < attachementsList.length; j++) {
                    createAttachment(domainObject, attachementsList[j]);
                }
            }
        }
    }

    private boolean isEmptySimvol(String testString) {
        //Символ "_" строки означает у нас пустую строку если не указано конкретное значение символа пустой строки в метаинформации файла в ключе EMPTY_STRING_SYMBOL
        return (emptyStringSymbol == null && testString.equals("_")) || (emptyStringSymbol != null && testString.equals(emptyStringSymbol));
    }

    /**
     * Создание вложения для переданного доменного объекта
     * @param domainObject
     * @param string
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    private DomainObject createAttachment(DomainObject domainObject, String filePath) throws IOException,
            NoSuchAlgorithmException {
        DomainObject result = null;
        AccessToken accessToken = accessService.createSystemAccessToken(this.getClass().getName());
        //Получение типа доменного объекта вложения
        AttachmentTypeInfo attachmentTypeInfo = getAttachmentTypeInfo(typeName);

        //Проверка наличия вложения у типа
        if (attachmentTypeInfo != null) {

            //Получение имени вложения
            File attachmentFile = new File(filePath);

            //Получение вложения по имени
            DomainObject attachment = null;
            List<DomainObject> attachments = domainObjectDao.findLinkedDomainObjects(domainObject.getId(),
                    attachmentTypeInfo.attachmentTypeConfig.getName(), attachmentTypeInfo.refAttrName, accessToken);
            for (DomainObject existsAttachment : attachments) {
                if (existsAttachment.getString("Name").equals(attachmentFile.getName())) {
                    attachment = existsAttachment;
                }
            }

            //Если вложение не найдено то создание объекта вложения
            if (attachment == null) {
                attachment = createAttachmentDomainObjectFor(domainObject, attachmentTypeInfo);
                attachment.setString("Name", attachmentFile.getName());
            }

            //Сравниваем вложения и в базу вложения записываем только если оно изменилось
            if (attachment.isNew() || !contentEquals(filePath, attachment)) {

                //Установка контента вложения
                InputStream stream = this.getClass().getClassLoader().getResourceAsStream(filePath);
                long contentLength = getContentLength(filePath);

                result = saveAttachment(stream, attachment, contentLength);
            }
        }
        return result;
    }

    /**
     * Проверка существующего вложения на идентичность сохраняемому файлу
     * @param filePath
     * @param attachment
     * @return
     * @throws NoSuchAlgorithmException
     * @throws FileNotFoundException
     * @throws IOException
     */
    private boolean contentEquals(String filePath, DomainObject attachment) throws NoSuchAlgorithmException,
            FileNotFoundException, IOException {
        //Используем HASH MD5 для проверки идентичности вложений
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] buffer = new byte[1024];
        //Получаем HASH сохраняемого файла
        try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(filePath)) {
            DigestInputStream dis = new DigestInputStream(is, md);
            while (dis.read(buffer) != -1)
                ;
            dis.close();
        }
        byte[] fileDigest = md.digest();

        //Получаем HASH существующего вложения
        md.reset();
        try (InputStream is = attachmentContentDao.loadContent(attachment)) {
            DigestInputStream dis = new DigestInputStream(is, md);
            while (dis.read(buffer) != -1)
                ;
            dis.close();
        }
        byte[] attachmentDigest = md.digest();

        //Сравниваем HASH-и
        return Arrays.equals(fileDigest, attachmentDigest);
    }

    private AttachmentTypeInfo getAttachmentTypeInfo(String type) {
        AttachmentTypeInfo result = null;
        //Получение типа доменного объекта вложения
        DomainObjectTypeConfig typeConfig = configurationExplorer.getConfig(DomainObjectTypeConfig.class, type);

        //Проверка наличия вложения у типа
        if (typeConfig.getAttachmentTypesConfig() != null &&
                typeConfig.getAttachmentTypesConfig().getAttachmentTypeConfigs() != null &&
                typeConfig.getAttachmentTypesConfig().getAttachmentTypeConfigs().size() > 0) {
            result = new AttachmentTypeInfo();
            result.attachmentTypeConfig = typeConfig.getAttachmentTypesConfig().getAttachmentTypeConfigs().get(0);
            result.refAttrName = type;
        }

        //Если нет конфигурации вложения проверяем наличия супертипа
        if (result == null && typeConfig.getExtendsAttribute() != null) {
            //Если супертип есть, то проверяем наличие вложения у него
            result = getAttachmentTypeInfo(typeConfig.getExtendsAttribute());
        }
        return result;
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
        try {
            String type = null;
            String value = null;
            String fieldName = null;
            FieldConfig fieldConfig = null;
            FieldConfig refFieldConfig = configurationExplorer.getFieldConfig(typeName, refFieldName);
            String[] field = null;
            //Проверяем наличие точки до равно
            if (referenceValueAsString.matches("[_a-zA-Z1-9]+\\.[_a-zA-Z1-9]+=.+")) {
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

            String query = getQuery(type, new String[] { fieldName }, new String[] { value });
            return getReferenceFromSelect(query);
        } catch (Exception ex) {
            throw new FatalException("Error get reference from expression. FieldName=" + refFieldName + "; Value=" + referenceValueAsString, ex);
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
     * Получение ссылки с помощью запроса
     * @param query
     * @return
     */
    private Id getReferenceFromSelect(String query) {
        AccessToken accessToken = null;
        if (login == null) {
            accessToken = accessService.createSystemAccessToken(this.getClass().getName());
        } else {
            accessToken = accessService.createCollectionAccessToken(login);
        }
        IdentifiableObjectCollection collection = collectionsDao.findCollectionByQuery(query, 0, 1000, accessToken);
        Id result = null;
        if (collection.size() > 0) {
            result = collection.get(0).getId();
        }
        
        if (result == null){
            throw new FatalException("Not find value by query: " + query);
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
        if (login == null) {
            accessToken = accessService.createSystemAccessToken(this.getClass().getName());
        } else {
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
                        String conditionValue = null;
                        if (conditionValues[i] == null) {
                            conditionValue = " is null";
                        } else {
                            conditionValue = " = " + conditionValues[i];
                        }

                        if (where == null) {
                            where = " where t" + typeNo + ".\"" + fieldConfig.getName() + "\" " + conditionValue;
                        } else {
                            where += " and t" + typeNo + ".\"" + fieldConfig.getName() + "\" " + conditionValue;
                        }
                    }
                }
            }
            //Заполяем типы
            if (typeNo == 0) {
                from = " from \"" + config.getName() + "\" t" + typeNo + " ";
            } else {
                from +=
                        "inner join \"" + config.getName() + "\" t" + typeNo + " on (t" + typeNo + ".id = t"
                                + (typeNo - 1)
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
        String result = null;
        String valueAsString = fieldValues[fieldIndex.get(fieldName)];

        if (valueAsString.length() > 0) {
            //Получение конфигурации поля
            FieldConfig fieldConfig = configurationExplorer.getFieldConfig(typeName, fieldName);
            //Пока поддерживаем только строки и Long Вряд ли что то другое будет ключем
            if (fieldConfig.getFieldType() == FieldType.LONG) {
                result = valueAsString;
            } else if (fieldConfig.getFieldType() == FieldType.REFERENCE) {
                Id referenceValue = getReference(fieldName, valueAsString);
                if (referenceValue != null) {
                    result = String.valueOf(((RdbmsId) referenceValue).getId());
                }
            } else if (fieldConfig.getFieldType() == FieldType.BOOLEAN) {
                boolean boolValue = Boolean.parseBoolean(valueAsString);
                if (boolValue){
                    result = "1";
                }else{
                    result = "0";
                }
            } else {
                if (isEmptySimvol(valueAsString)) {
                    result = "''";
                } else {
                    valueAsString = valueAsString.replace("'", "''");
                    result = "'" + valueAsString + "'";
                }
            }
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

    private DomainObject createAttachmentDomainObjectFor(DomainObject objectId, AttachmentTypeInfo attachmentTypeInfo) {
        DomainObject attachmentDomainObject =
                (GenericDomainObject) createDomainObject(attachmentTypeInfo.attachmentTypeConfig.getName());

        attachmentDomainObject.setReference(attachmentTypeInfo.refAttrName, objectId);
        return attachmentDomainObject;
    }

    private DomainObject
            saveAttachment(InputStream inputStream, DomainObject attachmentDomainObject, long contentLength) {
        AccessToken accessToken = accessService.createSystemAccessToken(this.getClass().getName());

        StringValue newFilePathValue = null;
        DomainObject savedDoaminObject = null;
        try {
            String newFilePath = attachmentContentDao.saveContent(inputStream);
            //если newFilePath is null или empty не обрабатываем
            if (newFilePath == null || newFilePath.isEmpty()) {
                throw new DaoException("File isn't created");
            }
            newFilePathValue = new StringValue(newFilePath);
            StringValue oldFilePathValue = (StringValue) attachmentDomainObject.getValue("path");
            attachmentDomainObject.setValue(BaseAttachmentServiceImpl.PATH_NAME, new StringValue(newFilePath));

            attachmentDomainObject.setLong("ContentLength", contentLength);

            savedDoaminObject = domainObjectDao.save(attachmentDomainObject, accessToken);

            //предыдущий файл удаляем
            if (oldFilePathValue != null && !oldFilePathValue.isEmpty()) {
                //файл может быть и не удален, в случае если заблокирован
                attachmentDomainObject.setValue(BaseAttachmentServiceImpl.PATH_NAME, oldFilePathValue);
                attachmentContentDao.deleteContent(attachmentDomainObject);
            }
            savedDoaminObject.setValue("path", newFilePathValue);
            return savedDoaminObject;
        } catch (Exception ex) {
            if (newFilePathValue != null && !newFilePathValue.isEmpty()) {
                attachmentDomainObject.setValue(BaseAttachmentServiceImpl.PATH_NAME, newFilePathValue);
                attachmentContentDao.deleteContent(attachmentDomainObject);
            }
            throw new FatalException("Error save attachment", ex);
        }
    }

    private long getContentLength(String filePath) throws IOException {
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(filePath);
        long result = 0;
        long read = 0;
        byte[] buffer = new byte[1024];
        while ((read = inputStream.read(buffer)) > 0) {
            result += read;
        }
        return result;
    }

    private class AttachmentTypeInfo {
        private AttachmentTypeConfig attachmentTypeConfig;
        private String refAttrName;
    }

}
