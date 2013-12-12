package ru.intertrust.cm.core.service.it;

import java.io.File;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class DeployArchives {
    public static final EnterpriseArchive SOCHI_PLATFORM = createFullDeployment();
    private static Logger LOG = LoggerFactory.getLogger(DeployArchives.class);

    private static EnterpriseArchive createFullDeployment() {
        File ear = Maven.resolver()
                .loadPomFromFile("pom.xml")
                .resolve("ru.intertrust.cm-sochi:ear:ear:0.5.2-1-SNAPSHOT")
                .withoutTransitivity()
                .asSingleFile();

        EnterpriseArchive archive = ShrinkWrap.create(ZipImporter.class, "test.ear").importFrom(ear).as(EnterpriseArchive.class);
        return archive;
    }

}
