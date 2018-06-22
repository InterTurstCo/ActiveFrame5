package ru.intertrust.cm.core.gui.impl.server.widget;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.gui.api.server.ComponentHandler;
import ru.intertrust.cm.core.gui.model.form.widget.RepresentationRequest;

/**
 * Интерфейс описывает методы которые должен реализовать хендлер отвязаной от ДО формы, который обьявлен
 * при конфигурации формы в атрибуте alternative-saver-component
 */
public interface LinkedDomainObjectFreeFormHandler extends ComponentHandler {

  /**
   * Вернуть Dto обьект (ожидается RowItemsResponse) со списком RowItem чтобы оттобразить из в
   * табличной части LinkedDomainObjectsTable SummaryTable
   * @param representationRequest состояние кастомной формы и конфигурация SummaryTable
   * @return RepresentationRequest
   */
  Dto getRowItems(RepresentationRequest representationRequest);

}
