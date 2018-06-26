package ru.intertrust.cm.core.gui.impl.server.form;

import jdk.nashorn.internal.ir.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.gui.form.widget.SummaryTableColumnConfig;
import ru.intertrust.cm.core.config.gui.form.widget.SummaryTableConfig;
import ru.intertrust.cm.core.gui.impl.server.widget.LinkedDomainObjectFreeFormHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.FieldPath;
import ru.intertrust.cm.core.gui.model.form.FormObjects;
import ru.intertrust.cm.core.gui.model.form.FormState;
import ru.intertrust.cm.core.gui.model.form.ObjectsNode;
import ru.intertrust.cm.core.gui.model.form.SingleObjectNode;
import ru.intertrust.cm.core.gui.model.form.widget.EnumBoxState;
import ru.intertrust.cm.core.gui.model.form.widget.FreeFormRowItemsResponse;
import ru.intertrust.cm.core.gui.model.form.widget.LabelState;
import ru.intertrust.cm.core.gui.model.form.widget.RepresentationRequest;
import ru.intertrust.cm.core.gui.model.form.widget.RowItem;
import ru.intertrust.cm.core.gui.model.form.widget.RowItemsResponse;
import ru.intertrust.cm.core.gui.model.form.widget.TextState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

@ComponentName("TestFreeFormHandler")
public class FreeFormHandler implements LinkedDomainObjectFreeFormHandler {

  @Autowired
  CrudService crudService;



  /**
   * Метод должен обеспечить генерацию обьектов типа RowItem для их отображения в таблице
   * виджета LinkedDomainObjectsTable, а также вернуть обьект FormState для каждой
   * записи RowItem
   * @param representationRequest состояние кастомной формы и конфигурация SummaryTable
   * @return
   */
  @Override
  public Dto getRowItemsAndFormStates(RepresentationRequest representationRequest) {
    SummaryTableConfig summaryTableConfig = representationRequest.getSummaryTableConfig();
    // Сосотояние всех виджетов, используем их, реализуем любую бизнес-логику
    FormState myFormState = representationRequest.getCreatedObjectState();
    String sampleText = ((TextState)myFormState.getFullWidgetsState().get("textBox")).getText();
    int repeat = Integer.valueOf(((EnumBoxState)myFormState.getFullWidgetsState().
        get("priority")).getDisplayTextToValue().get(((EnumBoxState)myFormState.getFullWidgetsState().
        get("priority")).getSelectedText()).toString());

    FreeFormRowItemsResponse response = new FreeFormRowItemsResponse();
    for(int i=0;i<repeat;i++) {
      RowItem item = new RowItem();
      item.setDomainObjectType(representationRequest.getCreatedObjectState().getRootDomainObjectType());

      for (SummaryTableColumnConfig summaryTableColumnConfig : summaryTableConfig.getSummaryTableColumnConfigList()) {
        item.setValueByKey(summaryTableColumnConfig.getColumnId(), sampleText+i
            + summaryTableColumnConfig.getColumnId());
      }
       response.getRowItemsMap().put("Some_Unique_Key"+i, item);
      response.getFormStates().put("Some_Unique_Key"+i,
          createFormState(representationRequest.getRealFormName(),sampleText+"_"+i));
    }


    return response;
  }

  /**
   * Здесь создается FormState для каждого RowItem
   * параметры зависят от сложности формы. Это просто пример
   * Параметры ДО тип и прочее также задаются здесь т.к. хендлер обслуживает создание конкретного
   * типа ДО
   */
  private FormState createFormState(String formName, String department){
    FormState state = new FormState();
    //Имя формы которое должно открываться для данного ДО в норме (default form) или какаято другая
    state.setName(formName);

    //Созданый доменный обьект (но не сохраненный, сохраняется он потом)
    DomainObject domainObject = crudService.createDomainObject("Department");
    List<DomainObject> organizations = crudService.findAll("Organization");
    domainObject.setReference("organization",organizations.get(0));
    FormObjects formObjects = new FormObjects();
    ObjectsNode objectsNode = new SingleObjectNode(domainObject);
    formObjects.setNode(new FieldPath(""),objectsNode);
    state.setObjects(formObjects);

    //Состояния виджетов находящихся на форме
    state.setWidgetStateMap(new HashMap<String, WidgetState>());
    state.setWidgetState("Name-Label", new LabelState("Название"));
    state.setWidgetState("Name",new TextState(department,false));


    return state;
  }
}
