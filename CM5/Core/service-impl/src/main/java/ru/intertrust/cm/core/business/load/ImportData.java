package ru.intertrust.cm.core.business.load;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.BaseAttachmentService;
import ru.intertrust.cm.core.business.api.ImportDataService;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.business.api.util.ThreadSafeDateFormat;
import ru.intertrust.cm.core.config.*;
import ru.intertrust.cm.core.config.doel.DoelExpression;
import ru.intertrust.cm.core.config.importcsv.BeforeImportConfig;
import ru.intertrust.cm.core.config.importcsv.DeleteAllConfig;
import ru.intertrust.cm.core.config.importcsv.ImportSettingsConfig;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.access.DomainObjectAccessType;
import ru.intertrust.cm.core.dao.api.*;
import ru.intertrust.cm.core.dao.dto.AttachmentInfo;
import ru.intertrust.cm.core.dao.exception.DaoException;
import ru.intertrust.cm.core.model.FatalException;

import javax.annotation.PostConstruct;
import javax.transaction.*;
import java.io.*;
import java.math.BigDecimal;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.*;

/**
 * Класс импортирования одного файла
 * @author larin
 * 
 */
public class ImportData {
    private static final Logger logger = Logger.getLogger(ImportData.class);
    private static final String TIMELESS_DATE_PATTERN = "dd.MM.yyyy";
    private static final String DATE_TIME_PATTERN = "dd.MM.yyyy HH:mm:ss";
    //Имя спринг бина для работы под системными правами
    public static final String SYSTEM_IMPORT_BEAN = "system-import-data";
    //Имя спринг бина для работы под пользовательскими правами
    public static final String PERSON_IMPORT_BEAN = "person-import-data";

    @Autowired
    private CollectionsDao collectionsDao;
    @Autowired
    private ConfigurationExplorer configurationExplorer;
    @Autowired
    private DomainObjectDao domainObjectDao;
    @Autowired
    private AttachmentContentDao attachmentContentDao;
    @Autowired
    private AccessControlService accessService;
    @Autowired
    private CurrentUserAccessor currentUserAccessor;
    @Autowired
    private DoelEvaluator doelEvaluator;
    @org.springframework.beans.factory.annotation.Value("${import.rows.in.one.transaction:1000}")
    private int rowsInOneTransaction;
    @org.springframework.beans.factory.annotation.Value("${import.transaction.timeout:1000}")
    private int transactionTimeout;

    private String typeName;
    private boolean deleteOther;
    private String[] keys;
    private String[] fields;
    private String login;
    private Hashtable<String, Integer> fieldIndex;
    private String emptyStringSymbol;
    private LinkedHashSet<Id> importedIds;
    private boolean systemPermission;
    private String attachmentBasePath;
    private boolean inTransaction;
    private Set<String> serviceFields;

    /**
     * Конструктор. Инициалитзирует класс флагом с какими правами должен
     * работать сервис
     * @param systemPermission
     *            в случае если параметр равен true импорт производить под
     *            системными правами, иначе под правами текущего пользователя
     */
    public ImportData(boolean systemPermission) {
        this.systemPermission = systemPermission;
    }

    @PostConstruct
    public void init() {
        if (!systemPermission) {
            login = currentUserAccessor.getCurrentUser();
        }
    }

