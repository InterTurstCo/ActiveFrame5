package ru.intertrust.cm.core.gui.impl.markup.cmj41.navigation.client.resouces;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

/**
 * @author laputski
 */
public interface SystemTreeImageResource extends ClientBundle {

  @Source(value = { "no.png" })
  ImageResource no();

  @Source(value = { "treeClosed.png" })
  ImageResource treeClosed();

  @Source(value = { "treeOpen.png" })
  ImageResource treeOpen();

  @Source(value = { "treeLeaf.png" })
  ImageResource treeLeaf();

  @Source(value = { "star.png" })
  ImageResource star();

  @Source(value = { "flag.png" })
  ImageResource flag();

  @Source(value = { "sign.png" })
  ImageResource sign();

  @Source(value = { "add.png" })
  ImageResource add();
}
