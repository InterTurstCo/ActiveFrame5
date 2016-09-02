package ru.intertrust.cm.deployment.tool;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import ru.intertrust.cm.deployment.tool.config.AppConfig;
import ru.intertrust.cm.deployment.tool.manager.EarDeployManager;
import ru.intertrust.cm.deployment.tool.manager.VerifyConfigurationManager;

public class DeploymentToolApplication {

    public static void main(String[] args) {
        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(AppConfig.class);

        VerifyConfigurationManager verifyConfigurationManager = applicationContext.getBean(VerifyConfigurationManager.class);
        EarDeployManager earDeployManager = applicationContext.getBean(EarDeployManager.class);

        if (args.length > 0) { // if used commands - start or verify-config
            String arg = args[0];
            if ("start".equals(arg)) {
                earDeployManager.deploy();
            } else if ("verify-config".equals(arg)) {
                verifyConfigurationManager.verify();
            }
        } else {
            verifyConfigurationManager.verify();
            earDeployManager.deploy();
        }
    }

    public static void stopApp() {
        System.exit(0);
    }
}
