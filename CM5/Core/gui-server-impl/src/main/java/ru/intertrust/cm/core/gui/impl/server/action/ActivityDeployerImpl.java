package ru.intertrust.cm.core.gui.impl.server.action;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import ru.intertrust.cm.core.gui.model.plugin.DeployConfigType;

public class ActivityDeployerImpl extends DeployerBase {

    @Override
    public DeployConfigType getDeployConfigType() {
        return new DeployConfigType("bpmn", "Шаблон процесса Activiti", "bpmn");
    }

    @Override
    protected Charset getEncoding() {
        return StandardCharsets.UTF_8;
    }

}
