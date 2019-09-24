package ru.intertrust.cm.core.gui.impl.client.form.widget;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import ru.intertrust.cm.core.business.api.dto.BooleanValue;
import ru.intertrust.cm.core.business.api.dto.DateTimeValue;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.LongValue;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.dto.TimelessDateValue;
import ru.intertrust.cm.core.config.gui.form.widget.RuleTypeConfig;
import ru.intertrust.cm.core.config.gui.form.widget.RulesTypeConfig;
import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.api.client.event.WidgetBroadcastEvent;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.ListBoxState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

/**
 * @author Denis Mitavskiy
 *         Date: 15.09.13
 *         Time: 22:40
 */
@ComponentName("list-box")
public class ListBoxWidget extends BaseWidget {
  private HashMap<String, Id> idMap;
  private List<DomainObject> originalDoList = new ArrayList<>();
  private LinkedHashMap<Id, String> originalDisplayList = new LinkedHashMap<>();

  @Override
  public Component createNew() {
    return new ListBoxWidget();
  }

  @Override
  public void setValue(Object value) {
    //TODO: Implementation required
  }

  @Override
  public void disable(Boolean isDisabled) {
    //TODO: Implementation required
  }

  @Override
  public void reset() {
    ((ListBox) impl).setSelectedIndex(0);
  }

  @Override
  public void applyFilter(String value) {
    ListBoxState st = ((ListBoxState) getFullClientStateCopy());

    if (value != null && value.trim() != "") {
      st.setListValues(getFilteredList(originalDoList, originalDisplayList, value));
    } else {
      st.setListValues(originalDisplayList);
    }
    setCurrentState(st);
    reset();
    Application.getInstance().getEventBus().fireEvent(new WidgetBroadcastEvent(getContainer(),initialData.getWidgetId(),
        getContainer().hashCode()
        , getContainer().getPlugin().getView().getActionToolBar().hashCode()));
  }

  @Override
  public Object getValueTextRepresentation() {
    if (impl instanceof Label) {
      return ((Label) impl).getText();
    } else {
      return ((ListBox) impl).getSelectedItemText();
    }
  }

  public void setCurrentState(WidgetState currentState) {
    idMap = getStateHandler().setState(impl, (ListBoxState) currentState);
    if (originalDoList.isEmpty()) {
      originalDoList = ((ListBoxState) currentState).getOriginalObjects();
      originalDisplayList = ((ListBoxState) currentState).getListValues();
    }
  }

  @Override
  protected boolean isChanged() {
    final ListBoxState initialState = getInitialData();
    final Set<Id> initialValue = initialState.getSelectedIdsSet();
    final StateHandler stateHandler = new ListBoxStateHandler();
    final ListBoxState currentState = stateHandler.getState(impl, initialState, idMap);
    final Set<Id> currentValue = currentState.getSelectedIdsSet();
    return initialValue == null ? currentValue != null : !initialValue.equals(currentValue);
  }

  @Override
  protected ListBoxState createNewState() {
    final ListBoxState state = getStateHandler().getState(impl, (ListBoxState) getInitialData(), idMap);
    return state;
  }

  @Override
  public WidgetState getFullClientStateCopy() {
    if (!isEditable()) {
      return super.getFullClientStateCopy();
    }
    ListBoxState state = new ListBoxState();
    ListBoxState newState = createNewState();
    state.setSelectedIds(newState.getSelectedIds());
    ListBoxState initialState = getInitialData();
    state.setListValues(initialState.getListValues());
    state.setIdFieldPathIndexMapping(initialState.getIdFieldPathIndexMapping());
    state.setFieldPaths(initialState.getFieldPaths());
    state.setConstraints(initialState.getConstraints());
    state.setWidgetProperties(initialState.getWidgetProperties());
    return state;

  }

  @Override
  protected Widget asEditableWidget(WidgetState state) {
    ListBox listBox = new ListBox(true);
    listBox.addBlurHandler(new BlurHandler() {
      @Override
      public void onBlur(BlurEvent event) {
        validate();
      }
    });
    return listBox;
  }

  @Override
  protected Widget asNonEditableWidget(WidgetState state) {
    Label noneEditableWidget = new Label();
    noneEditableWidget.removeStyleName("gwt-Label");
    return noneEditableWidget;
  }

  private <T extends Widget> StateHandler<T> getStateHandler() {
    return (StateHandler<T>) (isEditable ? new ListBoxStateHandler() : new LabelStateHandler());
  }

  @Override
  public Object getValue() {
    if (isEditable()) {
      ListBox listBox = (ListBox) impl;
      int index = listBox.getSelectedIndex();
      if (index != -1) {
        return listBox.getValue(index);
      }
    }
    return null;
  }



  private interface StateHandler<T extends Widget> {
    ListBoxState getState(T widget, ListBoxState initialState, HashMap<String, Id> idMap);

    HashMap<String, Id> setState(T widget, ListBoxState state);
  }

  private static class LabelStateHandler implements StateHandler<Label> {
    @Override
    public ListBoxState getState(Label widget, ListBoxState initialState, HashMap<String, Id> idMap) {
      return initialState;
    }

    @Override
    public HashMap<String, Id> setState(Label widget, ListBoxState state) {
      final LinkedHashMap<Id, String> items = state.getListValues();
      List<Id> selectedIds = state.getIds();
      final StringBuilder builder = new StringBuilder();
      for (Id id : selectedIds) {
        final String itemName = items.get(id);
        builder.append(itemName).append(", ");
      }
      if (builder.length() > 0) {
        builder.setLength(builder.length() - 2);
      }
      widget.setText(builder.toString());
      return null;
    }
  }

  private static class ListBoxStateHandler implements StateHandler<ListBox> {
    @Override
    public ListBoxState getState(
        final ListBox listBox, final ListBoxState initialState, final HashMap<String, Id> idMap) {
      final ListBoxState result = new ListBoxState();
      if (listBox.getItemCount() == 0) {
        return result;
      }
      final ArrayList<ArrayList<Id>> selectedIds =
          new ArrayList<ArrayList<Id>>(initialState.getFieldPaths().length);
      for (int index = 0; index < initialState.getFieldPaths().length; index++) {
        selectedIds.add(new ArrayList<Id>());
      }
      for (int index = 0; index < listBox.getItemCount(); index++) {
        if (listBox.isItemSelected(index)) {
          final Id id = idMap.get(listBox.getValue(index));
          if (id != null) {
            selectedIds.get(initialState.getFieldPathIndex(id)).add(id);
          }
        }
      }
      result.setSelectedIds(selectedIds);
      return result;
    }

    @Override
    public HashMap<String, Id> setState(final ListBox listBox, final ListBoxState state) {
      boolean singleChoice = state.isSingleChoice();
      listBox.setMultipleSelect(!singleChoice);
      final HashSet<Id> selectedIdsSet = state.getSelectedIdsSet();
      final LinkedHashMap<Id, String> listValues = state.getListValues();

      final HashMap<String, Id> idMap = new HashMap<String, Id>(listValues.size());
      listBox.clear();
      int index = 0;
      for (Id id : listValues.keySet()) {
        String idString = id == null ? "" : id.toStringRepresentation();
        listBox.addItem(listValues.get(id), idString);
        idMap.put(idString, id);
        if (selectedIdsSet.contains(id)) {
          listBox.setItemSelected(index, true);
        }
        ++index;
      }
      return idMap;
    }
  }

}
