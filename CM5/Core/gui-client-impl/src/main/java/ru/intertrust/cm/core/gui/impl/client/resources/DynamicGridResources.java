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
 * @author mike-khukh
 * @since 4.1
 */
public interface DynamicGridResources extends CssResource {
  @ClassName("loading-div")
  String loadingDiv();

  @ClassName("gridCheckBox")
  String gridCheckBox();

  @ClassName("gridCheckBoxClear")
  String gridCheckBoxClear();

  @ClassName("gridCheckBoxTd")
  String gridCheckBoxTd();

  @ClassName("gridInsertColumnDiv")
  String gridInsertColumnDiv();

  @ClassName("rootMainPanel")
  String rootMainPanel();

  @ClassName("rootPathPanel-popup")
  String rootPathPanelPopup();

  @ClassName("rootPath-popup")
  String rootPathPopup();

  @ClassName("leftRootPathView")
  String leftRootPathView();

  @ClassName("rootPathView")
  String rootPathView();
  
  @ClassName("rootPathLink")
  String rootPathLink();

  @ClassName("grid-settings-panel")
  String gridSettingsPanel();

  @ClassName("Ds_col_search_box")
  String dsColSearchBox();

  @ClassName("Ds_search_button")
  String dsSearchButton();

  @ClassName("Ds_search_checkbox")
  String dsSearchСheckbox();
  
  @ClassName("Ds_close_button")
  String dsCloseButton();
 
  @ClassName("Ds_search_result")
  String dsSearchResult();
  
  @ClassName("selectionWidgetHeader")
  String selectionWidgetHeader();
  
  @ClassName("subject_span")
  String subjectSpan();

  @ClassName("gridResizeColumnDiv")
  String gridResizeColumnDiv();

  @ClassName("gridMoveColumnDiv")
  String gridMoveColumnDiv();
}
