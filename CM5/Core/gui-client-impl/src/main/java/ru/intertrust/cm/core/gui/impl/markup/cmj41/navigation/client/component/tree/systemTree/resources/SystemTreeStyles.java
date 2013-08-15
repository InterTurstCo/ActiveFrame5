/*
 * Copyright 2011-2012 InterTrust LTD. All rights reserved. Visit our web-site: www.intertrust.ru.
 */
package ru.intertrust.cm.core.gui.impl.markup.cmj41.navigation.client.component.tree.systemTree.resources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource.NotStrict;

/**
 * CellStyles Resources used by the cell generator
 * 
 * @author alex oreshkevich
 */
/**  */
public interface SystemTreeStyles extends ClientBundle {

  SystemTreeStyles I = GWT.create(SystemTreeStyles.class);

  @NotStrict
  @Source("systemTree.css")
  public Style styles();

}
