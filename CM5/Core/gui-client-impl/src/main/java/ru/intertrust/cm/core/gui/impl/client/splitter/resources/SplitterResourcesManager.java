/**
 * Copyright 2000-2013 InterTrust LTD.
 *
 * All rights reserved.
 *
 * Visit our web-site: www.intertrust.ru.
 */
package ru.intertrust.cm.core.gui.impl.client.splitter.resources;

import com.google.gwt.core.client.GWT;

import java.util.HashMap;
import java.util.Map;

/**
* Менеджер тем для SplitterResources. 
*
* @author Nick Gritsenko
* @since 4.2
*/
public class SplitterResourcesManager {

  private static SplitterResourcesManager  instance;
  private Map<String, SplitterResources> map = new HashMap<String, SplitterResources>();

  private SplitterResourcesManager() {
	map.put(GlobalThemesManager.THEME_DEFAULT,			
 GWT.<SplitterResources> create(SplitterResources.class));
	map.put(GlobalThemesManager.THEME_IRIDESCENT,			
 GWT.<SplitterResourcesIridescent> create(SplitterResourcesIridescent.class));
	map.put(GlobalThemesManager.THEME_BRIGHT,			
 GWT.<SplitterResourcesBright> create(SplitterResourcesBright.class));

  }

  public static SplitterResourcesManager get() {
    if (instance == null) {
      instance = new SplitterResourcesManager();
    }
    return instance;
  }

  public SplitterResources getTheme() {
    String current = GlobalThemesManager.get().getCurrentTheme();
    SplitterResources t = map.get(current);
    if (t != null) {
      return t;
    }
    return map.get(GlobalThemesManager.THEME_DEFAULT);
  }
}
