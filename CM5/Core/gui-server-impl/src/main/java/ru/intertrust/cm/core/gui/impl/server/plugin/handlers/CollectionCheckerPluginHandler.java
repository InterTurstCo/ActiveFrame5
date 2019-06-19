package ru.intertrust.cm.core.gui.impl.server.plugin.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.ConfigurationService;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.base.CollectionConfig;
import ru.intertrust.cm.core.gui.api.server.plugin.PluginHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.plugin.CollectionCheckResult;
import ru.intertrust.cm.core.gui.model.plugin.CollectionCheckerPluginData;

import java.util.Collection;


@ComponentName("configuration.check.collection.plugin")
public class CollectionCheckerPluginHandler extends PluginHandler {

  @Autowired
  private ConfigurationService configurationService;

  @Override
  public CollectionCheckerPluginData initialize(Dto pluginConfig){
    CollectionCheckerPluginData data = new CollectionCheckerPluginData();
    Collection<CollectionConfig> colConfigs =  configurationService.getConfigs(CollectionConfig.class);
    data.setCollections(colConfigs);
    data.setCollectionsCount(Long.valueOf(colConfigs.size()));
    return data;
  }

  public Dto checkCollection(Dto config){
    CollectionCheckResult requestData = (CollectionCheckResult)config;
    requestData.setSuccess(true);
    return requestData;
  }
}
