package ru.intertrust.cm.core.process;

import org.flowable.bpmn.model.FlowableListener;
import org.flowable.bpmn.model.UserTask;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.engine.impl.bpmn.parser.BpmnParse;
import org.flowable.engine.impl.bpmn.parser.handler.UserTaskParseHandler;

/**
 * Обработчик фазы создания пользовательской задачи. Переопределен для того
 * чтобы добавить глобальный TaskListener для всех пользовательских задач.
 * Необходимо для того чтобы небыло необходимости добавлять идентичные листнеры
 * во все пользовательские задачи
 * 
 * 
 */
public class CustomUserTaskParseHandler extends UserTaskParseHandler {

    /**
     * Входная точка фазы создания задачи. Динамически добавляет листенер в
     * коллекцию листенеров
     */
    @Override
    protected void executeParse(BpmnParse bpmnParse, UserTask userTask) {
        FlowableListener globalCreateListener = new FlowableListener();
        globalCreateListener.setEvent(TaskListener.EVENTNAME_CREATE);
        globalCreateListener.setImplementation(GlobalCreateTaskListener.class.getName());
        globalCreateListener.setImplementationType("class");
        userTask.getTaskListeners().add(globalCreateListener);

        FlowableListener globalEndListener = new FlowableListener();
        globalEndListener.setEvent(TaskListener.EVENTNAME_DELETE);
        globalEndListener.setImplementation(GlobalDeleteTaskListener.class.getName());
        globalEndListener.setImplementationType("class");
        userTask.getTaskListeners().add(globalEndListener);

        super.executeParse(bpmnParse, userTask);
    }
}
