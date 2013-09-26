package ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.panel;


import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.dom.client.*;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.LoadingStateChangeEvent;
import com.google.gwt.user.cellview.client.LoadingStateChangeEvent.LoadingState;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HasScrolling;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.*;
import com.google.gwt.view.client.CellPreviewEvent.Handler;
import ru.intertrust.cm.core.gui.model.plugin.MyData;

import java.util.List;

/**
 * @author mike-khukh
 * @param <T>
 */
public class CellTableEx<T> extends CellTable<T> {

  private int                 currentRow               = 0;
  private int                 currentCol               = 0;
  private int                 touchRow;
  private ListDataProvider<T> dataProvider;
  private HasScrolling scrollable;
  private int                 horizontalScrollPosition = 0;

  public CellTableEx() {
    super();
    init();
  }

  public CellTableEx(final int pageSize) {
    super(pageSize);
    init();
  }

  public CellTableEx(ProvidesKey<T> keyProvider) {
    super(keyProvider);
    init();
  }

  public CellTableEx(int pageSize, Resources resources) {
    super(pageSize, resources);
    init();
  }

  public CellTableEx(int pageSize, ProvidesKey<T> keyProvider) {
    super(pageSize, keyProvider);
    init();
  }

  public CellTableEx(final int pageSize, Resources resources, ProvidesKey<T> keyProvider) {
    super(pageSize, resources, keyProvider);
    init();
  }

  public CellTableEx(final int pageSize, Resources resources, ProvidesKey<T> keyProvider,
                     Widget loadingIndicator) {
    super(pageSize, resources, keyProvider, loadingIndicator);
    init();
  }

  @Override
  public void onAttach() {
    super.onAttach();
    scrollParent();
  }

  public ListDataProvider<T> getDataProvider() {
    return dataProvider;
  }

  public void setDataProvider(ListDataProvider<T> dataProvider) {
    this.dataProvider = dataProvider;
    dataProvider.addDataDisplay(this);
  }

  public int getCurrentRow() {
    return currentRow;
  }

  public void setCurrentRow(int currentRow) {
    setCurrentRow(currentRow, true);
  }

  @SuppressWarnings("unchecked")
  public void setCurrentRow(int currentRow, boolean bOnly) {
    this.currentRow = currentRow;
    SelectionModel<? super T> selectionModel = getSelectionModel();
    List<T> list = getDataProvider().getList();
    if (list != null && currentRow < list.size()) {
      T value = list.get(currentRow);
      if (selectionModel instanceof SingleSelectionModel) {
        selectionModel.setSelected(value, true);
      }
      else if (selectionModel instanceof MultiSelectionModel) {
        if (selectionModel.isSelected(value)
            && (((MultiSelectionModel<T>) selectionModel).getSelectedSet().size() == 1 || !bOnly)) {
          return;
        }
        if (bOnly) {
          ((MultiSelectionModel<T>) selectionModel).clear();
        }
        selectionModel.setSelected(value, true);
      }
    }
  }

  public void setCurrentColumn(int currentCol) {
    int colCount = getColumnCount();
    this.currentCol = currentCol < 0 || currentCol >= colCount ? 0 : currentCol;
  }

  public int getCurrentColumn() {
    int colCount = getColumnCount();
    return currentCol >= colCount ? 0 : currentCol;
  }

  public void setTouchRow(int row) {
    touchRow = row;
  }

  public int getTouchRow() {
    return touchRow;
  }

  public void setHorizontalScrollable(HasScrolling scrollable) {
    this.scrollable = scrollable;
  }

  public void onHorizontalScroll(int scrollPos, int viewLeft, int viewWidth) {
    this.horizontalScrollPosition = scrollPos;
    int rowIndex = getCurrentRow();
    int colIndex = getCurrentColumn();
    TableCellElement td = getTDElementByIdx(rowIndex, colIndex);
    if (!isWholeElementVisible(td, viewLeft, viewWidth)) {
      for (colIndex = getColumnCount() - 1; colIndex >= 0; colIndex--) {
        td = getTDElementByIdx(rowIndex, colIndex);
        if (isWholeElementVisible(td, viewLeft, viewWidth)) {
          setCurrentColumn(colIndex);
          break;
        }
        else if (isElementVisible(td, viewLeft, viewWidth)) {
          if (isWholeElementVisible(getTDElementByIdx(rowIndex, colIndex - 1), viewLeft, viewWidth)) {
            setCurrentColumn(colIndex - 1);
            break;
          }
          else {
            setCurrentColumn(colIndex);
            break;
          }
        }
      }
    }
  }

  @Override
  public Column<T, ?> getColumn(int col) {
    if (col < 0 || col >= getColumnCount()) {
      col = 0;
    }
    return super.getColumn(col);
  }

  private void init() {
    this.setLoadingIndicator(null);
    this.setEmptyTableWidget(null);
    this.sinkEvents(Event.ONMOUSEMOVE);
    this.addCellPreviewHandler(new CellTableEventHandler<T>());
  }

  private Element getCellParent(TableCellElement td) {
    return td != null ? td.getFirstChildElement() : null;
  }

