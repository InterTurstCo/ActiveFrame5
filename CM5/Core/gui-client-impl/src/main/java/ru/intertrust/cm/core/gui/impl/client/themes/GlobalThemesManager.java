package ru.intertrust.cm.core.gui.impl.client.themes;

import com.google.gwt.resources.client.CssResource;
import com.google.gwt.storage.client.Storage;
import com.google.gwt.user.cellview.client.DataGrid;
import ru.intertrust.cm.core.config.ThemeConfig;
import ru.intertrust.cm.core.config.ThemesConfig;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.impl.client.themes.def.splitter.SplitterStyles;
import ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 19.05.14
 *         Time: 17:15
 */
public class GlobalThemesManager {
    private static BundleWrapper bundleWrapper;
    public static final String THEME_DEFAULT = "default-theme";
    public static final String THEME_DARK = "dark-theme";
    public static final String THEME_LIGHT = "light-theme";
    private static Map<String, ThemeConfig> themeNameImageMap;

    public static String getCurrentThemeComponentName() {
        return bundleWrapper.getName();
    }

    public static ThemeBundle getCurrentTheme() {
        return bundleWrapper.getThemeBundle();
    }

    public static void initTheme(ThemesConfig themesConfig) {
        themeNameImageMap = initThemesMap(themesConfig);
        Storage stockStore = Storage.getLocalStorageIfSupported();
        String defaultThemeComponent = findDefaultTheme(themesConfig);
        if (stockStore != null) {
            String themeComponent = stockStore.getItem(BusinessUniverseConstants.USER_THEME_NAME);
            bundleWrapper = (BundleWrapper) (themeComponent == null ? ComponentRegistry.instance.get(defaultThemeComponent)
                    : ComponentRegistry.instance.get(themeComponent));
        } else {
            bundleWrapper = ComponentRegistry.instance.get(defaultThemeComponent);
        }
        if (bundleWrapper == null) {
            bundleWrapper = ComponentRegistry.instance.get("default-theme");
        }
        bundleWrapper.getMainCss().ensureInjected();
    }

    private static String findDefaultTheme(ThemesConfig themesConfig) {
        if (themesConfig == null) {
            return THEME_DEFAULT;
        }
        List<ThemeConfig> themesConfigList = themesConfig.getThemes();
        for (ThemeConfig themeConfig : themesConfigList) {
            if (themeConfig.isDefaultTheme()) {
                return themeConfig.getComponentName();
            }
        }
        return THEME_DEFAULT;
    }

    private static Map<String, ThemeConfig> initThemesMap(ThemesConfig themesConfig) {
        if (themesConfig == null) {
            return null;
        }
        Map<String, ThemeConfig> themeNameImageMap = new LinkedHashMap<String, ThemeConfig>();
        List<ThemeConfig> themeConfigList = themesConfig.getThemes();
        for (ThemeConfig themeConfig : themeConfigList) {
            themeNameImageMap.put(themeConfig.getComponentName(), themeConfig);
        }
        return themeNameImageMap;
    }

    public static Map<String, ThemeConfig> getThemeNameImageMap() {
        return themeNameImageMap;
    }

    public static DataGrid.Resources getDataGridResources() {
        return bundleWrapper.getDataGridResources();
    }

    public static SplitterStyles getSplitterStyles() {
        return bundleWrapper.getSplitterStyles();
    }

    public static CssResource getNavigationTreeStyles() {
        return bundleWrapper.getNavigationTreeCss();
    }

    public static String getResourceFolder() {
        return bundleWrapper.getResourceFolder();
    }
}
