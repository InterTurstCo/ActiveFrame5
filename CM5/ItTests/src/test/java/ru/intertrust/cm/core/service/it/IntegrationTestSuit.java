package ru.intertrust.cm.core.service.it;

import java.io.File;
import java.io.FileInputStream;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.eu.ingwar.tools.arquillian.extension.suite.annotations.ArquilianSuiteDeployment;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;

/**
 * Группировка интеграционных тестов для выполнения в одном деплойменте
 * @author atsvetkov
 */
@ArquilianSuiteDeployment
public class IntegrationTestSuit {

    @Deployment
    public static Archive<EnterpriseArchive> createDeployment() {
        return createDeployment(new Class[] { }, new String[] {
                "test-data/status.csv",
                "test-data/import-department.csv",
                "test-data/import-organization.csv",
                "test-data/import-employee.csv",
                "test-data/set-department-boss.csv",
                "test-data/set-organization-boss.csv",
                "test-data/import-system-profile.csv",
                "test-data/import-person-profile.csv",
                "test-data/import-string-value.csv",
                "test-data/import-employee-prof.csv",
                "test-data/configuration-test.xml",
                "test-data/configuration-test-new.xml",
                "test-data/actions-test.xml",
                "beans.xml" });
    }

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

        // добавляются java пакеты с тестовыми классами
        JavaArchive testArchive = ShrinkWrap.create(JavaArchive.class)
                .addPackages(true, "ru.intertrust.cm.core.service.it")
                .addPackages(true, "ru.intertrust.cm.webcontext")
                .addClasses(LoginPasswordCallbackHandler.class);

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

}
