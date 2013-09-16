package ru.intertrust.cm.performance.dataset;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import javax.annotation.PostConstruct;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.xml.sax.SAXException;

import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.util.SpringApplicationContext;
import ru.intertrust.cm.performance.dataset.jaxb.ChildrenType;
import ru.intertrust.cm.performance.dataset.jaxb.DateTimeType;
import ru.intertrust.cm.performance.dataset.jaxb.FieldType;
import ru.intertrust.cm.performance.dataset.jaxb.ObjectType;
import ru.intertrust.cm.performance.dataset.jaxb.QueryType;
import ru.intertrust.cm.performance.dataset.jaxb.SetType;
import ru.intertrust.cm.performance.dataset.jaxb.TemplateType;


/**
 * 
 * @author erentsov
 *
 */
@Local(DatasetGenerationService.class)
@Remote(DatasetGenerationService.Remote.class)
@Stateless

public class DatasetGenerationServiceImpl implements DatasetGenerationService, DatasetGenerationService.Remote {
    
    private final StringBuffer response = new StringBuffer();
    private Unmarshaller unmarshaller;
    private RandomGenerators generators;
    private Date currentDate;
    
    @Autowired
    private CollectionsService collectionsService;
    
    @Autowired
    private CrudService crudService;
    
    public class XmlValidationEventHandler implements ValidationEventHandler {

        public boolean handleEvent(ValidationEvent event) {
            response.append("\n")
            .append("Event" + "\n")
            .append("Severity:  " + event.getSeverity() + "\n")
            .append("Message:  " + event.getMessage() + "\n")
            .append("Linked Exception:  " + event.getLinkedException() + "\n")
            .append("Locator:::" + "\n")
            .append("    Line number:  " + event.getLocator().getLineNumber() + "\n")
            .append("    Column number:  " + event.getLocator().getColumnNumber() + "\n")
            .append("    Offset:  " + event.getLocator().getOffset() + "\n")
            .append("    Object:  " + event.getLocator().getObject() + "\n")
            .append("    Node:  " + event.getLocator().getNode() + "\n")
            .append("    URL:  " + event.getLocator().getURL() + "\n");
            return true;
        }
    }
    
    @PostConstruct
    public void init() throws JAXBException, SAXException, IOException{
        //TODO Эти строчки можно будет удалить после того, как будет реализован autowire для удаленных ejb.
        AutowiredAnnotationBeanPostProcessor bpp = new AutowiredAnnotationBeanPostProcessor();
        bpp.setBeanFactory(SpringApplicationContext.getContext().getAutowireCapableBeanFactory());
        bpp.processInjection(this);
        //--------------------------------------------------------------------------------
        generators = RandomGenerators.getInstance();
        JAXBContext jc = JAXBContext.newInstance( "ru.intertrust.cm.performance.dataset.jaxb" );
        StreamSource schemaSource = new StreamSource(getClass().getResourceAsStream("/dataset-generator-configuration.xsd"));
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = factory.newSchema(schemaSource);
        unmarshaller = jc.createUnmarshaller();
        unmarshaller.setSchema(schema);
        unmarshaller.setEventHandler(new XmlValidationEventHandler());
    }
    
    public String execute(byte[] command){
        try{
            currentDate = new Date();
            response.setLength(0);            
            QueryType query = (QueryType) ((JAXBElement<?>) unmarshaller.unmarshal(new ByteArrayInputStream(command))).getValue();
            validateQuery(query);
            processQuery(query);
        }catch(Exception e){
            response.append(e.getMessage());
        }
        return response.toString();
    }
    
    private void validateQuery(QueryType query){      
       
    }
    
    private void processQuery(QueryType query){
       
    }
    
    

}

