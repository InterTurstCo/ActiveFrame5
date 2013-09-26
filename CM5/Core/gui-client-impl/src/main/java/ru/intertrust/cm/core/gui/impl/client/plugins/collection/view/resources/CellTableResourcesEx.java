package ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.resources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource.ImportedWithPrefix;
import com.google.gwt.user.cellview.client.CellTable.Resources;
import com.google.gwt.user.cellview.client.CellTable.Style;

/**
 * @author mike-khukh
 */
public interface CellTableResourcesEx extends Resources {

  public CellTableResourcesEx I           = GWT.create(CellTableResourcesEx.class);
  String DEFAULT_CSS = "ru/intertrust/cm/core/gui/impl/client/plugins/collection/view/resources/CellTableEx.css";

  @ImportedWithPrefix("extGwt-CellTable")
  public interface StyleEx extends Style {
  }

  @Override
  @Source(DEFAULT_CSS)
  StyleEx cellTableStyle();
}
