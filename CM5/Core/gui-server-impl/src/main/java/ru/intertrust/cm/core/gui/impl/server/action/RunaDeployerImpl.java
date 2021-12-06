package ru.intertrust.cm.core.gui.impl.server.action;

import org.apache.commons.codec.binary.Base64;
import ru.intertrust.cm.core.gui.model.plugin.DeployConfigType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class RunaDeployerImpl extends DeployerBase {

    @Override
    public DeployConfigType getDeployConfigType() {
        return new DeployConfigType("par", "Шаблон процесса Runa", "par");
    }

    protected String readFileAsString(File file) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(file.getPath()));
        return Base64.encodeBase64String(encoded);
    }

}
