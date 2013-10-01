/**
 * Copyright 2000-2013 InterTrust LTD.
 *
 * All rights reserved.
 *
 * Visit our web-site: www.intertrust.ru.
 */
package ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.resources;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 30/9/13
 *         Time: 12:05 PM
 */
public class DGCellTableResourceAdapter {

  private CellTableResourcesEx resources;

  public DGCellTableResourceAdapter(CellTableResourcesEx resources) {
    this.resources = resources;
  }

  public CellTableResourcesEx getResources() {
    return resources;
  }

}