  @Override
  protected boolean resetFocusOnCell() {
    if (getKeyboardSelectionPolicy() != KeyboardSelectionPolicy.DISABLED) {
      return super.resetFocusOnCell();
    }

    int rowIdx = getCurrentRow();
    int colIdx = getCurrentColumn();
    if (getColumnCount() > 0 && isRowWithinBounds(rowIdx)) {
      Column<T, ?> column = getColumn(colIdx);
      boolean bReset = resetFocusOnCellImpl(rowIdx, colIdx, column);
      if (!bReset) {
        setFocus(true);
      }
      return bReset;
    }
    return false;
  }

  private <C> boolean resetFocusOnCellImpl(int row, int col, Column<T, C> column) {
    Element parent = getCurrentSelectedElement();
    T value = getVisibleItem(row);
    Object key = getValueKey(value);
    C cellValue = column.getValue(value);
    Cell<C> cell = column.getCell();
    Context context = new Context(row + getPageStart(), col, key);
    return cell.resetFocus(context, parent, cellValue);
  }

  private Element getCurrentSelectedElement() {
    if (getKeyboardSelectionPolicy() != KeyboardSelectionPolicy.DISABLED) {
      return super.getKeyboardSelectedElement();
    }
    TableCellElement td = getTDElementByIdx(getCurrentRow(), getCurrentColumn());
    return getCellParent(td);
  }

  private TableCellElement getTDElementByIdx(int rowIndex, int columnIndex) {
    NodeList<TableRowElement> rows = getTableBodyElement().getRows();
    if (rowIndex >= 0 && rowIndex < rows.getLength() && getColumnCount() > 0) {
      TableRowElement tr = rows.getItem(rowIndex);
      TableCellElement td = tr.getCells().getItem(columnIndex);
      return td;
    }
    return null;
  }

  @Override
  protected void onBrowserEvent2(Event event) {
    try {
      super.onBrowserEvent2(event);
    }
    catch (Throwable e) {
      //Log.get(CellTableEx.class).log(Level.INFO, "CellTableEx::onBrowserEvent2(event)");
    }
  }

  @Override
  public void setFocus(final boolean focus) {
    if (getKeyboardSelectionPolicy() != KeyboardSelectionPolicy.DISABLED) {
      super.setFocus(focus);
      return;
    }
    final Element elem = getCurrentSelectedElement();
    if (elem != null) {
      if (focus) {
        elem.focus();
      }
      else {
        elem.blur();
      }
    }
  }

  public MyData getBeanByRowIndex(int ind) {
    return null;
  }

  @Override
  public TableSectionElement getTableBodyElement() {
    return super.getTableBodyElement();
  }

  @Override
  public TableSectionElement getTableHeadElement() {
    return super.getTableHeadElement();
  }

  @Override
  protected void onLoadingStateChanged(LoadingState state) {
    if (state == LoadingState.LOADING) {
      //showOrHide(getChildContainer(), true);
      fireEvent(new LoadingStateChangeEvent(state));
      return;
    }
    else if (state == LoadingState.LOADED && isEmpty()) {
      // Empty table.
      setWidth("100%", false);
    }
    super.onLoadingStateChanged(state);
  }

  private boolean isEmpty() {
    return dataProvider == null || dataProvider.getList() == null || dataProvider.getList().size() <= 0;
  }

  private boolean isWholeElementVisible(Element el, int viewLeft, int viewWidth) {
    if (el == null) {
      return false;
    }
    int left = el.getAbsoluteLeft();
    int width = el.getOffsetWidth();
    boolean b = viewLeft <= left && left + width <= viewLeft + viewWidth;
    return b;
  }

  private boolean isElementVisible(Element el, int viewLeft, int viewWidth) {
    if (el == null) {
      return false;
    }
    int left = el.getAbsoluteLeft();
    int width = el.getOffsetWidth();
    boolean b = viewLeft < left + width && left + width <= viewLeft + viewWidth || viewLeft <= left
        && left < viewLeft + viewWidth;
    return b;
  }

  private void scrollParent() {
    if (scrollable != null) {
      int currHorPos = scrollable.getHorizontalScrollPosition();
      int currVertPos = scrollable.getVerticalScrollPosition();
      if (currHorPos != horizontalScrollPosition) {
        scrollable.setHorizontalScrollPosition(horizontalScrollPosition);
      }
      if (currVertPos != 0) {
        scrollable.setVerticalScrollPosition(0);
      }
    }
  }

  /**
   *
   * @author mike-khukh
   * @param <T1>
   */
  private class CellTableEventHandler<T1> implements Handler<T1> {

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
        setCurrentColumn(event.getColumn());
          MyData myData = (MyData)(getDataProvider().getList().get(currentRow));
          Window.alert("selected  " + myData.getRowValues() );
        if (currentRow != CellTableEx.this.currentRow) {
          setCurrentRow(currentRow);
        }
      }
      else if ("focus".equals(nativeEvent.getType())) {
        setCurrentColumn(event.getColumn());
      }
      else if ("touchstart".equals(nativeEvent.getType())) {
        setTouchRow(event.getIndex());
      }
    }
  }
}
