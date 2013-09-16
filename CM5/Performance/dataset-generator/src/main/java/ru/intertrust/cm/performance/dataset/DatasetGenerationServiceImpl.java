package ru.intertrust.cm.performance.dataset;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Date;

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
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.util.SpringApplicationContext;
import ru.intertrust.cm.performance.dataset.jaxb.adapters.DateTimeAdapter;
import ru.intertrust.cm.performance.dataset.jaxb.adapters.DurationAdapter;
import ru.intertrust.cm.performance.dataset.jaxb.adapters.NonNegativeIntegerAdapter;
import ru.intertrust.cm.performance.dataset.xmltypes.QueryType;


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
    
    @Autowired
    ConfigurationExplorer configurationExplorer;
    
    @PostConstruct
    public void init() throws JAXBException, SAXException, IOException{
        //TODO Эти строчки можно будет удалить после того, как будет реализован autowire для удаленных ejb.
        AutowiredAnnotationBeanPostProcessor bpp = new AutowiredAnnotationBeanPostProcessor();
        bpp.setBeanFactory(SpringApplicationContext.getContext().getAutowireCapableBeanFactory());
        bpp.processInjection(this);
        //--------------------------------------------------------------------------------
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
    
    public String execute(byte[] command){
        try{
            currentDate = new Date();
            response.setLength(0);      
            validator.validate(new StreamSource(new ByteArrayInputStream(command)));
            QueryType query = persister.read(QueryType.class, new ByteArrayInputStream(command));
            validateQuery(query);
            processQuery(query);
        }catch(Exception e){
            //e.printStackTrace();
            response.append(e.getMessage());
        }
        return response.toString();
    }
    
    private void validateQuery(QueryType query){      
       
    }
    
    private void processQuery(QueryType query){
       
    }
    
    

}

