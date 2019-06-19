package ru.intertrust.cm.core.gui.impl.client.plugins.collectionchecker;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.Window;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginView;
import ru.intertrust.cm.core.gui.impl.client.plugins.collectionchecker.event.CollectionCheckerPluginEvent;
import ru.intertrust.cm.core.gui.impl.client.plugins.collectionchecker.event.CollectionCheckerPluginEventHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.plugin.CollectionCheckerPluginData;


@ComponentName("configuration.check.collection.plugin")
public class CollectionCheckerPlugin extends Plugin implements CollectionCheckerPluginEventHandler {

  private EventBus localEventBus;
  private CollectionCheckerPluginView view;


  @Override
  public PluginView createView() {
    view = new CollectionCheckerPluginView(this, (CollectionCheckerPluginData) getInitialData());
    return view;
  }

  @Override
  public Component createNew() {
    return new CollectionCheckerPlugin();
  }


  @Override
  public EventBus getLocalEventBus() {
    return localEventBus;
  }

  public CollectionCheckerPlugin() {
    localEventBus = GWT.create(SimpleEventBus.class);
    localEventBus.addHandler(CollectionCheckerPluginEvent.TYPE, this);
  }

  @Override
  public void dispatch(CollectionCheckerPluginEvent event) {
    switch (event.getAction()) {
      case START:
        ProcessingEngine.start(view,(CollectionCheckerPluginData)getInitialData());
        break;
      case STOP:
        ProcessingEngine.stop(view);
        break;
      default:
        break;
    }
  }
}
