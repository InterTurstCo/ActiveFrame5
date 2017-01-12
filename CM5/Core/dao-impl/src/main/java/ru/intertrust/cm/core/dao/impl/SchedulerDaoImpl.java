package ru.intertrust.cm.core.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.ClusterManager;
import ru.intertrust.cm.core.business.api.ScheduleService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTaskLoader;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.CollectionsDao;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.api.SchedulerDao;
import ru.intertrust.cm.core.dao.impl.utils.IdentifiableObjectConverter;

/**
 * Реализация DAO для работы с расписанием задач
 *
 */
public class SchedulerDaoImpl implements SchedulerDao {

    @Autowired
    private AccessControlService accessControlService;

    @Autowired
    private CollectionsDao collectionsDao;

    @Autowired
    private IdentifiableObjectConverter identifiableObjectConverter;

    @Autowired
    private DomainObjectDao domainObjectDao;
    
    @Autowired
    private ScheduleTaskLoader scheduleTaskLoader;    
    
    @Autowired
    private ClusterManager clusterManager;    
    
    public void setAccessControlService(AccessControlService accessControlService) {
        this.accessControlService = accessControlService;
    }

    @Override
    public IdentifiableObjectCollection getDeadScheduleExecution() {
        AccessToken accessToken = accessControlService.createSystemAccessToken(this.getClass().getName());

        String query = "select se.id from schedule s ";
        query += "join schedule_execution se on s.id = se.schedule ";
        query += "join status st on st.id = se.status ";
        query += "where se.node_id not in (select node_id from cluster_node) ";
        query += "and st.name != 'Complete' ";

        return collectionsDao.findCollectionByQuery(query, 0, 0, accessToken);
    }

    @Override
    public IdentifiableObjectCollection getActiveTask() {
        AccessToken accessToken = accessControlService.createSystemAccessToken(this.getClass().getName());

        String query = "select id, name,task_class,task_type,year,month,day_of_month,day_of_week,hour,minute,timeout,priority,parameters,active,all_nodes from schedule ";
        query += "where active = 1";

        return collectionsDao.findCollectionByQuery(query, 0, 0, accessToken);
    }

    @Override
    public IdentifiableObjectCollection getRunningScheduleExecution(Id taskId, String nodeId) {
        AccessToken accessToken = accessControlService.createSystemAccessToken(this.getClass().getName());

        String query = "select se.id from schedule_execution se ";
        query += "join status st on st.id = se.status ";
        query += "where se.schedule = {0} ";
        query += "and se.node_id = {1} ";
        query += "and st.name != 'Complete' ";
        
        List<Value> params = new ArrayList<Value>();
        params.add(new ReferenceValue(taskId));
        params.add(new StringValue(nodeId));
        
        return collectionsDao.findCollectionByQuery(query, params, 0, 0, accessToken);
    }

    @Override
    public IdentifiableObjectCollection getReadyScheduleExecution(String nodeId) {
        AccessToken accessToken = accessControlService.createSystemAccessToken(this.getClass().getName());

        String query = "select se.id, schedule from schedule_execution se ";
        query += "join status st on st.id = se.status ";
        query += "where se.node_id = {0}";
        query += "and st.name = 'Ready' ";

        List<Value> params = new ArrayList<Value>();
        params.add(new StringValue(nodeId));
        
        return collectionsDao.findCollectionByQuery(query, params, 0, 0, accessToken);
    }
    
    @Override
    public void createTaskExecution(Id taskId){
        AccessToken accessToken = accessControlService.createSystemAccessToken(this.getClass().getName());
        DomainObject task = domainObjectDao.find(taskId, accessToken);
        //Формируем записи задач или всем нодам или только одной случайной в зависимости от фланга AllNodes
        if (task.getBoolean(ScheduleService.SCHEDULE_ALL_NODES) != null && task.getBoolean(ScheduleService.SCHEDULE_ALL_NODES)) {
            Set<String> nodes = clusterManager.getNodesWithRole(ScheduleService.SCHEDULE_EXECUTOR_ROLE_NAME);
            for (String node : nodes) {
                createNodeTaskExecution(task.getId(), node);
            }
        } else {
            createNodeTaskExecution(task.getId(), scheduleTaskLoader.getNextNodeId());
        }        
    }
    
    /**
     * Производится проверка нет ли у этой ноды работающего задания с этим id, и
     * если нет то создается новое задание на исполнение
     * @param string
     */
    private Id createNodeTaskExecution(Id taskId, String nodeId) {
        AccessToken accessToken = accessControlService.createSystemAccessToken(this.getClass().getName());
        // Получение записей журнала исполнения этой ноды и этой задачи в статусе отличной от complete
        // И если таких нет то создаем новую задачу для этой ноды
        IdentifiableObjectCollection runningNodeTask = getRunningScheduleExecution(taskId, nodeId);
        if (runningNodeTask.size() == 0) {
            DomainObject taskExecution = createDomainObject(ScheduleService.SCHEDULE_EXECUTION);
            taskExecution.setTimestamp(ScheduleService.SCHEDULE_REDY, new Date());
            taskExecution.setString(ScheduleService.SCHEDULE_NODE_ID, nodeId);
            taskExecution.setReference(ScheduleService.SCHEDULE_EXECUTION_SCHEDULE, taskId);
            taskExecution = domainObjectDao.save(taskExecution, accessToken);
            return taskExecution.getId(); 
        }
        return null;
    }  
    
    /**
     * Создание доменного объекта
     * @param type
     * @return
     */
    private DomainObject createDomainObject(String type) {
        GenericDomainObject domainObject = new GenericDomainObject();
        domainObject.setTypeName(type);
        Date currentDate = new Date();
        domainObject.setCreatedDate(currentDate);
        domainObject.setModifiedDate(currentDate);
        return domainObject;
    }    
}
