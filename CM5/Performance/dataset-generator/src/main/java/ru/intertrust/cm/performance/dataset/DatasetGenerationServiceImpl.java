package ru.intertrust.cm.performance.dataset;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.transaction.UserTransaction;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.Duration;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.transform.RegistryMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.xml.sax.SAXException;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.DateTimeFieldConfig;
import ru.intertrust.cm.core.config.DecimalFieldConfig;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.config.LongFieldConfig;
import ru.intertrust.cm.core.config.ReferenceFieldConfig;
import ru.intertrust.cm.core.config.StringFieldConfig;
import ru.intertrust.cm.core.config.UniqueKeyConfig;
import ru.intertrust.cm.core.config.UniqueKeyFieldConfig;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.util.SpringApplicationContext;
import ru.intertrust.cm.performance.dataset.generatefields.GenerateChildrenField;
import ru.intertrust.cm.performance.dataset.generatefields.GenerateDateTimeField;
import ru.intertrust.cm.performance.dataset.generatefields.GenerateDecimalField;
import ru.intertrust.cm.performance.dataset.generatefields.GenerateLongField;
import ru.intertrust.cm.performance.dataset.generatefields.GenerateReferenceField;
import ru.intertrust.cm.performance.dataset.generatefields.GenerateStringField;
import ru.intertrust.cm.performance.dataset.jaxb.adapters.DateTimeAdapter;
import ru.intertrust.cm.performance.dataset.jaxb.adapters.DurationAdapter;
import ru.intertrust.cm.performance.dataset.jaxb.adapters.NonNegativeIntegerAdapter;
import ru.intertrust.cm.performance.dataset.xmltypes.ChildrenType;
import ru.intertrust.cm.performance.dataset.xmltypes.DateTimeType;
import ru.intertrust.cm.performance.dataset.xmltypes.DecimalType;
import ru.intertrust.cm.performance.dataset.xmltypes.FieldType;
import ru.intertrust.cm.performance.dataset.xmltypes.LongType;
import ru.intertrust.cm.performance.dataset.xmltypes.ObjectType;
import ru.intertrust.cm.performance.dataset.xmltypes.QueryType;
import ru.intertrust.cm.performance.dataset.xmltypes.ReferenceType;
import ru.intertrust.cm.performance.dataset.xmltypes.SetType;
import ru.intertrust.cm.performance.dataset.xmltypes.StringType;
import ru.intertrust.cm.performance.dataset.xmltypes.TemplateType;

/**
 * Сервис генерации данных для тестового наполнения
 *
 * @author erentsov
 *
 */
@Local(DatasetGenerationService.class)
@Remote(DatasetGenerationService.Remote.class)
@TransactionManagement(TransactionManagementType.BEAN)
@Stateless
public class DatasetGenerationServiceImpl implements DatasetGenerationService, DatasetGenerationService.Remote {

    private final StringBuffer response = new StringBuffer();
    private Persister persister;
    private Validator validator;
    private RandomGenerators generators;
    private Date currentDate;

    // @Autowired
    // private CollectionsService collectionsService;

    @Autowired
    private DomainObjectDao domainObjectDao;

    @Autowired
    private AccessControlService accessControlService;

    // @Autowired
    // private DomainObject domainObject;

    @Autowired
    ConfigurationExplorer configurationExplorer;

    @Resource
    private SessionContext sessionContext;

    @PostConstruct
    public void init() throws JAXBException, SAXException, IOException {
        // TODO Эти строчки можно будет удалить после того, как будет реализован
        // autowire для удаленных ejb.
        AutowiredAnnotationBeanPostProcessor bpp = new AutowiredAnnotationBeanPostProcessor();
        bpp.setBeanFactory(SpringApplicationContext.getContext().getAutowireCapableBeanFactory());
        bpp.processInjection(this);
        // --------------------------------------------------------------------------------
        StreamSource schemaSource = new StreamSource(getClass().getResourceAsStream(
                "/dataset-generator-configuration.xsd"));
        SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
        /*factory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");*/

        Schema schema = factory.newSchema(schemaSource);
        validator = schema.newValidator();
        generators = RandomGenerators.getInstance();

        RegistryMatcher matcher = new RegistryMatcher();
        matcher.bind(Date.class, DateTimeAdapter.class);
        matcher.bind(Duration.class, DurationAdapter.class);
        matcher.bind(Integer.class, NonNegativeIntegerAdapter.class);
        persister = new Persister(matcher);

    }

