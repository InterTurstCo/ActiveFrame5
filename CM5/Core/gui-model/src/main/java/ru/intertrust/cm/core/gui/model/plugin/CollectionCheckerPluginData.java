package ru.intertrust.cm.core.gui.model.plugin;


import ru.intertrust.cm.core.config.base.CollectionConfig;

import java.util.ArrayList;
import java.util.Collection;

public class CollectionCheckerPluginData extends ActivePluginData {

  private Long collectionsCount = 0L;

  private Collection<CollectionConfig> collections;

  public Long getCollectionsCount() {
    return collectionsCount;
  }

  public void setCollectionsCount(Long collectionsCount) {
    this.collectionsCount = collectionsCount;
  }

  public Collection<CollectionConfig> getCollections() {
    if(collections == null){
      collections = new ArrayList<>();
    }
    return collections;
  }

  public void setCollections(Collection<CollectionConfig> collections) {
    this.collections = collections;
  }
}
