package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

@Root(name = "events")
public class EventsTypeConfig implements Dto {

  @Element(name = "subscriber")
  SubscriberTypeConfig subscriberTypeConfig;

  public SubscriberTypeConfig getSubscriberTypeConfig() {
    return subscriberTypeConfig;
  }

  public void setSubscriberTypeConfig(SubscriberTypeConfig subscriberTypeConfig) {
    this.subscriberTypeConfig = subscriberTypeConfig;
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

    EventsTypeConfig that = (EventsTypeConfig) o;

    if (subscriberTypeConfig != null ? !subscriberTypeConfig.equals(that.subscriberTypeConfig) : that.subscriberTypeConfig != null) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (subscriberTypeConfig != null ? subscriberTypeConfig.hashCode() : 0);
    return result;
  }
}
