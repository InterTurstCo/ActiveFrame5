package ru.intertrust.performance.jmeter;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.StringValue;

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

    @Override
    public SampleResult sample(Entry e) {
        SampleResult res = new SampleResult();
        String providerUrl = getProviderUrl();
        String securityPrincipal = getSecurityPrincipal();
        String securityCreditals = getSecurityCredentials();
        String objectName = getObjectName();
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
            DomainObject dobj = crudService.createDomainObject("Department");
            dobj.setValue("Name", new StringValue("name"));
            crudService.save(dobj);
            res.setResponseData(dobj.toString(), SampleResult.DEFAULT_HTTP_ENCODING);
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
}