    @Override
    public String execute(byte[] command) {
        try {
            currentDate = new Date();
            response.setLength(0);
            validator.validate(new StreamSource(new ByteArrayInputStream(command)));
            QueryType query = persister.read(QueryType.class, new ByteArrayInputStream(command));
            validateQuery(query);
            processQuery(query);

            response.append("Start filling database..." + '\n');
            System.out.println("Start filling database...");
            long timeout = System.currentTimeMillis();
            fillProcessQuery(query);
            timeout = System.currentTimeMillis() - timeout;
            response.append("Finish filling database. Time filling: " + timeout + " miliseconds" + '\n');
            System.out.println("Finish filling database.");
        } catch (Exception e) {
            // e.printStackTrace();
            response.append(e.getMessage());
        }
        return response.toString();
    }

    private void validateQuery(QueryType query) {

    }

    private void processQuery(QueryType query) {

    }

    private void fillProcessQuery(QueryType query) throws IOException, Exception {

        UserTransaction utx = sessionContext.getUserTransaction();

        // получим множество объектов Template из xml
        List<TemplateType> templateList = query.getTemplate();
        // получим множество объектов Set из xml
        List<SetType> setList = query.getSet();
        for (SetType setType : setList) {
            // Создаем множество для хранения объектов
            List<DomainObject> domainObjects = new ArrayList<DomainObject>();

            ObjectType objectType = setType.getObject();
            // объект set обработан не доконца. не реализованно математическое
            // ожидание
            int count = 0;
            if (setType.getQuantity() != null) {
                count = setType.getQuantity();
            } else {
                // Инициализируем генератор
                Random rnd = new Random(System.currentTimeMillis());
                // Получаем случайное число в диапазоне от min до max
                // (включительно)
                count = setType.getMinQuantity() + rnd.nextInt(setType.getMaxQuantity() - setType.getMinQuantity() + 1);
            }
            // System.out.println("adding main domain objects in list");
            System.out.println("Create main domain objects");
            // в цикле создаем доменные объекты по инструкции
            for (Long i = 0L; i < count; i++) {
                // открываем транзакцию
                try {
                    utx.begin();

                    // запускаем метод создания доменных объектов, а его
                    // результат добавляем в множество объектов
                    DomainObject newDomainObject = generateObject(templateList, objectType, null);

                    newDomainObject = addOfMissingFields(newDomainObject, null);

                    // сохраняем экземпляр доменного объекта для возможности
                    // получения его идентификатора
                    AccessToken accessToken = accessControlService.createSystemAccessToken("DatasetGenerationService");
                    newDomainObject = domainObjectDao.save(newDomainObject, accessToken);

                    // инициализируем класс анализа и создания доменных объектов
                    // из поля children
                    GenerateChildrenField generateChildrenFiled = new GenerateChildrenField();
                    // запускаем анализ поля children
                    generateChildrenFiled.generateField(newDomainObject, templateList, objectType, this);

                    // закрываем транзакцию
                    utx.commit();

                } catch (Exception e) {
                    e.printStackTrace();
                    utx.rollback();
                    throw new Exception(e);
                }
            }
            System.out.println("Finish create domain object in database.");
        }
    }

    public boolean getUniqueInfo(String nameDomainType, String fieldName) {

        DomainObjectTypeConfig domainObjectTypeConfig = configurationExplorer.getConfig(DomainObjectTypeConfig.class,
                nameDomainType);

        List<UniqueKeyConfig> uniqueKeyConfigList = domainObjectTypeConfig.getUniqueKeyConfigs();

        for (UniqueKeyConfig uniqueKeyConfig : uniqueKeyConfigList) {
            for (UniqueKeyFieldConfig uniqueKeyFieldConfig : uniqueKeyConfig.getUniqueKeyFieldConfigs()) {
                if (uniqueKeyFieldConfig.getName().equals(fieldName)) {
                    return true;
                }
            }
        }
        return false;
    }