    /**
     * Загрузка одного файла с данными
     * @param loadFileAsByteArray
     */
    public List<Id> importData(byte[] loadFileAsByteArray, String encoding, Boolean rewrite, String attachmentBasePath, UserTransaction transaction) {
        Reader reader = null;
        this.attachmentBasePath = attachmentBasePath;

        try {
            ByteArrayInputStream input = new ByteArrayInputStream(loadFileAsByteArray);
            reader = new InputStreamReader(input, encoding != null ? encoding : ImportDataService.DEFAULT_ENCODING);

            Iterable<CSVRecord> records = CSVFormat.EXCEL.withDelimiter(';').parse(reader);
            int lineNum = 0;

            //имя типа доменного объекта который будет создаваться
            typeName = null;
            //Ключевые поля, которые используются для поиска импортируемой записи при обновление, и для исключения дублирования при создание
            keys = null;
            //Имена полей
            fields = null;
            //Символ означающий пустую строку
            emptyStringSymbol = null;
            //Удаление всех записей данного типа, которые отсутствуют в импортируемом файле
            deleteOther = false;
            //Список импортированных записей
            importedIds = new LinkedHashSet<>();
            //Служебные поля, которые не надо проверять на наличие данного поля в конфигурации. Инерпритировать как строки
            serviceFields = new HashSet<String>();
            

            logger.info("Import data from csv by " + rowsInOneTransaction + " rows in transactions.");
            //итератор по строкам
            for (CSVRecord record : records) {
                if (!isEmptyRow(record)) {
                    //CMFIVE-2116 В случае если в одном файле импортируются данные разных типов то сбрасываем счетчик строк в 0 и обнуляем всю метаинформацию
                    if (lineNum > 0 && record.size() > 0 && record.get(0).trim().toUpperCase().startsWith(ImportDataService.TYPE_NAME + "=")) {
                        commitTransaction(transaction);

                        deleteOther(transaction);

                        lineNum = 0;
                        typeName = null;
                        keys = null;
                        fields = null;
                        emptyStringSymbol = null;
                        deleteOther = false;                        
                    }

                    //Первые две строки это метаданные
                    if (lineNum == 0) {
                        //Метаданные
                        for (String metaData : record) {
                            String normalMetaData = metaData.trim();
                            String[] metaItem = normalMetaData.split("=");
                            if (metaItem[0].equalsIgnoreCase(ImportDataService.TYPE_NAME)) {
                                typeName = metaItem[1];
                            } else if (metaItem[0].equalsIgnoreCase(ImportDataService.KEYS)) {
                                keys = metaItem[1].split(",");
                            } else if (metaItem[0].equalsIgnoreCase(ImportDataService.EMPTY_STRING_SYMBOL)) {
                                emptyStringSymbol = metaItem[1];
                            } else if (metaItem[0].equalsIgnoreCase(ImportDataService.DELETE_OTHER)) {
                                deleteOther = Boolean.parseBoolean(metaItem[1]);
                            } else if (metaItem[0].equalsIgnoreCase(ImportDataService.SERVICE_FIELDS)) {
                                String[] serviceFieldArr = metaItem[1].split(",");
                                for (String serviceField : serviceFieldArr) {
                                    serviceFields.add(serviceField);
                                }
                            }
                            
                        }
                    } else if (lineNum == 1) {
                        //Имена полей
                        List<String> fieldList = new ArrayList<String>();
                        for (int i = 0; i < record.size(); i++) {
                            if (record.get(i).trim().length() > 0) {
                                fieldList.add(record.get(i).trim());
                            }
                            fields = fieldList.toArray(new String[fieldList.size()]);
                        }
                        //Строим таблицу соответствия имени поля и его индекса, для оптимизации
                        fieldIndex = new Hashtable<String, Integer>();
                        for (int i = 0; i < fields.length; i++) {
                            fieldIndex.put(fields[i], i);
                        }
                    } else {
                        beginTransactionIfNeed(transaction, lineNum-2);
                        //Импорт одной строки
                        Id importedId = importLine(csvRecordToArray(record), rewrite);
                        importedIds.add(importedId);
                        commitTransactionIfNeed(transaction, lineNum-2);
                    }

                    lineNum++;
                }
            }
            commitTransaction(transaction);
            deleteOther(transaction);
            return new ArrayList<>(importedIds);
        } catch (Exception ex) {
            try {
                if (transaction != null && transaction.getStatus() == javax.transaction.Status.STATUS_ACTIVE){
                    transaction.rollback();
                }
            } catch (Exception ignoreEx) {
            }
            throw new FatalException("Error load data. TypeName=" + typeName, ex);
        } finally {
            try {
                reader.close();                
            } catch (Exception ignoreEx) {
            }
        }
    }

