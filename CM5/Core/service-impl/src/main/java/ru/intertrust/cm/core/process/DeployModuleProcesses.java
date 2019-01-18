package ru.intertrust.cm.core.process;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.config.module.ModuleConfiguration;
import ru.intertrust.cm.core.config.module.ModuleService;

/**
 * Класс автоматической загрузки шаблонов процессов, определенных как ресурсы модуля
 * @author larin
 *
 */
public class DeployModuleProcesses {
    private static final Logger logger = Logger.getLogger(DeployModuleProcesses.class);
            
    @Autowired
    private ModuleService moduleService;

    public void load() {
        //Цикл по модулям
        for (ModuleConfiguration moduleConfiguration : moduleService.getModuleList()) {
            if (moduleConfiguration.getDeployProcesses() != null && moduleConfiguration.getDeployProcesses().size() > 0) {
                //Цикл по процессам
                for (String processResource : moduleConfiguration.getDeployProcesses()) {
                    //Деполй процесса
                    ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
                    RepositoryService repositoryService = processEngine.getRepositoryService();
                    Deployment deployment = repositoryService.createDeployment()
                            .addClasspathResource(processResource)
                            .name(processResource)
                            .deploy();
                    logger.info("Process + " + processResource + " is deployed. Process name: " + deployment.getName() + "; Process ID: " + deployment.getId());
                }
            }
        }
    }
}
