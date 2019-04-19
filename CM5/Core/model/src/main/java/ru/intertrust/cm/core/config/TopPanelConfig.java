package ru.intertrust.cm.core.config;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

@Root(name = "top-panel")
public class TopPanelConfig implements Dto {

  @Attribute(name = "notification-visible", required = false)
  private Boolean nvisible = true;

  @Attribute(name = "search-visible", required = false)
  private Boolean svisible = true;

  public Boolean getNvisible() {
    return nvisible;
  }

  public void setNvisible(Boolean nvisible) {
    this.nvisible = nvisible;
  }

  public Boolean getSvisible() {
    return svisible;
  }

  public void setSvisible(Boolean svisible) {
    this.svisible = svisible;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    TopPanelConfig that = (TopPanelConfig) o;

    if (nvisible != null ? !nvisible.equals(that.nvisible) : that.nvisible != null) {
      return false;
    }
    if (svisible != null ? !svisible.equals(that.svisible) : that.svisible != null) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int result =  nvisible != null ? nvisible.hashCode() : 0;
    result = 31 * result + (svisible != null ? svisible.hashCode() : 0);
    return result;
  }
}
