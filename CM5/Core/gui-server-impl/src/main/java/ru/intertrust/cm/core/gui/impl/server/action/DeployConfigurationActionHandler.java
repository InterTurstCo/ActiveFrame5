package ru.intertrust.cm.core.gui.impl.server.action;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.PropertyResolver;
import ru.intertrust.cm.core.business.api.ConfigurationControlService;
import ru.intertrust.cm.core.business.api.dto.ConfigurationDeployedItem;
import ru.intertrust.cm.core.gui.api.server.action.ActionHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.ActionContext;
import ru.intertrust.cm.core.gui.model.action.DeployConfigurationActionContext;
import ru.intertrust.cm.core.gui.model.action.DeployConfigurationActionData;
import ru.intertrust.cm.core.gui.model.form.widget.AttachmentItem;

import javax.ejb.EJBException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 03.06.14
 *         Time: 17:15
 */
@ComponentName("deploy.configuration.action")
public class DeployConfigurationActionHandler extends ActionHandler {
    @Autowired
    private ConfigurationControlService configurationControlService;

    @Autowired
    private PropertyResolver propertyResolver;

    private static final String TEMP_STORAGE_PATH = "${attachment.temp.storage}";

    @Override
    public DeployConfigurationActionData executeAction(ActionContext context) {
        DeployConfigurationActionContext deployContext = (DeployConfigurationActionContext)context;

        List<AttachmentItem> attachmentItems = deployContext.getAttachmentItems();
        List<ConfigurationDeployedItem> configurationDeployedItems = new ArrayList<>();
        String pathForTempFilesStore = propertyResolver.resolvePlaceholders(TEMP_STORAGE_PATH);
        for (AttachmentItem attachmentItem : attachmentItems) {
            String filePath = pathForTempFilesStore + attachmentItem.getTemporaryName();
            ConfigurationDeployedItem  configurationDeployedItem = new ConfigurationDeployedItem();
            configurationDeployedItem.setFileName(attachmentItem.getName());
            try {
                String configAsString = readFileAsString(filePath, StandardCharsets.UTF_8);
                configurationControlService.updateConfiguration(configAsString);
                boolean restartRequired  = configurationControlService.restartRequiredForFullUpdate(configAsString);
                configurationDeployedItem.setRestartRequired(restartRequired);
                configurationDeployedItem.setSuccess(true);
            } catch (EJBException ejbException){
                configurationDeployedItem.setSuccess(false);
                configurationDeployedItem.setMessage(ejbException.getLocalizedMessage());
            } catch (IOException e) {
                configurationDeployedItem.setSuccess(false);
                configurationDeployedItem.setMessage(e.getLocalizedMessage());
            }finally {
                configurationDeployedItems.add(configurationDeployedItem);
            }
        }
        DeployConfigurationActionData deployConfigurationActionData = new DeployConfigurationActionData();
        deployConfigurationActionData.setConfigurationDeployedItems(configurationDeployedItems);
        return deployConfigurationActionData;
    }

    /*private String readFileAsString(String filePath) throws IOException {
        StringBuilder fileData = new StringBuilder();
        BufferedReader reader = new BufferedReader(
                new FileReader(filePath));
        char[] buf = new char[1024];
        int numRead=0;
        while((numRead=reader.read(buf)) != -1){
            String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
        }
        reader.close();
        return fileData.toString();
    }*/
    private String readFileAsString(String path, Charset encoding)
            throws IOException
    {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }
}