    private void commitTransactionIfNeed(UserTransaction transaction, int lineNum) throws SecurityException, IllegalStateException, SystemException, RollbackException, HeuristicMixedException, HeuristicRollbackException {
        if (transaction != null && inTransaction){
            if (((lineNum+1) % rowsInOneTransaction) == 0){
                if (transaction.getStatus() == javax.transaction.Status.STATUS_ACTIVE){
                    transaction.commit();
                    logger.info("Commit transaction on block " + (lineNum / rowsInOneTransaction));
                    inTransaction = false;
                }else{
                    throw new FatalException("Transaction can not committed. Transaction status " + transaction.getStatus());
                }
            }
        }
    }

    private void commitTransaction(UserTransaction transaction) throws SecurityException, IllegalStateException, SystemException, RollbackException, HeuristicMixedException, HeuristicRollbackException {
        if (transaction != null && inTransaction){
            if (transaction.getStatus() == javax.transaction.Status.STATUS_ACTIVE){
                transaction.commit();
                logger.info("Commit transaction on file or partition");
                inTransaction = false;
            }else{
                throw new FatalException("Transaction can not committed. Transaction status " + transaction.getStatus());
            }
        }
    }

    private void beginTransactionIfNeed(UserTransaction transaction, int lineNum) throws SystemException, NotSupportedException {
        if (transaction != null){
            if ((lineNum % rowsInOneTransaction) == 0 
                    && transaction.getStatus() == javax.transaction.Status.STATUS_NO_TRANSACTION){
                transaction.setTransactionTimeout(transactionTimeout);
                transaction.begin();
                logger.info("Begin transaction on block " + (lineNum / rowsInOneTransaction));
                inTransaction = true;
            }
        }
    }

    /**
     * Выполняем действия описанные в конфигурации импорта
     */
    private void doBeforeImportRow(Id importedId) {
        Collection<ImportSettingsConfig> importSettings = configurationExplorer.getConfigs(ImportSettingsConfig.class);
        for (ImportSettingsConfig importSettingsConfig : importSettings) {
            if (importSettingsConfig.getBeforeImport() != null) {
                for (BeforeImportConfig beforeImportConfig : importSettingsConfig.getBeforeImport()) {
                    //Проверка на соответствие типа
                    if (beforeImportConfig.getImportType().equalsIgnoreCase(typeName)) {
                        //Выполняем удаление дочерних типов
                        deleteAll(importedId, beforeImportConfig.getDeleteAll());
                    }
                }
            }
        }
    }

    /**
     * Удаление дочерних типов, настроенных в конфигурации импорта
     * @param deleteAll
     */
    private void deleteAll(Id importedId, List<DeleteAllConfig> deleteAll) {
        if (importedId != null && deleteAll != null) {
            for (DeleteAllConfig deleteAllConfig : deleteAll) {
                DoelExpression expression = DoelExpression.parse(deleteAllConfig.getDoel());
                List<Value> domainObjectsToDelete = doelEvaluator.evaluate(expression, importedId, getSelectAccessToken());
                for (Value id : domainObjectsToDelete) {
                    Id deleteId = (Id) id.get();
                    domainObjectDao.delete(deleteId, getDeleteAccessToken(deleteId));
                }
            }
        }
    }

    /**
     * Удаление всех записей импортируемого типа, если данные записи не
     * упоминаются в csv файле
     * @throws NotSupportedException 
     * @throws SystemException 
     * @throws HeuristicRollbackException 
     * @throws HeuristicMixedException 
     * @throws RollbackException 
     * @throws IllegalStateException 
     * @throws SecurityException 
     */
    private void deleteOther(UserTransaction transaction) throws SystemException, NotSupportedException, SecurityException, IllegalStateException, RollbackException, HeuristicMixedException, HeuristicRollbackException {
        //Проверка флага удалить лишнее в заголовке
        if (deleteOther) {
            //Поиск объектов в базе
            List<Id> toDeleteIds = new ArrayList<Id>();
            String query = "select id from " + typeName;
            IdentifiableObjectCollection collection = collectionsDao.findCollectionByQuery(query, 0, 0, getSelectAccessToken());
            for (IdentifiableObject identifiableObject : collection) {
                if (!importedIds.contains(identifiableObject.getId())) {
                    toDeleteIds.add(identifiableObject.getId());
                }
            }

            //Удаление лишних записей
            if (toDeleteIds.size() > 0) {
                int rows = 0;
                for (Id id : toDeleteIds) {
                    beginTransactionIfNeed(transaction, rows);
                    domainObjectDao.delete(id, getDeleteAccessToken(id));
                    commitTransactionIfNeed(transaction, rows);
                    rows++;
                }
            }
        }
    }

