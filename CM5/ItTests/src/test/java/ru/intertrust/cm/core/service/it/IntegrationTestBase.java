package ru.intertrust.cm.core.service.it;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;

/**
 * Базовый класс для интеграционных тестов
 * @author larin
 *
 */
public class IntegrationTestBase {
    
    private InitialContext ctx;
    /**
     * Получение ear архива для установки на сервер приложений
     * @param currentClass
     * @return
     */
    public static Archive<EnterpriseArchive> createDeployment(Class<?> currentClass) {
        
        File ear = Maven.resolver()
                .loadPomFromFile("pom.xml")
                .resolve("ru.intertrust.cm-sochi:ear:ear:?")
                .withoutTransitivity()
                .asSingleFile();
        
        EnterpriseArchive archive = ShrinkWrap.create(ZipImporter.class, "test.ear").importFrom(ear).as(EnterpriseArchive.class);

        JavaArchive testArchive = ShrinkWrap.create(JavaArchive.class)
                .addClasses(currentClass, LoginPasswordCallbackHandler.class, IntegrationTestBase.class);        
        testArchive.addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

        WebArchive webApp = archive.getAsType(WebArchive.class, "web-app.war");
        webApp.addAsLibraries(testArchive);
        
        
        return archive;
    }

    /**
     * Подключение под именем пользователя и пароля
     * @param login
     * @param password
     * @return
     * @throws LoginException
     */
    protected LoginContext login(String login, String password)
            throws LoginException {
        LoginContext lc = null;
        lc = new LoginContext("CM5", new LoginPasswordCallbackHandler(login, password));
        lc.login();
        return lc;
    }

    /**
     * Получение удаленного сервиса
     * @param serviceName
     * @param remoteInterfaceClass
     * @return
     * @throws NamingException
     */
    protected Object getRemoteService(String serviceName, Class remoteInterfaceClass, String login, String password) throws Exception {
        if (ctx == null) {
            
            Properties properties = new Properties(); 
            properties.load(new FileInputStream("client.properties"));
            
            Properties jndiProps = new Properties();
            jndiProps.put(Context.INITIAL_CONTEXT_FACTORY,
                    "org.jboss.naming.remote.client.InitialContextFactory");
            jndiProps.put(Context.PROVIDER_URL, "remote://" + properties.getProperty("address"));
            jndiProps.put("jboss.naming.client.ejb.context", "true");
            jndiProps
                    .put("jboss.naming.client.connect.options.org.xnio.Options.SASL_POLICY_NOPLAINTEXT",
                            "false");
            jndiProps.put(Context.SECURITY_PRINCIPAL, login);
            jndiProps.put(Context.SECURITY_CREDENTIALS, password);
            jndiProps.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");

            ctx = new InitialContext(jndiProps);
        }

        Object service = ctx.lookup("ejb:cm-sochi/web-app//" + serviceName + "!" + remoteInterfaceClass.getName());
        return service;

    }
    
    
}
