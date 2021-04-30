package ru.intertrust.cm.core.business.api.workflow;

import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.common.engine.impl.util.io.BytesStreamSource;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.ProcessEngines;
import org.flowable.image.ProcessDiagramGenerator;
import org.springframework.util.StreamUtils;
import ru.intertrust.cm.core.business.api.dto.DeployedProcess;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.ProcessVariable;
import ru.intertrust.cm.core.business.api.dto.SortOrder;
import ru.intertrust.cm.core.model.FatalException;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface WorkflowEngine {
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
     *
     * @param processDefinitionId
     *            идентификатор шаблона процесса
     * @param cascade
     *            каскадное удаление(true) или нет(false)
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
     * @param contextId идентификатор документа привязаннорго к процессу
     */
    void sendProcessMessage(String processName, Id contextId, String message, List<ProcessVariable> variables);

    /**
     * Отправка сигнала всем процессам
     */
    void sendProcessSignal(String signal);

    /**
     * Получение активных задач всех пользователей от движка процессов
     * @return
     */
    List<WorkflowTaskData> getEngeneTasks();

    /**
     * Создание группы в движке WF. Если группа есть то она находится по имени и возвращается ее идентификатор
     * @param name Имя группы
     * @return Идентификатор группы в движке WF
     */
    boolean createOrUpdateGroup(String name, Set<String> persons);

    /**
     * Создание или изменение информации о пользователе
     * @param login логин пользователя
     * @param active флаг активности пользователя
     * @return Идентификатор пользователя
     */
    boolean createOrUpdateUser(String login, boolean active);

    /**
     * Проверка поддерживает ли движок устанавливаемый шаблон процессов
     * @param processName
     * @return
     */
    boolean isSupportTemplate(String processName);

    /**
     * Возвращает имя движка процессов
     * @return
     */
    String getEngeneName();

    /**
     * Получение информации из модели процесса
     * @param tempale
     * @return
     */
    ProcessTemplateInfo getProcessTemplateInfo(byte[] tempale);

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
     * Получение идентификатора крайней версии шаблона процесса с переданным ключем
     * @param processDefinitionKey
     * @return
     */
    String getLastProcessDefinitionId(String processDefinitionKey);

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
     * @param template
     * @return
     */
    byte[] getProcessTemplateModel(byte[] template);

    /**
     * Получение диагараммы экземпляра процесса с подсвеченными элементами модели в виде png изображения
     * @param processInstanceId
     * @return
     */
    byte[] getProcessInstanceModel(String processInstanceId);
}
