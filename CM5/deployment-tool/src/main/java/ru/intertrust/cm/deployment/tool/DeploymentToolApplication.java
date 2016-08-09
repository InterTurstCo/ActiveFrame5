package ru.intertrust.cm.deployment.tool;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import ru.intertrust.cm.deployment.tool.manager.EarDeployManager;
import ru.intertrust.cm.deployment.tool.property.UpgradePropertiesValidator;

@SpringBootApplication
public class DeploymentToolApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(DeploymentToolApplication.class, args);

        UpgradePropertiesValidator propertiesValidator = applicationContext.getBean(UpgradePropertiesValidator.class);
        propertiesValidator.validate();

        EarDeployManager earDeployManager = applicationContext.getBean(EarDeployManager.class);
        earDeployManager.deploy();
    }
}
