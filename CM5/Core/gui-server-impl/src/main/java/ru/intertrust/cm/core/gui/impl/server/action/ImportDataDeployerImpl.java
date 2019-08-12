package ru.intertrust.cm.core.gui.impl.server.action;

import java.nio.charset.Charset;

import ru.intertrust.cm.core.gui.model.plugin.DeployConfigType;

public class ImportDataDeployerImpl extends DeployerBase{

    @Override
    public DeployConfigType getDeployConfigType() {
        return new DeployConfigType("import-data", "Данные в формате csv", "csv");
    }

    @Override
    protected Charset getEncoding() {
        return Charset.forName("Windows-1251");
    }

}
