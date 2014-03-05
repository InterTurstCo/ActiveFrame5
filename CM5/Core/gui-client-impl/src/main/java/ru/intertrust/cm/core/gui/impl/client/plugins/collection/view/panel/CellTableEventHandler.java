package ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.panel;

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.event.CollectionRowSelectedEvent;
import ru.intertrust.cm.core.gui.model.plugin.CollectionPluginData;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 17/9/13
 *         Time: 12:05 PM
 */
public class CellTableEventHandler<T1> implements CellPreviewEvent.Handler<T1> {
    public DataGrid dataGrid;
    protected Plugin plugin;
    private EventBus eventBus;

    public CellTableEventHandler(DataGrid dataGrid, Plugin plugin, EventBus eventBus) {
        this.dataGrid = dataGrid;
        this.plugin = plugin;
        this.eventBus = eventBus;
    }

    /**
   * Обработка события {@link com.google.gwt.view.client.CellPreviewEvent}.
   * Обрабатываются события типа:
   * "click" - устанавливается текущая строка и колонка таблицы;
   * "focus" - устанавливается текущая колонка;
   * "touchstart" - запоминается номер строки, которой коснулся пользователь.
   *
   * @param event
   *          Экземпляр {@link com.google.gwt.view.client.CellPreviewEvent}
   */
  @Override
  public void onCellPreview(CellPreviewEvent<T1> event) {
    NativeEvent nativeEvent = event.getNativeEvent();
    if ("click".equals(nativeEvent.getType())) {
      int currentRow = event.getIndex();

        if (plugin != null) {
            CollectionPluginData pluginData = plugin.getInitialData();
            eventBus.fireEvent(new CollectionRowSelectedEvent(pluginData.
                    getItems().get(currentRow).getId()));
        }
    }

  }
}
