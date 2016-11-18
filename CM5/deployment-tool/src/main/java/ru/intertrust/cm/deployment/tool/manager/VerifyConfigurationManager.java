package ru.intertrust.cm.deployment.tool.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.intertrust.cm.deployment.tool.property.UpgradePropertiesValidator;
import ru.intertrust.cm.deployment.tool.service.JbossService;
import ru.intertrust.cm.deployment.tool.service.PostgresService;
import ru.intertrust.cm.deployment.tool.service.ResourceService;

import static ru.intertrust.cm.deployment.tool.DeploymentToolApplication.stopApp;

/**
 * Created by Alexander Bogatyrenko on 02.09.16.
 * <p>
 * This class represents...
 */
@Service
public class VerifyConfigurationManager {

    private static Logger logger = LoggerFactory.getLogger(VerifyConfigurationManager.class);

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private PostgresService postgresService;

    @Autowired
    private JbossService jbossService;


    @Autowired
    private UpgradePropertiesValidator propertiesValidator;

    public void verify() {
        logger.info("Start verify configuration");

        boolean isValid = propertiesValidator.validate();
        if (!isValid) {
            stopApp();
        }

        if (resourceService.verifyPaths() && postgresService.testConnection() && testJbossCommands()) {
            logger.info("Verify configuration - success");
        } else {
            logger.error("Verify configuration - fail");
            stopApp();
        }
    }

    @Deprecated
    private boolean testJbossCommands() {
        if (jbossService.isServerStarted()) {
            logger.info("Start testing stop jboss command");

            logger.info("Start testing start jboss command");
            return true;
        } else {
            logger.info("Start testing start jboss command");
            boolean hasStarted = true;
            logger.info("Start testing stop jboss command");
            return true;
        }
    }
}
