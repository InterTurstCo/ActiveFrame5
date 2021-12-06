package ru.intertrust.cm.core.business.api;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;

import ru.intertrust.cm.core.business.api.dto.DeployedProcess;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.ProcessVariable;
import ru.intertrust.cm.core.business.api.dto.SortOrder;
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
     * @param processDefinitionId
     *            ID процесса в таблице process_definition
     * @return возвращается идентификатор шаблона процесса
     */
    String deployProcess(Id processDefinitionId);

    /**
     * Сохранение процесса в хранилище
     * Метод устарел, необходимо использовать
     * {@link this#saveProcess(InputStreamProvider, String, SaveType)}
     * @param processDefinitionProvider provider, передающий описание процесса
     * @param fileName имя сохраняемого файла
     * @param deploy флаг необходимости устанавливать процесс в движок WF
     * @return идентификатор сохраненного процесса
     */
    @Deprecated
    Id saveProcess(InputStreamProvider processDefinitionProvider, String fileName, boolean deploy);

    /**
     * Сохранение процесса в хранилище
     *
     * @param processDefinitionProvider provider, передающий описание процесса
     * @param fileName имя сохраняемого файла
     * @param type какие действия необходимо произвести с сохраняемым процессом
     * @see SaveType
     * @return идентификатор сохраненного процесса
     */
    Id saveProcess(InputStreamProvider processDefinitionProvider, String fileName, SaveType type);


    /**
     * Сохранение процесса в хранилище
     * Метод необходим для RPC. При локальных вызовах, предпочтительнее использование метода
     * {@link this#saveProcess(InputStreamProvider, String, deploy)}
     * Метод устарел, необходимо использовать
     *{@link this#saveProcess(byte[], String, SaveType)}
     *
     * @param processDefinition описание процесса
     * @param fileName имя сохраняемого файла
     * @param deploy флаг необходимости устанавливать процесс в движок WF
     * @return идентификатор сохраненного процесса
     */
    @Deprecated
    Id saveProcess(byte[] processDefinition, String fileName, boolean deploy);

    /**
     * Сохранение процесса в хранилище
     * Метод необходим для RPC. При локальных вызовах, предпочтительнее использование метода
     * {@link this#saveProcess(InputStreamProvider, String, SaveType)}
     *
     * @param processDefinition описание процесса
     * @param fileName имя сохраняемого файла
     * @param type какие действия необходимо произвести с сохраняемым процессом
     * @see SaveType
     * @return идентификатор сохраненного процесса
     */
    Id saveProcess(byte[] processDefinition, String fileName, SaveType type);

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
     * @param processName
     * @return
     */
    boolean isSupportTemplate(String processName);

    /**
     * Получение активного в данный момент движка процессов
     * @return Имя движка процессов
     */
    String getEngeneName();

    /**
     * Получение информации об экземпляре процесса
     * @param processInstanceId
     * @return
     */
    ProcessInstanceInfo getProcessInstanceInfo(String processInstanceId);

    /**
     * Получение информации о запущенных процессах
     * @param offset
     * @param limit
     * @param name
     * @param startDateBegin
     * @param startDateEnd
     * @param finishDateBegin
     * @param finishDateEnd
     * @param sortOrder
     * @return
     */
    List<ProcessInstanceInfo> getProcessInstanceInfos(
            int offset, int limit, String name,
            Date startDateBegin, Date startDateEnd,
            Date finishDateBegin, Date finishDateEnd,
            SortOrder sortOrder);

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

    /**
     * Получение идентификатора крайней версии процесса с переданным ключем
     * @param processDefinitionKey
     * @return
     */
    Id getLastProcessDefinitionId(String processDefinitionKey);

    /**
     * Приостановить процесс
     * @param processInstanceId
     */
    void suspendProcessInstance(String processInstanceId);

    /**
     * Возобновить процесс
     * @param processInstanceId
     */
    void activateProcessInstance(String processInstanceId);

    /**
     * Удаление экземпляра процесса
     * @param processInstanceId
     */
    void deleteProcessInstance(String processInstanceId);


    /**
     * Получение диаграммы шаблона процесса в виде png изображения
     * @param processDefinitionId
     * @return
     */
    byte[] getProcessTemplateModel(Id processDefinitionId);

    /**
     * Получение диагараммы экземпляра процесса с подсвеченными элементами модели в виде png изображения
     * @param processInstanceId
     * @return
     */
    byte[] getProcessInstanceModel(String processInstanceId);

    /**
     *
     */
    enum SaveType {
        /**
         * Только сохранить
         */
        ONLY_SAVE,
        /**
         * Задеплоить, но не активировать
         */
        DEPLOY,
        /**
         * Задеплоить и активировать
         */
        ACTIVATE
    }

}