    /**
     * Проверка на то, что строка содержит только пустые значения
     * @param record
     * @return
     */
    private boolean isEmptyRow(CSVRecord record) {
        boolean result = true;
        for (String item : record) {
            if (!item.isEmpty()) {
                result = false;
                break;
            }
        }
        return result;
    }

    private String[] csvRecordToArray(CSVRecord record) {
        String[] result = new String[record.size()];
        for (int i = 0; i < record.size(); i++) {
            result[i] = record.get(i);
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
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    private Id importLine(String[] line, Boolean rewrite) throws ParseException, IOException,
            NoSuchAlgorithmException {
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

        //Выполяем действия перед импортом
        doBeforeImportRow(domainObject.getId());

        //Если доменный объект новый или стоит флаг перезаписывать атрибуты то устанавливаем атрибуты
        if (rewrite || domainObject.isNew()) {

            String attachments = null;
            //Установка полей
            for (int i = 0; i < fields.length; i++) {
                String fieldName = fields[i];

                //Обрабатываем ключевое поле вложения
                if (ImportDataService.ATTACHMENT_FIELD_NAME.equals(fieldName)) {
                    if (!fieldValues[i].isEmpty()) {
                        attachments = fieldValues[i];
                    }
                } else {
                    //Запоминаем старое значение
                    Value oldValue = domainObject.getValue(fieldName);
                    Value newValue = getFieldValue(fieldName, fieldValues[i]);
                    //Сравниваем изменения. Если значение поменялось, тогда пишем в доменный объект
                    if ((oldValue != null && !oldValue.equals(newValue)) || (oldValue == null && newValue != null)) {
                        domainObject.setValue(fieldName, newValue);
                    }
                }
            }

            //Доменный объект сохраняем только если он изменился
            if (domainObject.isNew() || domainObject.isDirty()) {
                domainObject = domainObjectDao.save(domainObject, getWriteAccessToken(domainObject));
            }

            //Создаем вложения если необходимо
            if (attachments != null) {
                String[] attachementsList = attachments.split(",");
                for (int j = 0; j < attachementsList.length; j++) {
                    createAttachment(domainObject, attachementsList[j]);
                }
            }
        }
        return domainObject.getId();
    }

    private Value getFieldValue(String fieldName, String fieldValue) throws ParseException {
        Value newValue = null;
        FieldConfig fieldConfig = configurationExplorer.getFieldConfig(typeName, fieldName);
        if (fieldConfig != null) {
            if (fieldConfig.getFieldType() == FieldType.BOOLEAN) {
                if (fieldValue.length() != 0) {
                    newValue = new BooleanValue(Boolean.valueOf(fieldValue));
                }
            } else if (fieldConfig.getFieldType() == FieldType.DATETIME) {
                if (fieldValue.length() != 0) {
                    newValue = new DateTimeValue(ThreadSafeDateFormat.parse(fieldValue, DATE_TIME_PATTERN));
                }
            } else if (fieldConfig.getFieldType() == FieldType.DATETIMEWITHTIMEZONE) {
                if (fieldValue.length() != 0) {
                    Date date = ThreadSafeDateFormat.parse(fieldValue, DATE_TIME_PATTERN);
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(date);
                    DateTimeWithTimeZone dateTimeWithTimeZone = new DateTimeWithTimeZone(TimeZone.getDefault().getID(),
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH),
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            calendar.get(Calendar.SECOND), 0
                            );
                    newValue = new DateTimeWithTimeZoneValue(dateTimeWithTimeZone);
                }
            } else if (fieldConfig.getFieldType() == FieldType.DECIMAL) {
                if (fieldValue.length() != 0) {
                    newValue = new DecimalValue(new BigDecimal(fieldValue));
                }
            } else if (fieldConfig.getFieldType() == FieldType.LONG) {
                if (fieldValue.length() != 0) {
                    newValue = new LongValue(Long.parseLong(fieldValue));
                }
            } else if (fieldConfig.getFieldType() == FieldType.REFERENCE) {
                //Здесь будут выражения в формате type.field="Значение поля" или field="Значение поля" или запрос
                newValue = new ReferenceValue(getReference(fieldName, fieldValue));
            } else if (fieldConfig.getFieldType() == FieldType.TIMELESSDATE) {
                if (fieldValue.length() != 0) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(ThreadSafeDateFormat.parse(fieldValue, TIMELESS_DATE_PATTERN));
                    newValue = new TimelessDateValue(new TimelessDate(
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    ));
                }
            } else {
                //В остальных случаях считаем строкой
                if (fieldValue.length() == 0) {
                    newValue = null;
                } else if (isEmptySimvol(fieldValue)) {
                    //Символ "_" строки означает у нас пустую строку если не указано конкретное значение символа пустой строки в метаинформации файла в ключе EMPTY_STRING_SYMBOL
                    newValue = new StringValue("");
                } else {
                    //Если тип string проверяем длину строки
                    if (fieldConfig.getFieldType() == FieldType.STRING) {
                        int maxLen = ((StringFieldConfig)fieldConfig).getLength();
                        if (fieldValue.length() > maxLen){
                            logger.warn("String value " + fieldValue + " of field " + fieldName + " is truncate to " + maxLen + " simvols");
                            String truncateStringValue = fieldValue.substring(0, maxLen-1);                            
                            newValue = new StringValue(truncateStringValue);
                        }else{
                            newValue = new StringValue(fieldValue);
                        }
                    }else{
                        newValue = new StringValue(fieldValue);
                    }
                }
            }
        } else {
            //В случае со служебным полем формируем строковое значение, иначе выбрасываем исключение
            if (serviceFields.contains(fieldName)){
                newValue = new StringValue(fieldValue);
            }else{
                throw new FatalException("Field " + fieldName + " not found in type " + typeName);
            }
        }

        return newValue;
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
    private DomainObject createAttachment(DomainObject domainObject, String fileInfo) throws IOException,
            NoSuchAlgorithmException {
        DomainObject result = null;

        //Получение типа вложения путь к ресурсу из csv, если тип не задан возмется первый тип указанный в конфигурации
        String attachmentType = null;
        String filePath = null;
        if (fileInfo.indexOf(":") > 0) {
            String[] fileInfoArray = fileInfo.split(":");
            attachmentType = fileInfoArray[0];
            filePath = fileInfoArray[1];
        } else {
            filePath = fileInfo;
        }

        //Получение описания типа доменного объекта вложения
        AttachmentTypeInfo attachmentTypeInfo = getAttachmentTypeInfo(typeName, attachmentType);

        //Проверка наличия вложения у типа
        if (attachmentTypeInfo != null) {

            //Получение имени вложения
            File attachmentFile = new File(filePath);

            //Получение вложения по имени
            DomainObject attachment = null;
            List<DomainObject> attachments = domainObjectDao.findLinkedDomainObjects(domainObject.getId(),
                    attachmentTypeInfo.attachmentTypeConfig.getName(), attachmentTypeInfo.refAttrName, getSelectAccessToken());
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
                //TODO Delete
                //long contentLength = getContentLength(filePath);
                try (InputStream stream = getAttachmentStream(filePath)) {
                    result = saveAttachment(stream, attachment, domainObject);
                }
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
        try (InputStream is = getAttachmentStream(filePath)) {
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

    private AttachmentTypeInfo getAttachmentTypeInfo(String type, String attachmentType) {
        AttachmentTypeInfo result = null;
        //Получение типа доменного объекта вложения
        DomainObjectTypeConfig typeConfig = configurationExplorer.getConfig(DomainObjectTypeConfig.class, type);

        //Проверка наличия вложения у типа
        if (typeConfig.getAttachmentTypesConfig() != null &&
                typeConfig.getAttachmentTypesConfig().getAttachmentTypeConfigs() != null &&
                typeConfig.getAttachmentTypesConfig().getAttachmentTypeConfigs().size() > 0) {
            if (attachmentType == null) {
                result = new AttachmentTypeInfo();
                result.attachmentTypeConfig = typeConfig.getAttachmentTypesConfig().getAttachmentTypeConfigs().get(0);
                result.refAttrName = type;
            } else {
                for (AttachmentTypeConfig attachmentTypeConfig : typeConfig.getAttachmentTypesConfig().getAttachmentTypeConfigs()) {
                    if (attachmentTypeConfig.getName().equalsIgnoreCase(attachmentType)) {
                        result = new AttachmentTypeInfo();
                        result.attachmentTypeConfig = attachmentTypeConfig;
                        result.refAttrName = type;
                    }
                }
            }
        }

        //Если нет конфигурации вложения проверяем наличия супертипа
        if (result == null && typeConfig.getExtendsAttribute() != null) {
            //Если супертип есть, то проверяем наличие вложения у него
            result = getAttachmentTypeInfo(typeConfig.getExtendsAttribute(), attachmentType);
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
                result = getReferenceFromSelect(referenceValueAsString, new ArrayList<Value>());
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

            List<Value> values = new ArrayList<Value>();
            if (fieldConfig != null && fieldConfig.getFieldType() == FieldType.LONG) {
                values.add(new LongValue(Long.parseLong(getNormalizationField(field[1]))));
            } else {
                values.add(new StringValue(getNormalizationField(field[1])));
            }

            String query = getQuery(type, new String[] { fieldName }, values);
            return getReferenceFromSelect(query, values);
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
    private Id getReferenceFromSelect(String query, List<Value> params) {
        IdentifiableObjectCollection collection = collectionsDao.findCollectionByQuery(query, params, 0, 0, getSelectAccessToken());
        Id result = null;
        if (collection.size() > 0) {
            result = collection.get(0).getId();
        }

        if (result == null) {
            throw new FatalException("Not find value by query: " + query);
        }
        return result;
    }

    /**
     * Поиск доменного объекта
     * @param fieldValues
     * @return
     * @throws ParseException
     */
    private DomainObject findDomainObject(String[] fieldValues) throws ParseException {
        DomainObject result = null;
        if (keys != null){
            List<Value> values = getPlatformFieldValues(fieldValues);
    
            String query = getQuery(typeName, keys, values);
    
            IdentifiableObjectCollection collection = collectionsDao.findCollectionByQuery(query, values, 0, 0, getSelectAccessToken());
            
            if (collection.size() > 0) {
                result = domainObjectDao.find(collection.get(0).getId(), getReadAccessToken(collection.get(0).getId()));
            }
        }
        return result;
    }

    /**
     * Формирование запроса с учетом иерархии типов
     * @param typeName
     * @param conditionFields
     * @param conditionValues
     * @return
     */
    private String getQuery(String typeName, String[] conditionFields, List<Value> conditionValues) {
        //Получение конфигурации типа
        DomainObjectTypeConfig config = configurationExplorer.getConfig(DomainObjectTypeConfig.class, typeName);
        String result = null;
        int typeNo = 0;
        result = "select t0.id ";
        String from = null;
        String where = null;
        int paramIndex = 0;
        List<Value> workConditionsValues = new ArrayList<Value>();
        //Формируем запрос
        do {
            //Заполняем where
            for (FieldConfig fieldConfig : config.getFieldConfigs()) {
                for (int i = 0; i < conditionFields.length; i++) {
                    if (conditionFields[i].equalsIgnoreCase(fieldConfig.getName())) {
                        String conditionValue = null;
                        if (conditionValues.get(i) == null) {
                            conditionValue = " is null";
                        } else {
                            conditionValue = " = {" + paramIndex++ + "}";
                            workConditionsValues.add(conditionValues.get(i));
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

        //Подменяем conditionValues на workConditionsValues
        conditionValues.clear();
        for (int i = 0; i < workConditionsValues.size(); i++) {
            conditionValues.add(workConditionsValues.get(i));
        }

        return result;
    }

    /**
     * Получение значение поля, для подстановки в запрос, с учетом типа поля
     * @param fieldValues
     * @param fieldName
     * @return
     * @throws ParseException
     */
    private List<Value> getPlatformFieldValues(String[] fieldValues) throws ParseException {
        List<Value> result = new ArrayList<Value>();

        for (int i = 0; i < keys.length; i++) {
            String fieldName = keys[i];

            //Получение значения поля
            String fieldValue = fieldValues[fieldIndex.get(fieldName)];

            if (fieldValue.length() > 0) {
                result.add(getFieldValue(fieldName, fieldValue));
            } else {
                result.add(null);
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

    private DomainObject saveAttachment(InputStream inputStream, DomainObject attachmentDomainObject, DomainObject parentObject) {

        try {
            String fileName = attachmentDomainObject.getString(BaseAttachmentService.NAME);
            AttachmentInfo attachmentInfo = attachmentContentDao.saveContent(inputStream, parentObject, attachmentDomainObject.getTypeName(), fileName);
            String newFilePath = attachmentInfo.getRelativePath();
            if (newFilePath == null || newFilePath.isEmpty()) {
                throw new DaoException("File isn't created");
            }
            attachmentDomainObject.setValue(BaseAttachmentService.PATH, new StringValue(newFilePath));

            attachmentDomainObject.setLong(BaseAttachmentService.CONTENT_LENGTH, attachmentInfo.getContentLength());
            attachmentDomainObject.setString(BaseAttachmentService.MIME_TYPE, attachmentInfo.getMimeType());
            
            return domainObjectDao.save(attachmentDomainObject, getWriteAccessToken(attachmentDomainObject));
        } catch (Exception ex) {
            throw new FatalException("Error save attachment", ex);
        }
    }

    private long getContentLength(String filePath) throws IOException {
        try (InputStream inputStream = getAttachmentStream(filePath)) {
            long result = 0;
            long read = 0;
            byte[] buffer = new byte[1024];
            while ((read = inputStream.read(buffer)) > 0) {
                result += read;
            }
            return result;
        }
    }

    private InputStream getAttachmentStream(String filePath) throws FileNotFoundException {
        InputStream inputStream = null;
        if (attachmentBasePath == null) {
            inputStream = this.getClass().getClassLoader().getResourceAsStream(filePath);
        } else {
            inputStream = new FileInputStream(attachmentBasePath + "/" + filePath);
            // DBG import data
            // inputStream = this.getClass().getClassLoader().getResourceAsStream(attachmentBasePath + "/" + filePath);
        }
        return inputStream;
    }

    private class AttachmentTypeInfo {
        private AttachmentTypeConfig attachmentTypeConfig;
        private String refAttrName;
    }

    private AccessToken getSelectAccessToken() {
        AccessToken result = null;
        if (systemPermission) {
            result = accessService.createSystemAccessToken(this.getClass().getName());
        } else {
            result = accessService.createCollectionAccessToken(login);
        }
        return result;
    }

    private AccessToken getDeleteAccessToken(Id domainObjectId) {
        AccessToken result = null;
        if (systemPermission) {
            result = accessService.createSystemAccessToken(this.getClass().getName());
        } else {
            result = accessService.createAccessToken(login, domainObjectId, DomainObjectAccessType.DELETE);
        }
        return result;
    }

    private AccessToken getWriteAccessToken(DomainObject domainObject) {
        AccessToken result = null;
        if (systemPermission) {
            result = accessService.createSystemAccessToken(this.getClass().getName());
        } else {
            if (domainObject.isNew()) {
                result = accessService.createDomainObjectCreateToken(login, domainObject);
            } else {
                result = accessService.createAccessToken(login, domainObject.getId(), DomainObjectAccessType.WRITE);
            }
        }
        return result;
    }

    private AccessToken getReadAccessToken(Id domainObjectId) {
        AccessToken result = null;
        if (systemPermission) {
            result = accessService.createSystemAccessToken(this.getClass().getName());
        } else {
            result = accessService.createAccessToken(login, domainObjectId, DomainObjectAccessType.READ);
        }
        return result;
    }

}
