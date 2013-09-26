package ru.intertrust.cm.core.process;

import org.activiti.bpmn.model.ActivitiListener;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.bpmn.parser.handler.UserTaskParseHandler;

/**
 * Обработчик фазы создания пользовательской задачи. Переопределен для того
 * чтобы добавить глобальный TaskListener для всех пользовательских задач.
 * Необходимо для того чтобы небыло необходимости добавлять идентичные листнеры во все пользовательские задачи
 * 
 * 
 */
public class CustomUserTaskParseHandler extends UserTaskParseHandler {
	
	/**
	 * Входная точка фазы создания задачи. Динамически добавляет листенер в коллекцию листенеров 
	 */
	@Override
	protected void executeParse(BpmnParse bpmnParse, UserTask userTask) {
		ActivitiListener globalCreateListener = new ActivitiListener();
		globalCreateListener.setEvent(TaskListener.EVENTNAME_CREATE);
		globalCreateListener.setImplementation(GlobalCreateTaskListener.class.getName());		
		globalCreateListener.setImplementationType("class");
		userTask.getTaskListeners().add(globalCreateListener);

		super.executeParse(bpmnParse, userTask);
	}
}
