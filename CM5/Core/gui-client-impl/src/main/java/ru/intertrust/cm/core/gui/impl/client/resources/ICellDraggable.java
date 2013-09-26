package ru.intertrust.cm.core.gui.impl.client.resources;

/**
 * @author lvov
 */
public interface ICellDraggable {
  /**
   * Install draggable property
   * 
   * @param isDraggable
   */
  void setDraggable(boolean isDraggable);

  /**
   * Returns draggable property
   * 
   * @return isDraggable
   */
  boolean isDraggable();

}
