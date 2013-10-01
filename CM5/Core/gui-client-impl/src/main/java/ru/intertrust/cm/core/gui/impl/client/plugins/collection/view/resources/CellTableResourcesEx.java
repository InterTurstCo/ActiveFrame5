package ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.resources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.CssResource.ImportedWithPrefix;
import com.google.gwt.user.cellview.client.CellTable.Resources;
import com.google.gwt.user.cellview.client.CellTable.Style;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 01/10/13
 *         Time: 12:05 PM
 */
public interface CellTableResourcesEx extends Resources {

  public CellTableResourcesEx I = GWT.create(CellTableResourcesEx.class);

  String CSS_FILE = "CellTableCommon.css";

  @ImportedWithPrefix("extGwt-CellTable")
  public interface StyleEx extends Style {

      @ClassName("docs-common-celltable-tr-unread")
      String docsCommonCelltableTrUnread();

      @ClassName("docs-common-celltable-tr-common")
      String docsCommonCelltableTrCommon();

      @ClassName("docs-common-celltable-body")
      String docsCommonCelltableBody();

      String hover();

      @ClassName("docs-common-celltable-header-panel")
      String docsCommonCelltableHeaderPanel();

      @ClassName("docs-common-celltable-header")
      String docsCommonCelltableHeader();

      @ClassName("docs-common-ct-row-selected")
      String docsCommonCtRowSelected();

      @ClassName("docs-common-th-header-row")
      String docsCommonThHeaderRow();
  }

  @Override
  @CssResource.NotStrict
  @ClientBundle.Source( CSS_FILE )
  StyleEx cellTableStyle();
}
