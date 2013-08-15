package ru.intertrust.cm.core.gui.impl.markup.cmj41.navigation.client.resouces;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ImageResource;

/**
 * @author laputski
 */
public class SystemTreeResources implements TreeResources {

  private static SystemTreeImageResource images = GWT.create(SystemTreeImageResource.class);

  @Override
  public ImageResource treeClosed() {
    return images.treeClosed();
  }

  @Override
  public ImageResource treeOpen() {
    return images.treeOpen();
  }

  @Override
  public ImageResource treeLeaf() {
    return images.treeLeaf();
  }

  @Override
  public ImageResource star() {
    return images.star();
  }

  @Override
  public ImageResource flag() {
    return images.flag();
  }

  @Override
  public ImageResource sign() {
    return images.sign();
  }

  @Override
  public ImageResource add() {
    return images.add();
  }

  @Override
  public ImageResource no() {
    return images.no();
  }

}
