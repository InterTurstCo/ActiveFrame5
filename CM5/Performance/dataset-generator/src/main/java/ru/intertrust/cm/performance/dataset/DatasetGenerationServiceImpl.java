package ru.intertrust.cm.performance.dataset;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
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

import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.util.SpringApplicationContext;
import ru.intertrust.cm.performance.dataset.jaxb.adapters.DateTimeAdapter;
import ru.intertrust.cm.performance.dataset.jaxb.adapters.DurationAdapter;
import ru.intertrust.cm.performance.dataset.jaxb.adapters.NonNegativeIntegerAdapter;
import ru.intertrust.cm.performance.dataset.xmltypes.*;
import ru.intertrust.cm.performance.dataset.generatefields.*;

/**
 * Сервис генерации данных для тестового наполнения
 * @author erentsov
 * 
 */
@Local(DatasetGenerationService.class)
@Remote(DatasetGenerationService.Remote.class)
@Stateless
public class DatasetGenerationServiceImpl implements DatasetGenerationService, DatasetGenerationService.Remote {

    private final StringBuffer response = new StringBuffer();
    private Persister persister;
    private Validator validator;
    private RandomGenerators generators;
    private Date currentDate;

    @Autowired
    private CollectionsService collectionsService;

    @Autowired
    private CrudService crudService;

     //@Autowired
     //private DomainObject domainObject;

    @Autowired
    ConfigurationExplorer configurationExplorer;

    @PostConstruct
    public void init() throws JAXBException, SAXException, IOException {
        // TODO Эти строчки можно будет удалить после того, как будет реализован
        // autowire для удаленных ejb.
        AutowiredAnnotationBeanPostProcessor bpp = new AutowiredAnnotationBeanPostProcessor();
        bpp.setBeanFactory(SpringApplicationContext.getContext().getAutowireCapableBeanFactory());
        bpp.processInjection(this);
        // --------------------------------------------------------------------------------
        StreamSource schemaSource = new StreamSource(getClass().getResourceAsStream("/dataset-generator-configuration.xsd"));
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = factory.newSchema(schemaSource);
        validator = schema.newValidator();
        generators = RandomGenerators.getInstance();

        RegistryMatcher matcher = new RegistryMatcher();
        matcher.bind(Date.class, DateTimeAdapter.class);
        matcher.bind(Duration.class, DurationAdapter.class);
        matcher.bind(Integer.class, NonNegativeIntegerAdapter.class);
        persister = new Persister(matcher);

    }

    public String execute(byte[] command) {
        try {
            currentDate = new Date();
            response.setLength(0);
            validator.validate(new StreamSource(new ByteArrayInputStream(command)));
            QueryType query = persister.read(QueryType.class, new ByteArrayInputStream(command));
            validateQuery(query);
            processQuery(query);
            fillProcessQuery(query);
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

    private void fillProcessQuery(QueryType query) {
        // получим множество объектов Template из xml
        List<TemplateType> templateList = query.getTemplate();

        // получим множество объектов Set из xml
        List<SetType> setList = query.getSet();

        // Создаем множество для хранения объектов
        List<DomainObject> domainObjects = new ArrayList<DomainObject>();

        for (SetType setType : setList) {
            ObjectType objectType = setType.getObject();
            //объект set одработан не доконца. не реализованно получение объектов из интервала и математическое ожидание
            Long quantity = Long.valueOf(setType.getQuantity().longValue());

            quantity = Long.valueOf(1L); // временное ограничение по количеству
                                         // создаваемых объектов
            for (Long i = 0L; i < quantity; i++) {
                // Создаем доменный объект. в качестве наименования используем
                // тип из инструкции
                DomainObject domainObject = crudService.createDomainObject(objectType.getType());
                
                List<FieldType> fields = objectType.getStringOrDateTimeOrReference();

                for (FieldType field : fields) {
                    if (field instanceof StringType) {
                        StringType stringType = (StringType) field;
                        GenerateStringField genStringField = new GenerateStringField();
                        genStringField.generateField(stringType, templateList, objectType.getType());

                        domainObject.setString(genStringField.getField(), genStringField.getValue());
                        
                    } else if (field instanceof DateTimeType) {

                        DateTimeType dateTimeType = (DateTimeType) field;
                        GenerateDateTimeField genDateField = new GenerateDateTimeField();
                        genDateField.generateField(dateTimeType, templateList, objectType.getType());
                        if (!genDateField.ignoreField()) {
                            domainObject.setTimestamp(genDateField.getField(), genDateField.getValue());
                        }

                    } else if (field instanceof ReferenceType) {
                        System.out.println("this is ReferenceType");
                        
                       /* ReferenceType referenceType = (ReferenceType)field;
                        GenerateReferenceField genReferenceField = new GenerateReferenceField();
                        genReferenceField.generateField(referenceType, templateList);
                        
                        if (!genReferenceField.ignoreField()) {
                            //domainObject.setReference(field, id);
                        }*/
                        
                    } else if (field instanceof ChildrenType) {
                        System.out.println("this is ChildrenType");
                        /*
                        ChildrenType childrenType = (ChildrenType)field;
                        GenerateChildrenField genChildrenField = new GenerateChildrenField();
                        genChildrenField.generateField(childrenType, tamplateList);
                        
                        if (!genChildrenField.ignoreField()) {
                            //domainObject.setReference(genChildrenField.getField(), genChildrenField.getValue());
                        }
                        */
                    } else if (field instanceof LongType) {
                        
                        LongType longType = (LongType)field;
                        GenerateLongField genLongField = new GenerateLongField();
                        genLongField.generateField(longType, templateList, objectType.getType());
                        
                        domainObject.setLong(genLongField.getField(), genLongField.getValue());
                        
                    } else if (field instanceof DecimalType) {
                        
                        DecimalType decimalType = (DecimalType)field;
                        GenerateDecimalField genDecimalField = new GenerateDecimalField();
                        genDecimalField.generateField(decimalType, templateList, objectType.getType());
                        
                        domainObject.setDecimal(genDecimalField.getField(), genDecimalField.getValue());
                        
                    }
                }

                domainObjects.add(domainObject);

            }

        }
        // Сохраняем доменные объекты в базе
        crudService.save(domainObjects);
        System.out.println("Save objects");
    }

}
