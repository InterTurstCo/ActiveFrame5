/**
 * Copyright 2000-2013 InterTrust LTD.
 *
 * All rights reserved.
 *
 * Visit our web-site: www.intertrust.ru.
 */
package ru.intertrust.cm.core.gui.impl.client.resources;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;


/**
 * TODO Описание (от mike-khukh)
 * @author mike-khukh
 * @since 4.1
 */
public interface DGCellTableResources extends CellTableResourcesEx {

  String CSS_FILE = "";

  /**
   * TODO Описание (от mike-khukh)
   * @author mike-khukh
   * @since 4.1
   */
  interface TableStyle extends CellTableResourcesEx.StyleEx {
    String docsCelltableBody();

    String docsCelltableTrCommon();

    String docsCelltableTrUnread();

    String docsCelltableHeader();

    String docsCelltableHeaderPanel();

    String docsCelltableRowSelected();
  }

  @Override
  @CssResource.NotStrict
  @ClientBundle.Source({ CellTableResourcesEx.DEFAULT_CSS, CSS_FILE })
  TableStyle cellTableStyle();

}
