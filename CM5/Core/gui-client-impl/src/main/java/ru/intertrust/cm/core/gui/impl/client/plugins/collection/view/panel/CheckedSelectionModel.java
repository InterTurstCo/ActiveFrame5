package ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.panel;

import java.util.Set;
import com.google.gwt.user.client.Timer;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

import ru.intertrust.cm.core.gui.model.plugin.CollectionRowItem;

public class CheckedSelectionModel<T extends CollectionRowItem> extends SingleSelectionModel<T> {
  private static final int         SELECTION_DELAY   = 500;

  private MultiSelectionModelEx<T> outerModel;

  public CheckedSelectionModel() {
    outerModel = new MultiSelectionModelEx<T>(this);
  }

  @Override
  public Set<T> getSelectedSet() {
    return super.getSelectedSet();
  }

  @Override
  public boolean isSelected(T object) {
    return super.isSelected(object);
  }

  @Override
  public void setSelected(T object, boolean selected) {
    if (object != null) {
      super.setSelected(object, selected);
    }
  }



  @Override
  public void fireSelectionChangeEvent() {
    super.fireSelectionChangeEvent();

  }

  private class MultiSelectionModelEx<T1 extends CollectionRowItem> extends MultiSelectionModel<T1> implements
      SelectionChangeEvent.Handler {
    private Timer selectionTimer = new SelectionTimer();

    public MultiSelectionModelEx(CheckedSelectionModel<T1> sourceModel) {
      sourceModel.addSelectionChangeHandler(this);
    }

    @Override
    public void onSelectionChange(SelectionChangeEvent event) {

      selectionTimer.cancel();
      selectionTimer.schedule(SELECTION_DELAY);
    }

    private void onSelectionChangeScheduled() {

      SelectionChangeEvent.fire(MultiSelectionModelEx.this);
    }

    private class SelectionTimer extends Timer {
      @Override
      public void run() {
        onSelectionChangeScheduled();
      }
    }
  }
}
