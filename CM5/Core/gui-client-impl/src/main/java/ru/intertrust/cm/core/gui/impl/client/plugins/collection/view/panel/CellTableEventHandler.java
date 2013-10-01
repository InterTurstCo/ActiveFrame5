package ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.panel;

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.view.client.CellPreviewEvent;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.event.CollectionRowSelectedEvent;
import ru.intertrust.cm.core.gui.model.plugin.CollectionPluginData;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 17/9/13
 *         Time: 12:05 PM
 */
public class CellTableEventHandler<T1> implements CellPreviewEvent.Handler<T1> {
    public CellTable cellTableEx;
    private Plugin plugin;

    public CellTableEventHandler(CellTable cellTableEx, Plugin plugin) {
        this.cellTableEx = cellTableEx;
        this.plugin = plugin;
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
            plugin.getEventBus().fireEvent(new CollectionRowSelectedEvent(pluginData.
                    getCollection().get(currentRow).getId()));
        }
    }

  }
}
