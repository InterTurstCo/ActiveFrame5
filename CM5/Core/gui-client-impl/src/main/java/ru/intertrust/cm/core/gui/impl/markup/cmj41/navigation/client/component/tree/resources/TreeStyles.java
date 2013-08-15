package ru.intertrust.cm.core.gui.impl.markup.cmj41.navigation.client.component.tree.resources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;

public interface TreeStyles extends ClientBundle {
  TreeStyles I = GWT.create(TreeStyles.class);

  @Source("tree.css")
  TreeResources treeStyle();

}
