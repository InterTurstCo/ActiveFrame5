package ru.intertrust.cm.core.gui.impl.client.panel;

import com.google.gwt.user.client.Timer;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;

import java.util.Set;

public class CheckedSelectionModel<T extends MyData> extends MultiSelectionModel<T> {
  private static final int         SELECTION_DELAY   = 500;
//  private HashMap<String, T>       checkedSet        = new HashMap<String, T>();
//  private Set<String>              checkedSelfURLSet = new HashSet<String>();
//  private Set<String>              checkedDocURLSet  = new HashSet<String>();
  private MultiSelectionModelEx<T> outerModel;

  public CheckedSelectionModel() {
    outerModel = new MultiSelectionModelEx<T>(this);
  }

//  public MultiSelectionModel<T> getOuterModel() {
//    return outerModel;
//  }

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
    // SelectionChangeEvent.fire(this);
  }

  private class MultiSelectionModelEx<T1 extends MyData> extends MultiSelectionModel<T1> implements
      SelectionChangeEvent.Handler {
    private Timer selectionTimer = new SelectionTimer();

    public MultiSelectionModelEx(CheckedSelectionModel<T1> sourceModel) {
      sourceModel.addSelectionChangeHandler(this);
    }

    @Override
    public void onSelectionChange(SelectionChangeEvent event) {
      // clear();
      selectionTimer.cancel();
      selectionTimer.schedule(SELECTION_DELAY);
    }

    private void onSelectionChangeScheduled() {
      // to force onSelectionChanged if user navigates checked nodes
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
