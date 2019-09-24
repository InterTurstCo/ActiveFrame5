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
import ru.intertrust.cm.core.gui.model.form.widget.ComboBoxState;
import ru.intertrust.cm.core.gui.model.form.widget.ListBoxState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Denis Mitavskiy
 *         Date: 15.09.13
 *         Time: 22:40
 */
@ComponentName("combo-box")
public class ComboBoxWidget extends BaseWidget {
  private HashMap<String, Id> idMap;
  private Id nonEditableId;
  private List<DomainObject> originalDoList = new ArrayList<>();
  private LinkedHashMap<Id, String> originalDisplayList = new LinkedHashMap<>();

  @Override
  public Component createNew() {
    return new ComboBoxWidget();
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
    ComboBoxState st = ((ComboBoxState) getFullClientStateCopy());

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
  public void setCurrentState(WidgetState currentState) {
    ComboBoxState comboBoxState = (ComboBoxState) currentState;
    Id selectedId = comboBoxState.getSelectedId();
    Map<Id, String> listValues = comboBoxState.getListValues();
    if (!isEditable()) {
      if (selectedId != null) {
        nonEditableId = selectedId;
        ((Label) impl).setText(listValues.get(selectedId));
      }
      return;
    }
    idMap = new HashMap<String, Id>(listValues.size());
    ListBox listBox = (ListBox) impl;
    listBox.clear();
    int index = 0;
    for (Id id : listValues.keySet()) {
      String idString = id == null ? "" : id.toStringRepresentation();
      listBox.addItem(listValues.get(id), idString);
      idMap.put(idString, id);
      if (id == null && selectedId == null || id != null && id.equals(selectedId)) {
        listBox.setSelectedIndex(index);
      }
      ++index;
    }
    if (originalDoList.isEmpty()) {
      originalDoList = ((ComboBoxState) currentState).getOriginalObjects();
      originalDisplayList = ((ComboBoxState) currentState).getListValues();
    }
  }

  @Override
  protected boolean isChanged() {
    final ListBox listBox = (ListBox) impl;
    final Id selectedId;
    if (listBox.getItemCount() == 0) {
      selectedId = null;
    } else {
      selectedId = idMap.get(listBox.getValue(listBox.getSelectedIndex()));
    }
    final ComboBoxState state = getInitialData();
    return selectedId == null ? state.getSelectedId() != null : !selectedId.equals(state.getSelectedId());
  }

  @Override
  protected ComboBoxState createNewState() {
    ComboBoxState state = new ComboBoxState();
    if (!isEditable()) {
      state.setSelectedId(nonEditableId);
      return state;
    }
    ListBox listBox = (ListBox) impl;
    if (listBox.getItemCount() == 0) {
      return state;
    }
    state.setSelectedId(idMap.get(listBox.getValue(listBox.getSelectedIndex())));
    return state;
  }

  @Override
  public WidgetState getFullClientStateCopy() {
    if (!isEditable()) {
      return super.getFullClientStateCopy();
    }
    ComboBoxState stateWithSelectedIds = createNewState();
    ComboBoxState fullClientState = new ComboBoxState();
    fullClientState.setSelectedId(stateWithSelectedIds.getSelectedId());
    ComboBoxState initialState = getInitialData();
    fullClientState.setListValues(initialState.getListValues());
    fullClientState.setConstraints(initialState.getConstraints());
    fullClientState.setWidgetProperties(initialState.getWidgetProperties());
    return fullClientState;
  }

  @Override
  protected Widget asEditableWidget(WidgetState state) {
    ListBox listBox = new ListBox(false);
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

  @Override
  public Object getValueTextRepresentation() {
    if (impl instanceof Label) {
      return ((Label) impl).getText();
    } else {
      return ((ListBox) impl).getSelectedItemText();
    }
  }
}
