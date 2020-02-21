package ru.intertrust.cm.core.gui.impl.server.action;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.codec.binary.Base64;
import ru.intertrust.cm.core.gui.model.plugin.DeployConfigType;

public class ActivityDeployerImpl extends DeployerBase {

    @Override
    public DeployConfigType getDeployConfigType() {
        return new DeployConfigType("bpmn", "Шаблон процесса Activiti", "bpmn");
    }

    protected String readFileAsString(File file) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(file.getPath()));
        return Base64.encodeBase64String(encoded);
    }
}
