package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;


@Root(name = "rules")
public class RulesTypeConfig implements Dto {

  @Element(name = "hide",required = false)
  private HideRulesTypeConfig hideRulesTypeConfig;

  @Element(name = "filter",required = false)
  private FilterRulesTypeConfig filterRulesTypeConfig;

  @Element(name = "access",required = false)
  private AccessRulesTypeConfig accessRulesTypeConfig;

  @Element(name = "reset",required = false)
  private ResetRulesTypeConfig resetRulesTypeConfig;

  @Element(name = "value",required = false)
  private ValueRulesTypeConfig valueRulesTypeConfig;


  public HideRulesTypeConfig getHideRulesTypeConfig() {
    return hideRulesTypeConfig;
  }

  public void setHideRulesTypeConfig(HideRulesTypeConfig hideRulesTypeConfig) {
    this.hideRulesTypeConfig = hideRulesTypeConfig;
  }

  public FilterRulesTypeConfig getFilterRulesTypeConfig() {
    return filterRulesTypeConfig;
  }

  public void setFilterRulesTypeConfig(FilterRulesTypeConfig filterRulesTypeConfig) {
    this.filterRulesTypeConfig = filterRulesTypeConfig;
  }

  public AccessRulesTypeConfig getAccessRulesTypeConfig() {
    return accessRulesTypeConfig;
  }

  public void setAccessRulesTypeConfig(AccessRulesTypeConfig accessRulesTypeConfig) {
    this.accessRulesTypeConfig = accessRulesTypeConfig;
  }

  public ResetRulesTypeConfig getResetRulesTypeConfig() {
    return resetRulesTypeConfig;
  }

  public void setResetRulesTypeConfig(ResetRulesTypeConfig resetRulesTypeConfig) {
    this.resetRulesTypeConfig = resetRulesTypeConfig;
  }

  public ValueRulesTypeConfig getValueRulesTypeConfig() {
    return valueRulesTypeConfig;
  }

  public void setValueRulesTypeConfig(ValueRulesTypeConfig valueRulesTypeConfig) {
    this.valueRulesTypeConfig = valueRulesTypeConfig;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }

    RulesTypeConfig that = (RulesTypeConfig) o;

    if (hideRulesTypeConfig != null ? !hideRulesTypeConfig.equals(that.hideRulesTypeConfig) : that.hideRulesTypeConfig != null) {
      return false;
    }
    if (filterRulesTypeConfig != null ? !filterRulesTypeConfig.equals(that.filterRulesTypeConfig) : that.filterRulesTypeConfig != null) {
      return false;
    }
    if (accessRulesTypeConfig != null ? !accessRulesTypeConfig.equals(that.accessRulesTypeConfig) : that.accessRulesTypeConfig != null) {
      return false;
    }
    if (resetRulesTypeConfig != null ? !resetRulesTypeConfig.equals(that.resetRulesTypeConfig) : that.resetRulesTypeConfig != null) {
      return false;
    }
    if (valueRulesTypeConfig != null ? !valueRulesTypeConfig.equals(that.valueRulesTypeConfig) : that.valueRulesTypeConfig != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (hideRulesTypeConfig != null ? hideRulesTypeConfig.hashCode() : 0);
    result = 31 * result + (filterRulesTypeConfig != null ? filterRulesTypeConfig.hashCode() : 0);
    result = 31 * result + (accessRulesTypeConfig != null ? accessRulesTypeConfig.hashCode() : 0);
    result = 31 * result + (resetRulesTypeConfig != null ? resetRulesTypeConfig.hashCode() : 0);
    result = 31 * result + (valueRulesTypeConfig != null ? valueRulesTypeConfig.hashCode() : 0);
    return result;
  }
}
