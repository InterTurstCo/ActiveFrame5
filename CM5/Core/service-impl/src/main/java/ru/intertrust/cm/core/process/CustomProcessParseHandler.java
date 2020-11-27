package ru.intertrust.cm.core.process;

import org.flowable.bpmn.model.FlowableListener;
import org.flowable.bpmn.model.Process;
import org.flowable.engine.delegate.ExecutionListener;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.engine.impl.bpmn.parser.BpmnParse;
import org.flowable.engine.impl.bpmn.parser.handler.ProcessParseHandler;

public class CustomProcessParseHandler extends ProcessParseHandler {
    @Override
    protected void executeParse(BpmnParse bpmnParse, Process process) {
        FlowableListener globalStartListener = new FlowableListener();
        globalStartListener.setEvent(ExecutionListener.EVENTNAME_START);
        globalStartListener.setImplementation(GlobalStartProcessListener.class.getName());
        globalStartListener.setImplementationType("class");
        process.getExecutionListeners().add(globalStartListener);

        FlowableListener globalEndListener = new FlowableListener();
        globalEndListener.setEvent(ExecutionListener.EVENTNAME_END);
        globalEndListener.setImplementation(GlobalEndProcessListener.class.getName());
        globalEndListener.setImplementationType("class");
        process.getExecutionListeners().add(globalEndListener);

        super.executeParse(bpmnParse, process);
    }
}
