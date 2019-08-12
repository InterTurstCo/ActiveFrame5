package ru.intertrust.cm.core.gui.impl.server.action;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.PropertyResolver;

import ru.intertrust.cm.core.business.api.dto.ConfigurationDeployedItem;
import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.gui.api.server.action.ActionHandler;
import ru.intertrust.cm.core.gui.api.server.action.ConfigurationDeployer;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.DeployConfigurationActionContext;
import ru.intertrust.cm.core.gui.model.action.DeployConfigurationActionData;
import ru.intertrust.cm.core.gui.model.form.widget.AttachmentItem;
import ru.intertrust.cm.core.model.FatalException;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 03.06.14
 *         Time: 17:15
 */
@ComponentName("deploy.configuration.action")
public class DeployConfigurationActionHandler
        extends ActionHandler<DeployConfigurationActionContext, DeployConfigurationActionData> {

    @Autowired
    private PropertyResolver propertyResolver;
    
    @Autowired
    private List<ConfigurationDeployer> deployers;    

    private static final String TEMP_STORAGE_PATH = "${attachment.temp.storage}";

    @Override
    public DeployConfigurationActionData executeAction(DeployConfigurationActionContext deployContext) {

        ConfigurationDeployer deployer = getConfigurationDeployer(deployContext.getConfigType());
        
        List<AttachmentItem> attachmentItems = deployContext.getAttachmentItems();
        List<ConfigurationDeployedItem> configurationDeployedItems = new ArrayList<>();
        String pathForTempFilesStore = propertyResolver.resolvePlaceholders(TEMP_STORAGE_PATH);
        for (AttachmentItem attachmentItem : attachmentItems) {
            File file = new File(pathForTempFilesStore, attachmentItem.getTemporaryName());
            ConfigurationDeployedItem  configurationDeployedItem = new ConfigurationDeployedItem();
            configurationDeployedItem.setFileName(attachmentItem.getName());
            try {
                deployer.deploy(attachmentItem.getName(), file);
                configurationDeployedItem.setSuccess(true);
            } catch (Exception ex){
                configurationDeployedItem.setSuccess(false);
                configurationDeployedItem.setMessage(ex.getLocalizedMessage());
            }finally {
                configurationDeployedItems.add(configurationDeployedItem);
            }
        }
        DeployConfigurationActionData deployConfigurationActionData = new DeployConfigurationActionData();
        deployConfigurationActionData.setConfigurationDeployedItems(configurationDeployedItems);
        return deployConfigurationActionData;
    }

    private ConfigurationDeployer getConfigurationDeployer(String configType) {
        for (ConfigurationDeployer configurationDeployer : deployers) {
            if (configurationDeployer.getDeployConfigType().getType().equals(configType)) {
                return configurationDeployer;
            }
        }
        throw new FatalException("Deployer fore type " + configType + " not found");
    }

    @Override
    public DeployConfigurationActionContext getActionContext(final ActionConfig actionConfig) {
        return new DeployConfigurationActionContext(actionConfig);
    }

}
