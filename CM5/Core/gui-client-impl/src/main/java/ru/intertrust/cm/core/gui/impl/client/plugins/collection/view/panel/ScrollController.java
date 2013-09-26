/**
 * Copyright 2000-2013 InterTrust LTD.
 *
 * All rights reserved.
 *
 * Visit our web-site: www.intertrust.ru.
 */
package ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.panel;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * TODO Описание (от mike-khukh)
 * @author mike-khukh
 * @since 4.1
 */

public class ScrollController extends ScrollPanel {

  public static int   SCROLL_WIDTH = 18;

  private SimplePanel simplePanel;
  private int         scrollPosition;

  // private int height;

  public ScrollController() {
    setSize("" + SCROLL_WIDTH + "px", "100%");
    simplePanel = new SimplePanel();
    setTotalHeight(0);
    setWidget(simplePanel);
  }

  public void setTotalHeight(int height) {
    if (height != getTotalHeight()) {
      simplePanel.setPixelSize(1, height);
    }
  }

  public int getTotalHeight() {
    return simplePanel.getOffsetHeight();
  }


  @Override
  public void setVerticalScrollPosition(final int position) {
    scrollPosition = position;
    super.setVerticalScrollPosition(scrollPosition);
  }

  private void correctPosition() {
    getElement().getParentElement().getStyle().clearLeft();
    getElement().getParentElement().getStyle().setRight(0, Unit.PX);
    getElement().getParentElement().getStyle().setWidth(ScrollController.SCROLL_WIDTH, Unit.PX);
  }

  @Override
  public void onAttach() {
    super.onAttach();
    correctPosition();
    super.setVerticalScrollPosition(scrollPosition);
  }

  @Override
  public void onDetach() {
    super.onDetach();
  }

  @Override
  public Widget asWidget() {
    return this;
  }
}
