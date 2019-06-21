package ru.intertrust.cm.core.gui.impl.client.plugins.collectionchecker;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginView;
import ru.intertrust.cm.core.gui.impl.client.form.widget.buttons.ConfiguredButton;
import ru.intertrust.cm.core.gui.impl.client.plugins.collectionchecker.event.CollectionCheckerPluginEvent;
import ru.intertrust.cm.core.gui.impl.client.plugins.collectionchecker.event.PluginActions;
import ru.intertrust.cm.core.gui.impl.client.plugins.globalcache.GlobalCacheControlUtils;
import ru.intertrust.cm.core.gui.model.plugin.CollectionCheckerPluginData;


public class CollectionCheckerPluginView extends PluginView {

  private AbsolutePanel rootContainer;
  private TabPanel tabPanel;
  private AbsolutePanel reportsPanel;
  private Grid shortStatGrid;
  private HorizontalPanel shortStatPanel;
  private Panel buttons;
  private TextArea area;
  private CollectionCheckerPlugin parent;
  private CollectionCheckerPluginData pData;


  @Override
  public IsWidget getViewWidget() {
    IsWidget isWidget = buildLayout();
    return isWidget;
  }

  public CollectionCheckerPluginView(Plugin p, CollectionCheckerPluginData pluginData) {
    super(p);
    parent = (CollectionCheckerPlugin)p;
    pData = pluginData;
  }

  public IsWidget buildLayout() {
    area = new TextArea();
    area.setStyleName("collectionCheckTextArea");
    rootContainer = new AbsolutePanel();
    tabPanel = new TabPanel();
    buttons = new HorizontalPanel();
    buttons.addStyleName(GlobalCacheControlUtils.STYLE_TOP_MENU_BUTTONS);
    buttons.add(buildStartButton());
    buttons.add(buildStopButton());
    rootContainer.add(buttons);

    buildShortStatPanel();

    tabPanel.add(shortStatPanel, "Проверка коллекций");

    tabPanel.selectTab(0);
    tabPanel.getWidget(0).getParent().getElement().getParentElement()
        .addClassName("gwt-TabLayoutPanel-wrapper");
    rootContainer.add(tabPanel);
    area.setReadOnly(true);
    rootContainer.add(area);
    return rootContainer;
  }

  private static native void actuator() /*-{
      txt = $doc.querySelector(".collectionCheckTextArea");
      txt.scrollTo({
          top: txt.scrollHeight,
          behavior: "smooth"
      });
  }-*/;

  private void buildShortStatPanel() {

    shortStatGrid = new Grid(1, 2);
    shortStatPanel = new HorizontalPanel();
    shortStatGrid.clear();
    shortStatGrid.setStyleName("shortStatGrid");
    shortStatGrid.setWidget(0, 0, new Label("Зарегистрировано коллекций"));
    shortStatGrid.setWidget(0, 1, new Label(pData.getCollectionsCount().toString()));
    shortStatPanel.add(shortStatGrid);

  }


  private Widget buildStartButton() {
    ConfiguredButton applyButton = GlobalCacheControlUtils.createButton(CollectionCheckerUtils.COL_START, CollectionCheckerUtils.BTN_IMG_START);
    applyButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        parent.getLocalEventBus().fireEvent(new CollectionCheckerPluginEvent(PluginActions.START));
      }
    });
    return applyButton;
  }

  private Widget buildStopButton() {
    ConfiguredButton applyButton = GlobalCacheControlUtils.createButton(CollectionCheckerUtils.COL_STOP, CollectionCheckerUtils.BTN_IMG_STOP);
    applyButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        parent.getLocalEventBus().fireEvent(new CollectionCheckerPluginEvent(PluginActions.STOP));
      }
    });
    return applyButton;
  }

  public void clearConsole(){
    area.setValue(null);
  }

  public void putMessage(String msg){
    area.setText(area.getValue()+msg+"\n");
    actuator();
  }

  public void addMessage(String msg){
    area.setText(area.getValue()+msg);
    actuator();
  }
}
