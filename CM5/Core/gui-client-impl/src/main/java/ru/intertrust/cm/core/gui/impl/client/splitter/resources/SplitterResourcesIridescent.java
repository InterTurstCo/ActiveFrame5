/**
 * Copyright 2000-2013 InterTrust LTD.
 *
 * All rights reserved.
 *
 * Visit our web-site: www.intertrust.ru.
 */
package ru.intertrust.cm.core.gui.impl.client.splitter.resources;

import com.google.gwt.resources.client.CssResource;

/**
 * TODO Описание (от Nick Gritsenko)
 * @author Nick Gritsenko
 * @since 4.1
 */

public interface SplitterResourcesIridescent extends SplitterResources {
  SplitterResources INST = SplitterResourcesManager.get().getTheme();

  @CssResource.NotStrict
  @Source("splitter.css")
  SplitterStyles styles();

}
