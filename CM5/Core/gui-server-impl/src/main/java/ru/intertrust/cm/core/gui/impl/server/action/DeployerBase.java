package ru.intertrust.cm.core.gui.impl.server.action;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.ConfigurationControlService;
import ru.intertrust.cm.core.gui.api.server.action.ConfigurationDeployer;
import ru.intertrust.cm.core.model.FatalException;

public abstract class DeployerBase implements ConfigurationDeployer {
    @Autowired
    private ConfigurationControlService configurationControlService;

    @Override
    public void deploy(String name, File file) {
        try {
            String configAsString = readFileAsString(file);
            configurationControlService.updateConfiguration(configAsString, name);
        } catch (IOException ex) {
            throw new FatalException("Error deploy", ex);
        }
    }

    protected Charset getEncoding(){
        return StandardCharsets.UTF_8;
    }

    protected String readFileAsString(File file) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(file.getPath()));
        return new String(encoded, getEncoding());
    }

}
