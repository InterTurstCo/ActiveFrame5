/**
 * Copyright 2000-2013 InterTrust LTD.
 *
 * All rights reserved.
 *
 * Visit our web-site: www.intertrust.ru.
 */
package ru.intertrust.cm.core.gui.impl.client.panel;

/**
 * TODO Описание (от mike-khukh)
 * @author mike-khukh
 * @since 4.1
 */

public enum SystemColumns {
    SELF ("self"),
    ISUNREAD ("isUnRead");

  private String columnName;

  private SystemColumns(String columnName) {
    this.columnName = columnName;
  }

  public String getColumnName() {
    return columnName;
  }

  public static boolean contains(String columnName) {
    for (SystemColumns item : SystemColumns.values()) {
      if (item.getColumnName().equals(columnName)) {
        return true;
      }
    }
    return false;
  }



}
