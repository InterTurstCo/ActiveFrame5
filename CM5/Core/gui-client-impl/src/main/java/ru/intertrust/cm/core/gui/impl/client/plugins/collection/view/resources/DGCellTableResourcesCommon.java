/**
 * Copyright 2000-2013 InterTrust LTD.
 *
 * All rights reserved.
 *
 * Visit our web-site: www.intertrust.ru.
 */
package ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.resources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;

/**
 * TODO Описание (от mike-khukh)
 * @author mike-khukh
 * @since 4.1
 */
public interface DGCellTableResourcesCommon extends DGCellTableResources {

    String                      CSS_FILE = "CellTableCommon.css";
    DGCellTableResources        I        = GWT.create(DGCellTableResourcesCommon.class);

    /** The styles applied to the table */
    @CssResource.ImportedWithPrefix(value = "docs-common")
    interface TableStyleCommon extends TableStyle {
        @Override
        @ClassName("docs-common-celltable-body")
        String docsCelltableBody();

        @Override
        @ClassName("docs-common-celltable-tr-common")
        String docsCelltableTrCommon();

        @Override
        @ClassName("docs-common-celltable-tr-unread")
        String docsCelltableTrUnread();

        @Override
        @ClassName("docs-common-celltable-header")
        String docsCelltableHeader();

        @Override
        @ClassName("docs-common-celltable-header-panel")
        String docsCelltableHeaderPanel();

        @Override
        @ClassName("docs-common-ct-row-selected")
        String docsCelltableRowSelected();

        @ClassName("docs-common-th-header-row")
        String docsCommonThHeaderRow();

        String hover();
    }

    @Override
    @CssResource.NotStrict
    @Source({ CellTableResourcesEx.DEFAULT_CSS, CSS_FILE })
    TableStyleCommon cellTableStyle();

}
