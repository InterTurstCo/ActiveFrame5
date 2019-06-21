package ru.intertrust.cm.core.gui.impl.server.plugin.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.ConfigurationService;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.config.base.CollectionConfig;
import ru.intertrust.cm.core.config.base.CollectionFilterConfig;
import ru.intertrust.cm.core.config.base.CollectionFilterCriteriaConfig;
import ru.intertrust.cm.core.gui.api.server.plugin.PluginHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.plugin.CollectionCheckResult;
import ru.intertrust.cm.core.gui.model.plugin.CollectionCheckerPluginData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


@ComponentName("configuration.check.collection.plugin")
public class CollectionCheckerPluginHandler extends PluginHandler {

  @Autowired
  private ConfigurationService configurationService;

  @Autowired
  private CollectionsService collectionsService;

  @Override
  public CollectionCheckerPluginData initialize(Dto pluginConfig) {
    CollectionCheckerPluginData data = new CollectionCheckerPluginData();
    Collection<CollectionConfig> colConfigs = configurationService.getConfigs(CollectionConfig.class);
    data.setCollections(colConfigs);
    data.setCollectionsCount(Long.valueOf(colConfigs.size()));
    return data;
  }

  public Dto checkCollection(Dto config) {
    List<Filter> filters = new ArrayList<>();
    CollectionCheckResult requestData = (CollectionCheckResult) config;
    try {
      collectionsService.findCollectionCount(requestData.getConfig().getName(),filters);
      requestData.setSuccess(true);
    } catch (Exception e) {
      requestData.setSuccess(false);
      requestData.setException(e.getMessage());
    }

    return requestData;
  }
}
