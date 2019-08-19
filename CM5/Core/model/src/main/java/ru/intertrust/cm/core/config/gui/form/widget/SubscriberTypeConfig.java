package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

import java.util.ArrayList;
import java.util.List;

@Root(name = "subscriber")
public class SubscriberTypeConfig implements Dto {

  @ElementList(name = "subscribed", inline = true)
  private List<SubscribedTypeConfig> subscribedTypeConfigs;

  public List<SubscribedTypeConfig> getSubscribedTypeConfigs() {
    if(subscribedTypeConfigs == null){
      subscribedTypeConfigs = new ArrayList<>();
    }
    return subscribedTypeConfigs;
  }

  public void setSubscribedTypeConfigs(List<SubscribedTypeConfig> subscribedTypeConfigs) {
    this.subscribedTypeConfigs = subscribedTypeConfigs;
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

    SubscriberTypeConfig that = (SubscriberTypeConfig) o;

    if (subscribedTypeConfigs != null ? !subscribedTypeConfigs.equals(that.subscribedTypeConfigs) : that.subscribedTypeConfigs != null) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (subscribedTypeConfigs != null ? subscribedTypeConfigs.hashCode() : 0);
    return result;
  }
}
