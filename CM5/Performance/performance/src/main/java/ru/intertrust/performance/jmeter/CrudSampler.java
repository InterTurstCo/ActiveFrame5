package ru.intertrust.performance.jmeter;

import java.util.*;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.*;

/**
 * Класс, содержащий основную логику работы семплера.
 * @author Dmitry Lyakin Date: 30.08.13 Time: 17:23
 * 
 */
public class CrudSampler extends AbstractSampler {

    private static final long serialVersionUID = 1L;

    private static final Logger log = LoggerFactory.getLogger(CrudSampler.class);

    public static final String PROVIDER_URL = "JndiSampler.provider_url";
    public static final String SECURITY_PRINCIPAL = "JndiSampler.security_principal";
    public static final String SECURITY_CREDENTIALS = "JndiSampler.security_credentials";
    public static final String OBJECT_NAME = "JndiSampler.object_name";
    public static final String ATRIBUTE_NAME = "JndiSampler.atribute_name";
    public static final String ACTION_NAME = "JndiSampler.action_name";

    private final static String CREATE = "create";
    private final static String FIND = "find";
    private final static String MODIFY = "modify";
    private final static int COUNT = 10;

    @Override
    public SampleResult sample(Entry e) {
        SampleResult res = new SampleResult();
        String providerUrl = getProviderUrl();
        String securityPrincipal = getSecurityPrincipal();
        String securityCreditals = getSecurityCredentials();

        // String objectName = getObjectName();
        String objectName = "java:cm-sochi-1.0-SNAPSHOT/web-app/CrudServiceImpl!ru.intertrust.cm.core.business.api.CrudService$Remote";

        String actionName = getActionName();

        Properties jndiProps = new Properties();
        jndiProps.put(Context.INITIAL_CONTEXT_FACTORY, "org.jboss.naming.remote.client.InitialContextFactory");
        // Адрес подключения
        jndiProps.put(Context.PROVIDER_URL, providerUrl);
        // Имя пользователя в бд
        jndiProps.put(Context.SECURITY_PRINCIPAL, securityPrincipal);
        // Пароль
        jndiProps.put(Context.SECURITY_CREDENTIALS, securityCreditals);
        jndiProps.put("jboss.naming.client.ejb.context", true);
        jndiProps.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
        jndiProps.put("jboss.naming.client.connect.options.org.xnio.Options.SASL_POLICY_NOPLAINTEXT", "false");
        try {
            // Инициализация контекста
            Context ctx = new InitialContext(jndiProps);
            // Получение CrudService
            CrudService crudService = (CrudService) ctx.lookup(objectName);
            res.sampleStart();

            if (actionName.equals(CREATE)) {
                
                DomainObject dobj = createObject(crudService, "");
                
                //DomainObject dobj = crudService.createDomainObject("Department");
                //dobj.setValue("Name", new StringValue("name"));
                //crudService.save(dobj);
                res.setResponseData(dobj.toString(), SampleResult.DEFAULT_HTTP_ENCODING);
            } else if (actionName.equals(FIND)) {
                findObject(crudService);
            } else if (actionName.equals(MODIFY)) {

            }

            res.sampleEnd();
        } catch (Exception ex) {
            log.debug("", ex);
            res.setResponseMessage(ex.toString());
        }
        return res;
    }

    public void setProviderUrl(String value) {
        setProperty(PROVIDER_URL, value);
        setName(value);
    }

    public String getProviderUrl() {
        return getPropertyAsString(PROVIDER_URL);
    }

    public void setSecurityPrincipal(String value) {
        setProperty(SECURITY_PRINCIPAL, value);
        setName(value);
    }

    public String getSecurityPrincipal() {
        return getPropertyAsString(SECURITY_PRINCIPAL);
    }

    public void setSecurityCredentials(String value) {
        setProperty(SECURITY_CREDENTIALS, value);
        setName(value);
    }

    public String getSecurityCredentials() {
        return getPropertyAsString(SECURITY_CREDENTIALS);
    }

    public void setObjectName(String value) {
        setProperty(OBJECT_NAME, value);
        setName(value);
    }

    public String getObjectName() {
        return getPropertyAsString(OBJECT_NAME);
    }

    public void setAtributeName(String value) {
        setProperty(ATRIBUTE_NAME, value);
        setName(value);
    }

    public String getAtributeName() {
        return getPropertyAsString(ATRIBUTE_NAME);
    }

    public void setActionName(String value) {
        setProperty(ACTION_NAME, value);
        setName(value);
    }

    public String getActionName() {
        return getPropertyAsString(ACTION_NAME);
    }
    
    /**
     * Метод создает новый объект и сохраняет его в базе.
     * 
     * @param crudService - интерфейс для работы с объектом
     * @param ext - расширение для наименования
     * @return - возвращает объект типа DomainObject
     * 
     * @author Stepygin Sergey Date: 02.09.13 Time: 10:00
     */
    private DomainObject createObject(CrudService crudService, String ext){
        
        DomainObject domainObject = crudService.createDomainObject("Department".concat(ext));
        domainObject.setValue("Name", new StringValue("name".concat(ext)));
        crudService.save(domainObject);
        
        return domainObject;
    }
    
    /**
     * Метод осуществляет создание, определенного количества объектов,
     * сохранение объектов в базе и поиск объектов, по присвеенным им
     * идентификаторам
     * 
     * @param crudService
     * 
     * @author Stepygin Sergey Date: 02.09.13 Time: 10:00
     */
    private void findObject(CrudService crudService) {
        List<Id> idObjects = new ArrayList<Id>();

        for (int i = 0; i < COUNT; i++) {
            String ext = String.valueOf(i);
            DomainObject domainObject = createObject(crudService, ext);
            idObjects.add(domainObject.getId());
        }

        // поиск по списку идентификаторов
        if (!idObjects.isEmpty()) {
            for(Id id : idObjects){
                DomainObject dobj = crudService.find(id);
            }
        }
    }

    private void modifyObject(CrudService crudService) {
        List<Id> idObjects = new ArrayList<Id>();

        for (int i = 0; i < COUNT; i++) {
            String ext = String.valueOf(i);
            DomainObject domainObject = createObject(crudService, ext);
            idObjects.add(domainObject.getId());
        }

        // поиск по списку идентификаторов
        if (!idObjects.isEmpty()) {
            for(Id id : idObjects){
                DomainObject dobj = crudService.find(id);
                dobj.setValue("Name", new StringValue("new_name"));
            }
        }
    }
}
