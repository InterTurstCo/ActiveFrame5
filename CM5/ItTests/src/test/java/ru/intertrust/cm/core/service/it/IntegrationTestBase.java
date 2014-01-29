package ru.intertrust.cm.core.service.it;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.ejb.EJB;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;

import ru.intertrust.cm.core.business.api.ImportDataService;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;

/**
 * Базовый класс для интеграционных тестов
 * @author larin
 * 
 */
public class IntegrationTestBase {

    @EJB
    protected ImportDataService.Remote importService;

    @Inject
    protected DomainObjectTypeIdCache domainObjectTypeIdCache;

    /**
     * Получение ear архива для установки на сервер приложений
     * @param currentClass
     * @return
     */
    public static Archive<EnterpriseArchive> createDeployment(Class<?>[] classesForDeploy, String[] resourcesForDeploy) {

        String version = getArtifactVersion();
        File ear = Maven.resolver()
                .loadPomFromFile("pom.xml")
                .resolve("ru.intertrust.cm-sochi:ear:ear:" + version)
                .withoutTransitivity()
                .asSingleFile();

        EnterpriseArchive archive =
                ShrinkWrap.create(ZipImporter.class, "test.ear").importFrom(ear).as(EnterpriseArchive.class);

        JavaArchive testArchive = ShrinkWrap.create(JavaArchive.class)
                .addClasses(LoginPasswordCallbackHandler.class, IntegrationTestBase.class);

        testArchive.addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

        if (classesForDeploy != null) {
            for (int i = 0; i < classesForDeploy.length; i++) {
                testArchive.addClass(classesForDeploy[i]);
            }
        }

        if (resourcesForDeploy != null) {
            for (int i = 0; i < resourcesForDeploy.length; i++) {
                testArchive.addAsResource(resourcesForDeploy[i]);
            }
        }

        WebArchive webApp = archive.getAsType(WebArchive.class, "web-app.war");
        webApp.addAsLibraries(testArchive);

        return archive;
    }

    /**
     * Получение версии артифакта
     * @return
     */
    private static String getArtifactVersion() {
        FileInputStream stream = null;
        try {
            MavenXpp3Reader mavenreader = new MavenXpp3Reader();
            stream = new FileInputStream("pom.xml");
            Model model = mavenreader.read(stream);
            return model.getParent().getVersion();
        } catch (Exception ex) {
            throw new RuntimeException("Error get artifact version", ex);
        } finally {
            try {
                stream.close();
            } catch (Exception ignoreEx) {
            }
        }
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
     * Импорт тестовых данных
     * @param filePath
     * @throws IOException
     */
    protected void importTestData(String filePath) throws IOException {
        URL fileUrl = Thread.currentThread().getContextClassLoader().getResource(filePath);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        InputStream input = null;
        try {
            input = fileUrl.openStream();
            int read = 0;
            byte[] buffer = new byte[1024];
            while ((read = input.read(buffer)) > 0) {
                out.write(buffer, 0, read);
            }

            importService.importData(out.toByteArray());
        } finally {
            try {
                input.close();
            } catch (Exception ignoreEx) {

            }
        }
    }

}
