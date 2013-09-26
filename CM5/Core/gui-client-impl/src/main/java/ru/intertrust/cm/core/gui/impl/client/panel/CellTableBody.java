/**
 * Copyright 2000-2013 InterTrust LTD.
 *
 * All rights reserved.
 *
 * Visit our web-site: www.intertrust.ru.
 */
package ru.intertrust.cm.core.gui.impl.client.panel;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.cellview.client.RowStyles;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.view.client.ListDataProvider;
import ru.intertrust.cm.core.gui.impl.client.resources.DGCellTableResourceAdapter;

import java.util.List;

public class CellTableBody<T extends MyData> extends CellTableEx<T> {
   private final CheckedSelectionModel<T> selectionModel = new CheckedSelectionModel<T>();

 public CellTableBody(DGCellTableResourceAdapter adapter) {
    super(999, adapter.getResources());
    initialize(adapter);
  }


  @Override
  public MyData getBeanByRowIndex(int ind) {
    return getDataProvider().getList().get(ind);
  }

    @Override
    public void setRowData(int start, List<? extends T> values) {
        super.setRowData(start, values);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public void setData(List<T> values) {
        getDataProvider().setList((List<T>) values);
    }

  private void initialize(final DGCellTableResourceAdapter adapter) {
      setStyleName(adapter.getResources().cellTableStyle().docsCelltableBody());
      String emptyTableText = null;



    HTML emptyTableWidget = new HTML("<br/><div align='center'> <h1> " + emptyTableText + " </h1> </div>");
    emptyTableWidget.getElement().getStyle().setPaddingLeft(60, Unit.PX);

    setRowStyles(new RowStyles<T>() {
      @Override
      public String getStyleNames(T row, int rowIndex) {
        String style = adapter.getResources().cellTableStyle().docsCelltableTrCommon();
        if (row != null) {
          Boolean value = row.getValueByKey(SystemColumns.ISUNREAD.getColumnName(), Boolean.class);
          if (value != null && value.booleanValue()) {
            style = adapter.getResources().cellTableStyle().docsCelltableTrUnread();
          }
        }
        return style;
      }
    });


    getTableBodyElement().setId("grid-table-id");

    setEmptyTableWidget(emptyTableWidget);

    setDataProvider(new ListDataProvider<T>());
    setKeyboardPagingPolicy(KeyboardPagingPolicy.CURRENT_PAGE);
    setKeyboardSelectionPolicy(KeyboardSelectionPolicy.ENABLED);
    setSelectionModel(selectionModel);
    setTableLayoutFixed(true);
  }
}
