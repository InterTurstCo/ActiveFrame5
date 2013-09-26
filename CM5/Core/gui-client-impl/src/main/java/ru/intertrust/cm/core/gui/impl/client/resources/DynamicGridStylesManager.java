/**
 * Copyright 2000-2013 InterTrust LTD.
 *
 * All rights reserved.
 *
 * Visit our web-site: www.intertrust.ru.
 */
package ru.intertrust.cm.core.gui.impl.client.resources;

import com.google.gwt.core.client.GWT;
import ru.intertrust.cm.core.gui.impl.client.themes.GlobalThemesManager;

import java.util.HashMap;
import java.util.Map;

/**
* Менеджер тем для DynamicGridStyles. 
*
* @author Nick Gritsenko
* @since 4.2
*/
public class DynamicGridStylesManager {

  private static DynamicGridStylesManager instance;
  private Map<String, DynamicGridStyles> map = new HashMap<String, DynamicGridStyles>();

  private DynamicGridStylesManager() {
	map.put(GlobalThemesManager.THEME_DEFAULT,
 GWT.<DynamicGridStyles> create(DynamicGridStyles.class));
	map.put(GlobalThemesManager.THEME_IRIDESCENT,			
 GWT.<DynamicGridStylesIridescent> create(DynamicGridStylesIridescent.class));
	map.put(GlobalThemesManager.THEME_BRIGHT,			
 GWT.<DynamicGridStylesBright> create(DynamicGridStylesBright.class));

  }

  public static DynamicGridStylesManager get() {
    if (instance == null) {
      instance = new DynamicGridStylesManager();
    }
    return instance;
  }

  public DynamicGridStyles getTheme() {
    String current = GlobalThemesManager.get().getCurrentTheme();
    DynamicGridStyles t = map.get(current);
    if (t != null) {
      return t;
    }
    return map.get(GlobalThemesManager.THEME_DEFAULT);
  }
}
