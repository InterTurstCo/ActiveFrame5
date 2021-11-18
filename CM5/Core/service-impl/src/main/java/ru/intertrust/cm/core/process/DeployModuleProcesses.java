package ru.intertrust.cm.core.process;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.InputStreamProvider;
import ru.intertrust.cm.core.business.api.ProcessService;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.module.ModuleConfiguration;
import ru.intertrust.cm.core.config.module.ModuleService;
import ru.intertrust.cm.core.model.FatalException;

/**
 * Класс автоматической загрузки шаблонов процессов, определенных как ресурсы модуля
 * @author larin
 *
 */
public class DeployModuleProcesses {
    private static final Logger logger = Logger.getLogger(DeployModuleProcesses.class);
            
    @Autowired
    private ProcessService processService;

    @Autowired
    private ModuleService moduleService;

    public void load() {
        try {
            //Цикл по модулям
            for (ModuleConfiguration moduleConfiguration : moduleService.getModuleList()) {
                if (moduleConfiguration.getDeployProcesses() != null && moduleConfiguration.getDeployProcesses().size() > 0) {
                    //Цикл по процессам
                    for (String processResource : moduleConfiguration.getDeployProcesses()) {
                        // Проверка поддерживает ли текущий движок данный шаблон процесса
                        if (processService.isSupportTemplate(processResource)) {
                            //Деполй процесса
                            String processName = processResource;
                            if (processResource.contains("/")){
                                processName = processResource.substring(processResource.lastIndexOf("/") + 1);
                            }

                            InputStreamProvider provider = () -> getClass().getClassLoader().getResourceAsStream(processResource);
                            Id deployId = processService.saveProcess(provider, processName, ProcessService.SaveType.ACTIVATE);
                            logger.info("Process + " + processResource + " is deployed. Process name: " + processResource + "; Process ID: " + deployId);
                        } else {
                            logger.warn("Process " + processResource + " is not support by wf engene " + processService.getEngeneName());
                        }
                    }
                }
            }
        } catch (Exception ex) {
            throw new FatalException("Error install module processes", ex);
        }
    }
}
