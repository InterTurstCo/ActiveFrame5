/**
 * Copyright 2000-2013 InterTrust LTD.
 *
 * All rights reserved.
 *
 * Visit our web-site: www.intertrust.ru.
 */
package ru.intertrust.cm.core.gui.impl.client.resources;

import com.google.gwt.resources.client.CssResource;

/**
 * TODO Описание (от mike-khukh)
 * 
 * @author mike-khukh
 * @since 4.1
 */
public interface ColumnSortFilterResources extends CssResource {

  @ClassName("action-pointer")
  String actionPointer();

  @ClassName("header-table")
  String headerTable();

  @ClassName("header-table-tr")
  String headerTableTr();

  @ClassName("header-table-td")
  String headerTableTd();

  @ClassName("header-div")
  String headerDiv();

  @ClassName("header-sort-div")
  String headerSortDiv();

  @ClassName("sort-ctrl-div")
  String sortCtrlDiv();

//  @ClassName("sort-ctrl-btn")
//  String sortCtrlBtn();

  /*
   * @ClassName("arrow-up-down-d") String arrowUpDownD();
   * @ClassName("arrow-up-down-a") String arrowUpDownA();
   * @ClassName("arrow-down-up-a") String arrowDownUpA();
   * @ClassName("arrow-up-d") String arrowUpD();
   * @ClassName("arrow-up-a") String arrowUpA();
   * @ClassName("arrow-down-d") String arrowDownD();
   * @ClassName("arrow-down-a") String arrowDownA();
   */

  @ClassName("search-dialog-dlg")
  String searchDialogDlg();

  /*
   * @ClassName("search-dialog-glass") String searchDialogGlass();
   */

  @ClassName("search-dialog-vpanel")
  String searchDialogVpanel();

  @ClassName("search-dialog-maintd")
  String searchDialogMaintd();

  @ClassName("search-dialog-label")
  String searchDialogLabel();

  @ClassName("search-dialog-searchbtn")
  String searchDialogSearchbtn();

  @ClassName("search-dialog-cancelbtn")
  String searchDialogCancelbtn();

  @ClassName("search-dialog-title-div")
  String searchDialogTitlediv();

  @ClassName("search-dialog-closebtn-div")
  String searchDialogClosebtndiv();

  @ClassName("filter-ctrl-suggest")
  String filterCtrlSuggest();

  @ClassName("filter-ctrl-div")
  String filterCtrlDiv();

  @ClassName("filter-ctrl-btn-a")
  String filterCtrlBtnA();

  @ClassName("filter-ctrl-btn-d")
  String filterCtrlBtnD();

  @ClassName("filter-ctrl-txt")
  String filterCtrlTxt();

  @ClassName("filter-dbx-btn")
  String filterDbxBtn();

  @ClassName("search-dbx-btn")
  String searchDbxBtn();

  @ClassName("popup-date-picker")
  String popupDatePicker();

//  @ClassName("filter-panel")
//  String filterPanel();
//
//  @ClassName("filter-panel-btn")
//  String filterPanelBtn();
}
