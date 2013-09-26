package ru.intertrust.cm.core.gui.impl.client.themes;

public class GlobalThemesManager {

  public static final String         THEME_DEFAULT = "default";

  public static final String         THEME_IRIDESCENT     = "iridescent";
  public static final String         THEME_BRIGHT  = "bright";

  private static GlobalThemesManager instance;

  public static GlobalThemesManager get() {
    if (instance == null) {
      instance = new GlobalThemesManager();
    }
    return instance;
  }

  private String themeName = THEME_DEFAULT;

  public void setTheme(String theme) {
    this.themeName = theme;
  }

  public String getCurrentTheme() {
    return themeName;
  }
}