    public DomainObject addOfMissingFields(DomainObject domainObject, DomainObject parentDomainObject)
            throws IOException, NoSuchAlgorithmException {
        DomainObjectTypeConfig domainObjectTypeConfig = configurationExplorer.getConfig(DomainObjectTypeConfig.class,
                domainObject.getTypeName());

        // получим список конфигураций полей
        List<FieldConfig> fieldConfigList = domainObjectTypeConfig.getFieldConfigs();

        for (FieldConfig fieldConfig : fieldConfigList) {
            // получим имя поля из конфигурации
            String fieldNameFromConfig = fieldConfig.getName();
            // проверим наличие этого поля в объекте
            Object field = domainObject.getValue(fieldNameFromConfig);

            if (field == null) {
                ru.intertrust.cm.core.business.api.dto.FieldType fieldType = fieldConfig.getFieldType();

                if (fieldType.equals(ru.intertrust.cm.core.business.api.dto.FieldType.STRING)) {
                    // преобразуем поле конфигурации к строковому типу
                    StringFieldConfig stringFieldConfig = (StringFieldConfig) fieldConfig;
                    // сгененрируем поле
                    GenerateStringField generateStringField = new GenerateStringField();
                    generateStringField.generateField(stringFieldConfig.getName(), stringFieldConfig.getLength());
                    domainObject.setString(generateStringField.getField(), generateStringField.getValue());

                } else if (fieldType.equals(ru.intertrust.cm.core.business.api.dto.FieldType.DATETIME)) {
                    // преобразуем поле конфиграции к типу дата время
                    DateTimeFieldConfig dateTimeFieldConfig = (DateTimeFieldConfig) fieldConfig;
                    // сгененрируем поле
                    GenerateDateTimeField generateDateTimeField = new GenerateDateTimeField();
                    generateDateTimeField.generateField(dateTimeFieldConfig.getName());
                    domainObject.setTimestamp(generateDateTimeField.getField(), generateDateTimeField.getValue());

                } else if (fieldType.equals(ru.intertrust.cm.core.business.api.dto.FieldType.REFERENCE)) {
                    // преобразуем поле конфиграции к типу ссылка
                    ReferenceFieldConfig referenceFieldConfig = (ReferenceFieldConfig) fieldConfig;

                    String referenceTypeName = referenceFieldConfig.getType();

                    if (parentDomainObject != null) {
                        if (parentDomainObject.getTypeName().equals(referenceTypeName)) {
                            domainObject.setReference(referenceFieldConfig.getName(), parentDomainObject.getId());
                        }else{
                            // сгененрируем поле
                            GenerateReferenceField generateReferenceField = new GenerateReferenceField();
                            generateReferenceField.generateField(referenceFieldConfig.getName(), referenceTypeName, this);
                            domainObject.setReference(generateReferenceField.getField(), generateReferenceField.getValue());
                        }
                    } else {

                        // сгененрируем поле
                        GenerateReferenceField generateReferenceField = new GenerateReferenceField();
                        generateReferenceField.generateField(referenceFieldConfig.getName(), referenceTypeName, this);
                        domainObject.setReference(generateReferenceField.getField(), generateReferenceField.getValue());
                    }

                } else if (fieldType.equals(ru.intertrust.cm.core.business.api.dto.FieldType.LONG)) {
                    // преобразуем поле конфигурации в длинное число
                    LongFieldConfig longFieldConfig = (LongFieldConfig) fieldConfig;

                    // Сгенерируем поле
                    GenerateLongField generateLongField = new GenerateLongField();
                    generateLongField.generateField(longFieldConfig.getName());
                    domainObject.setLong(generateLongField.getField(), generateLongField.getValue());

                } else if (fieldType.equals(ru.intertrust.cm.core.business.api.dto.FieldType.DECIMAL)) {
                    // преобразеум поле конфигурации в цисло с точкой
                    DecimalFieldConfig decimalFieldConfig = (DecimalFieldConfig) fieldConfig;

                    // сгенерируем поле
                    GenerateDecimalField generateDecimalField = new GenerateDecimalField();
                    generateDecimalField.generateField(decimalFieldConfig.getName(), decimalFieldConfig.getPrecision(),
                            decimalFieldConfig.getScale());
                    domainObject.setDecimal(generateDecimalField.getField(), generateDecimalField.getValue());
                }
            }

        }

        return domainObject;
    }

    public List<DomainObject> getDomainObjectList(String type) {
        List<DomainObject> list = domainObjectDao.findAll(type,
                accessControlService.createSystemAccessToken("FullAccess"));
        return list;
    }

