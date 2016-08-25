package ru.intertrust.cm.deployment.tool;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import ru.intertrust.cm.deployment.tool.config.AppConfig;
import ru.intertrust.cm.deployment.tool.manager.EarDeployManager;
import ru.intertrust.cm.deployment.tool.property.UpgradePropertiesValidator;

public class DeploymentToolApplication {

    public static void main(String[] args) {
        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(AppConfig.class);

        UpgradePropertiesValidator propertiesValidator = applicationContext.getBean(UpgradePropertiesValidator.class);
        boolean isValid = propertiesValidator.validate();

        if (isValid) {
            EarDeployManager earDeployManager = applicationContext.getBean(EarDeployManager.class);
            earDeployManager.deploy();
        }
    }
}
