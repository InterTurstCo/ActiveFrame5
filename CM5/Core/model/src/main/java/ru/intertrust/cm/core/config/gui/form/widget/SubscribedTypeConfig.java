package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

@Root(name = "subscribed")
public class SubscribedTypeConfig implements Dto {

  @Attribute(name = "to-id")
  private String toId;


  public String getToId() {
    return toId;
  }

  public void setToId(String toId) {
    this.toId = toId;
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

    SubscribedTypeConfig that = (SubscribedTypeConfig) o;

    if (toId != null ? !toId.equals(that.toId) : that.toId != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (toId != null ? toId.hashCode() : 0);
    return result;
  }
}
