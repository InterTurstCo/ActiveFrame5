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
public interface DynamicGridStyles extends ClientBundle {

  DynamicGridStyles I = DynamicGridStylesManager.get().getTheme();

  @CssResource.NotStrict
  @Source("grid-cm4.css")
  DynamicGridResources dgStyle();

  @CssResource.NotStrict
  @Source("column-sort-filter.css")
  ColumnSortFilterResources csfStyle();
}
