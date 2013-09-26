package ru.intertrust.cm.core.business.api;

import java.util.List;

import ru.intertrust.cm.core.business.api.dto.DeployedProcess;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.ProcessVariable;

/**
 * Сервис для взаимодействия с процессами
 * 
 * @author larin
 * 
 */
public interface ProcessService {

	public static final long TASK_STATE_SEND = 1;
	public static final long TASK_STATE_ACQUIRED = 2;
	public static final long TASK_STATE_COMPLETE = 3;
	
	public static final String MAIN_ATTACHMENT = "MAIN_ATTACHMENT";
	public static final String MAIN_ATTACHMENT_ID = "MAIN_ATTACHMENT_ID";
		
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
	 * 
	 * @param key
	 *            идентификатор шаблона процесса
	 * @param cascade
	 *            каскадное удаление(true) или нет(false)
	 */
	void undeployProcess(String processDefinitionId, boolean cascade);

	/**
	 * Получение доступных задач для юзера
	 * 
	 * @return список объектов типов UserTask
	 */
	List<DomainObject> getUserTasks();

	/**
	 * Получение доступных задач для юзера и определенного доменного обьекта
	 * 
	 * @param attachedObjectId
	 *            Идентификатор доменного объекта
	 * @return список доменных объектов типа UserTask
	 */
	List<DomainObject> getUserDomainObjectTasks(Id attachedObjectId);

	/**
	 * Завершить задачу
	 * 
	 * @param taskId
	 *            идентификатор задачи
	 * @variables
	 *            Список переменных процесса
	 * 
	 */
	void completeTask(Id taskId, List<ProcessVariable> variables, String action);

	/**
	 * Получение информации об установленных процессах
	 * @return
	 */
	List<DeployedProcess> getDeployedProcesses();
}
