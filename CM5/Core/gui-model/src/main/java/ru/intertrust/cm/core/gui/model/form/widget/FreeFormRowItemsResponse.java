package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.gui.model.form.FormState;

import java.util.HashMap;
import java.util.Map;


public class FreeFormRowItemsResponse extends RowItemsResponse implements Dto {
  private Map<String,FormState> formStates;

  public FreeFormRowItemsResponse(){}


  public Map<String,FormState> getFormStates() {
    if(formStates==null){
      formStates = new HashMap<>();
    }
    return formStates;
  }

  public void setFormStates(Map<String,FormState> formStates) {
    this.formStates = formStates;
  }

}
