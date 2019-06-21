package ru.intertrust.cm.core.gui.model.plugin;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.base.CollectionConfig;

public class CollectionCheckResult implements Dto {
  private CollectionConfig config;
  private String exception;
  private Boolean success = false;

  public CollectionConfig getConfig() {
    return config;
  }

  public void setConfig(CollectionConfig config) {
    this.config = config;
  }

  public String getException() {
    return exception;
  }

  public void setException(String exception) {
    this.exception = exception;
  }

  public Boolean getSuccess() {
    return success;
  }

  public void setSuccess(Boolean success) {
    this.success = success;
  }
}
