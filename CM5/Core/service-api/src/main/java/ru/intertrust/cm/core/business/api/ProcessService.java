package ru.intertrust.cm.core.business.api;

import java.util.List;
import java.util.Map;

import ru.intertrust.cm.core.business.api.dto.DeployedProcess;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.ProcessVariable;
import ru.intertrust.cm.core.business.api.workflow.ProcessInstanceInfo;
import ru.intertrust.cm.core.business.api.workflow.ProcessTemplateInfo;
import ru.intertrust.cm.core.business.api.workflow.TaskInfo;
import ru.intertrust.cm.core.business.api.workflow.WorkflowTaskData;

/**
 * Сервис для взаимодействия с процессами
 * 
 * @author larin
 * 
 */
public interface ProcessService {

    public static final String TASK_STATE_SEND = "Send";
    public static final String TASK_STATE_ACQUIRED = "Acquired";
    public static final String TASK_STATE_COMPLETE = "Complete";
    public static final String TASK_STATE_PRETERMIT = "Pretermit";

    @Deprecated
    public static final String MAIN_ATTACHMENT = "MAIN_ATTACHMENT";
    @Deprecated
    public static final String MAIN_ATTACHMENT_ID = "MAIN_ATTACHMENT_ID";
    
    public static final String CTX = "CTX";
    public static final String CTX_ID = "CTX_ID";
    
    public static final String SESSION = "SESSION";

    /**
     * Удаленный интерфейс для EJB
     * 
     * @author larin
     * 
     */
    public interface Remote extends ProcessService {
    }

    /**
     * Запуск процесса
     * 
     * @param processName
     *            название(ключ) процесса
     * @param attachedObjectId
     *            Идентификатор приаттаченного
     * @param variables
     *            список переменных, прикрепляемых к процессу
     * @return возвращается идентификатор запущенного процесса
     */
    String startProcess(String processName, Id attachedObjectId,
            List<ProcessVariable> variables);

    /**
     * Остановка процесса
     * 
     * @param processId
     *            идентификатор запущенного процесса
     */
    void terminateProcess(String processId);

    /**
     * Установка шаблона процесса
     * 
     * @param processDefinition
     *            описания процесса в виде строки
     * @param processName
     *            имя файла
     * @return возвращается идентификатор шаблона процесса
     */
    String deployProcess(byte[] processDefinition, String processName);

    /**
     * Удаление шаблона процесса(каскадно или нет). При каскадном удалении
     * происходит удаление всех исполнений процессов от это шаблона, а так же
     * история выполненых процессов.
     * @param processDefinitionId
     * @param cascade
     */
    void undeployProcess(String processDefinitionId, boolean cascade);

    /**
     * Получение доступных задач для текущего юзера
     * 
     * @return список объектов типов UserTask
     */
    List<DomainObject> getUserTasks();
    
    /**
     * Получение доступных задач для юзера по userId
     * 
     * @param personId -id юзера, для которого нужно получить задачи
     * @return список объектов типов UserTask
     */
    List<DomainObject> getUserTasks(Id personId);

    /**
     * Получение доступных задач для текущего юзера и определенного доменного обьекта
     * 
     * @param attachedObjectId
     *            Идентификатор доменного объекта
     * @return список доменных объектов типа UserTask
     */
    List<DomainObject> getUserDomainObjectTasks(Id attachedObjectId);
    
    /**
     * Получение доступных задач для юзера по userId и определенного доменного обьекта
     * 
     * @param attachedObjectId
     *            Идентификатор доменного объекта
     * @param personId -id юзера, для которого нужно получить задачи        
     * @return список доменных объектов типа UserTask
     */
    List<DomainObject> getUserDomainObjectTasks(Id attachedObjectId, Id personId);

    /**
     * Завершить задачу
     * 
     * @param taskId
     *            идентификатор задачи   
     * @variables Список переменных процесса
     * 
     */
    void completeTask(Id taskId, List<ProcessVariable> variables, String action);

    /**
     * Получение информации об установленных процессах
     * @return
     */
    List<DeployedProcess> getDeployedProcesses();

    /**
     * Отправка сообщения процессу
     * @param processName Имя процесса
     * @param contextId идентификатор документа привязаннорго к процессу
     * @param variables переменные процесса
     */
    void sendProcessMessage(String processName, Id contextId, String message, List<ProcessVariable> variables);

    /**
     * Отправка сигнала всем процессам
     * @param signal Стгнал процессу
     */
    void sendProcessSignal(String signal);

    /**
     * Создание задача внешними системами.
     * @param taskData
     * @return
     */
    Id assignTask(WorkflowTaskData taskData);

    /**
     * Проверка поддерживает ли движок устанавливаемый шаблон процессов
     * @param processDefinition
     * @param processName
     * @return
     */
    boolean isSupportTemplate(byte[] processDefinition, String processName);

    /**
     * Получение активного в данный момент движка процессов
     * @return Имя движка процессов
     */
    String getEngeneName();


    /**
     * Получение информации о шаблоне процесса
     * @param template
     * @return
     */
    ProcessTemplateInfo getProcessTemplateInfo(byte[] template);

    /**
     * Получение информации об экземпляре процесса
     * @param processInstanceId
     * @return
     */
    ProcessInstanceInfo getProcessInstanceInfo(String processInstanceId);

    /**
     * Пщдгчение информации о запущзенных процессах
     * @param offset
     * @param limit
     * @return
     */
    List<ProcessInstanceInfo> getProcessInstanceInfos(int offset, int limit);

    /**
     * Получение задач процесса
     * @param processInstanceId
     * @return
     */
    List<TaskInfo> getProcessInstanceTasks(String processInstanceId, int offset, int limit);

    /**
     * Поучение переменных процесса
     * @param processInstanceId
     * @return
     */
    Map<String, Object> getProcessInstanceVariables(String processInstanceId, int offset, int limit);
}
