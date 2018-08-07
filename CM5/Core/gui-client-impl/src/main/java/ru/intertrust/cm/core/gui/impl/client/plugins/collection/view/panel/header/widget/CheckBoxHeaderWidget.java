package ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.panel.header.widget;

import java.util.List;


public class CheckBoxHeaderWidget extends HeaderWidget {

  public CheckBoxHeaderWidget() {
    init();
  }

  public void init() {
    setupJS(this);
    html = getTitleHtml();
  }

  @Override
  public boolean hasFilter() {
    return false;
  }

  @Override
  public String getId() {
    return null;
  }

  @Override
  public String getFilterValuesRepresentation() {
    return null;
  }

  @Override
  public List<String> getFilterValues() {
    return null;
  }

  @Override
  public void setFilterInputWidth(int filterWidth) {

  }

  @Override
  public void setFilterValuesRepresentation(String filterValue) {

  }

  @Override
  public String getFieldName() {
    return null;
  }

  @Override
  public boolean isShowFilter() {
    return false;
  }

  @Override
  public void setShowFilter(boolean showFilter) {

  }

  protected String getTitleHtml() {
    StringBuilder titleBuilder = new StringBuilder("<div  class=\"header-label\">");
    titleBuilder.append("<p style=\"overflow: hidden; text-overflow: ellipsis; " +
        "white-space: nowrap; position: relative; left: -4px\">");
    titleBuilder.append("<input type=\"checkbox\" id=\"chkall\" onclick=\"action(this);\"");
    titleBuilder.append("</p></div>");
    return titleBuilder.toString();
  }


  public native void setupJS(CheckBoxHeaderWidget inst) /*-{
      $wnd.action = function (cbox) {
          var checkboxes = $doc.querySelectorAll(".collection-plugin-view-container input[type='checkbox']:not([id])");
          if (cbox.checked) {
              checkboxes.forEach(function (checkbox) {
                  if (checkbox.checked != true) {
                      checkbox.click();
                  }
              });
          } else {
              checkboxes.forEach(function (checkbox) {
                  if (checkbox.checked==true){
                  checkbox.click();
                  }
              });
          }
      }
  }-*/;
}
