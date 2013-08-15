package ru.intertrust.cm.core.gui.impl.markup.cmj41.navigation.client.resouces;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.impl.ClippedImagePrototype;

/**
 * Расширенный функционал для работы с иконками в листьях дерева.
 * 
 * @author laputski
 */
public class SystemTreeImageAdapter {
  private static final TreeResources   DEFAULT_RESOURCES = GWT.create(TreeResources.class);
  private final AbstractImagePrototype treeClosed;
  private final AbstractImagePrototype treeOpen;
  private final AbstractImagePrototype treeLeaf;

  public SystemTreeImageAdapter() {
    this(DEFAULT_RESOURCES);
  }

  public SystemTreeImageAdapter(TreeResources resources) {
    treeClosed = AbstractImagePrototype.create(resources.treeClosed());
    treeOpen = AbstractImagePrototype.create(resources.treeOpen());
    treeLeaf = AbstractImagePrototype.create(resources.treeLeaf());
  }

  public AbstractImagePrototype treeClosed() {
    return treeClosed;
  }

  public AbstractImagePrototype treeOpen() {
    return treeOpen;
  }

  public AbstractImagePrototype treeLeaf() {
    return treeLeaf;
  }

  public AbstractImagePrototype choose(String str) {
    if (str.equals("treeClosed")) {
      return new TreeImagePrototype().createImage(DEFAULT_RESOURCES.treeClosed());
    }
    else if (str.equals("star")) {
      return new TreeImagePrototype().createImage(DEFAULT_RESOURCES.star());
    }
    else if (str.equals("flag")) {
      return new TreeImagePrototype().createImage(DEFAULT_RESOURCES.flag());
    }
    else if (str.equals("sign")) {
      return new TreeImagePrototype().createImage(DEFAULT_RESOURCES.sign());
    }
    else if (str.equals("add")) {
      return new TreeImagePrototype().createImage(DEFAULT_RESOURCES.add());
    }
    else {
      return new TreeImagePrototype().createImage(DEFAULT_RESOURCES.no());
    }

  }

  class TreeImagePrototype extends AbstractImagePrototype {
    public AbstractImagePrototype createImage(ImageResource resource) {
      return new ClippedImagePrototype(resource.getSafeUri(), resource.getLeft(), resource.getTop(),
          resource.getWidth(), resource.getHeight());
    }

    @Override
    public void applyTo(Image image) {
    }

    @Override
    public Image createImage() {
      return null;
    }

  }

}
