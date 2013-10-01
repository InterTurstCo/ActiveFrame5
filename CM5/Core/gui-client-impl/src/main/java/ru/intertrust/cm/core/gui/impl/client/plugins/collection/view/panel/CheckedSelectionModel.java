package ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.panel;

import com.google.gwt.user.client.Timer;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;
import ru.intertrust.cm.core.gui.model.plugin.CollectionData;

import java.util.Set;

public class CheckedSelectionModel<T extends CollectionData> extends MultiSelectionModel<T> {
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

  private class MultiSelectionModelEx<T1 extends CollectionData> extends MultiSelectionModel<T1> implements
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
