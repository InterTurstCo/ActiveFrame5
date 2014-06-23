/**
 * Copyright 2000-2013 InterTrust LTD.
 *
 * All rights reserved.
 *
 * Visit our web-site: www.intertrust.ru.
 */
package ru.intertrust.cm.core.gui.impl.client.themes.light.splitter;

import com.google.gwt.resources.client.CssResource;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 19.05.14
 *         Time: 17:15
 */

public interface SplitterStyles extends CssResource {

  @ClassName("gwt-splitter-shadow")
  String spliterShadow();

  @ClassName("gwt-SplitLayoutPanel-VDragger")
  String verticalBar();

  @ClassName("gwt-SplitLayoutPanel-VDraggerTop")
  String verticalBarTop();

  @ClassName("gwt-SplitLayoutPanel-VDraggerBottom")
  String verticalBarBottom();

  @ClassName("gwt-SplitLayoutPanel-HDragger")
  String horizontalBar();

  @ClassName("gwt-SplitLayoutPanel-HDraggerLeft")
  String horizontalBarLeft();

  @ClassName("gwt-SplitLayoutPanel-HDraggerRight")
  String horizontalBarRight();

  @ClassName("splitter-arrow")
  String splitterArrow();

  @ClassName("left-arrow-vert")
  String leftArrowVert();

  @ClassName("left-arrow-horiz")
  String leftArrowHoriz();

  @ClassName("right-arrow-vert")
  String rightArrowVert();

  @ClassName("right-arrow-horiz")
  String rightArrowHoriz();

  @ClassName("central-panel-horiz")
  String centralPanelHoriz();

  @ClassName("central-panel-horiz-top")
  String centralPanelHorizTop();

  @ClassName("central-panel-horiz-bottom")
  String centralPanelHorizBottom();

  @ClassName("change-mode-horiz")
  String changeModeHoriz();

  @ClassName("change-mode-horiz-top")
  String changeModeHorizTop();

  @ClassName("change-mode-horiz-bottom")
  String changeModeHorizBottom();

  @ClassName("change-mode-horiz-button")
  String changeModeHorizButton();

  @ClassName("central-panel-vert")
  String centralPanelVert();

  @ClassName("central-panel-vert-left")
  String centralPanelVertLeft();

  @ClassName("central-panel-vert-right")
  String centralPanelVertRight();

  @ClassName("change-mode-vert")
  String changeModeVert();

  @ClassName("change-mode-vert-left")
  String changeModeVertLeft();

  @ClassName("change-mode-vert-right")
  String changeModeVertRight();

  @ClassName("change-mode-vert-button")
  String changeModeVertButton();

  @ClassName("central-panel-horiz-dots")
  String centralPanelHorizDots();

  @ClassName("central-panel-vert-dots")
  String centralPanelVertDots();

  @ClassName("touch-dummy-button")
  String touchDummyButton();

  @ClassName("touch-dummy-splitter")
  String touchDummySplitter();


}
