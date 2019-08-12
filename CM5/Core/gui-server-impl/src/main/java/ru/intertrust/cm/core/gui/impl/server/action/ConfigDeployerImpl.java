package ru.intertrust.cm.core.gui.impl.server.action;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import ru.intertrust.cm.core.gui.model.plugin.DeployConfigType;

public class ConfigDeployerImpl extends DeployerBase{
    @Override
    public DeployConfigType getDeployConfigType() {
        return new DeployConfigType("af5-config", "Конфигурация AF5", "xml");
    }

    @Override
    protected Charset getEncoding() {
        return StandardCharsets.UTF_8;
    }

}