    public DomainObjectDao getDomainObjectDao() {
        return domainObjectDao;
    }

    /**
     * Создание нового доменного обьекта переданного типа
     *
     * @param type
     * @return
     */
    private DomainObject createDomainObject(String type) {
        GenericDomainObject taskDomainObject = new GenericDomainObject();
        taskDomainObject.setTypeName(type);
        Date currentDate = new Date();
        taskDomainObject.setCreatedDate(currentDate);
        taskDomainObject.setModifiedDate(currentDate);
        return taskDomainObject;
    }

    /**
     * Метод создает доменный объект, запускает методы анализа и создания полей
     * доменных объектов
     * */
    public DomainObject generateObject(List<TemplateType> templateList, ObjectType objectType,
            DomainObject parentDomainObject) throws IOException, NoSuchAlgorithmException {
        // Сохраним ссылку на объект, для работы с доменными объектами, в
        // глобальную переменную класса
        // Создаем объект, для заполнения его полями
        DomainObject domainObject = createDomainObject(objectType.getType());
        // получаем список полей из инструкции
        List<FieldType> fields = objectType.getStringOrDateTimeOrReference();
        // по типу полей запустим необходимые методы для их создания
        for (FieldType field : fields) {
            if (field instanceof StringType) {
                // тип поля - строка
                StringType stringType = (StringType) field;
                GenerateStringField genStringField = new GenerateStringField();
                genStringField.generateField(stringType, templateList, objectType.getType(), this);
                // добавляем строку в доменный объект
                domainObject.setString(genStringField.getField(), genStringField.getValue());

            } else if (field instanceof DateTimeType) {
                // тип поля - дата-время
                DateTimeType dateTimeType = (DateTimeType) field;
                GenerateDateTimeField genDateField = new GenerateDateTimeField();
                genDateField.generateField(dateTimeType, templateList, objectType.getType());
                // если при анализе поля удалось распознать значение как дату
                // то добавляем поле в доменный объект
                if (!genDateField.ignoreField()) {
                    domainObject.setTimestamp(genDateField.getField(), genDateField.getValue());
                }

            } else if (field instanceof ReferenceType) {
                // тип поля - ссылка
                // проверяем место расположения описания ссылки в инструкции
                ReferenceType referenceType = (ReferenceType) field;
                // проверяем наличие атрибута ParentReference
                // наличие этого атрибута со значением true говорит, что поле
                // ссылка указывает на дочерний объект
                if (referenceType.isParentReference() != null) {
                    if (referenceType.isParentReference()) {

                    }
                    if (!referenceType.isParentReference()) {
                        // обрабатываем поле reference если оно указано в
                        // инструкции к доменному объекту
                        GenerateReferenceField genReferenceField = new GenerateReferenceField();
                        genReferenceField.generateField(referenceType, templateList, objectType.getType(), this);

                        if (!genReferenceField.ignoreField()) {
                            domainObject.setReference(genReferenceField.getField(), genReferenceField.getValue());
                        }
                    }
                } else {
                    // обрабатываем поле reference если оно указано в инструкции
                    // к доменному объекту
                    GenerateReferenceField genReferenceField = new GenerateReferenceField();
                    genReferenceField.generateField(referenceType, templateList, objectType.getType(), this);
                    if (!genReferenceField.ignoreField()) {
                        domainObject.setReference(genReferenceField.getField(), genReferenceField.getValue());
                    }
                }

            } else if (field instanceof ChildrenType) {

            } else if (field instanceof LongType) {
                // тип поля - длинное число
                LongType longType = (LongType) field;
                GenerateLongField genLongField = new GenerateLongField();
                genLongField.generateField(longType, templateList, objectType.getType());
                // добавляем поле в доменный объект
                domainObject.setLong(genLongField.getField(), genLongField.getValue());

            } else if (field instanceof DecimalType) {
                // тип поля - дробное число
                DecimalType decimalType = (DecimalType) field;
                GenerateDecimalField genDecimalField = new GenerateDecimalField();
                genDecimalField.generateField(decimalType, templateList, objectType.getType());
                // добавляем поле с дробным числом в доменный объект
                domainObject.setDecimal(genDecimalField.getField(), genDecimalField.getValue());

            }
        }
        return domainObject;
    }
}
