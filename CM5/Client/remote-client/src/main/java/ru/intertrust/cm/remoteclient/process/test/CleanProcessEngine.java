package ru.intertrust.cm.remoteclient.process.test;

import java.util.List;

import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.ProcessService;
import ru.intertrust.cm.core.business.api.dto.DeployedProcess;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.remoteclient.ClientBase;

/**
 * Очистка подсистемы процессов от инсталлированных процессов
 * @author larin
 * 
 */
public class CleanProcessEngine extends ClientBase {
    public static void main(String[] args) {
        try {
            CleanProcessEngine test = new CleanProcessEngine();
            test.execute(args);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void execute(String[] args) throws Exception {
        super.execute(args);

        ProcessService.Remote service = (ProcessService.Remote) getService(
                "ProcessService", ProcessService.Remote.class);

        CrudService.Remote crudService = (CrudService.Remote) getService(
                "CrudServiceImpl", CrudService.Remote.class);

        List<DeployedProcess> deployedProcesses = service.getDeployedProcesses();
        for (DeployedProcess deployedProcess : deployedProcesses) {
            service.undeployProcess(deployedProcess.getId(), true);
            log("Undeploy process " + deployedProcess.getName());
        }

        // Удаление задач
        List<DomainObject> assignee = crudService.findAll("Assignee_Person");
        for (DomainObject domainObject : assignee) {
            crudService.delete(domainObject.getId());
        }

        
        List<DomainObject> tasks = crudService.findAll("person_task");
        log("Find " + tasks.size() + " tasks");
        for (DomainObject domainObject : tasks) {
            crudService.delete(domainObject.getId());
            log("Delete " + domainObject.getId());
        }
        
        writeLog();
    }
}
