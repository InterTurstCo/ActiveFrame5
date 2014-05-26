package ru.intertrust.cm.core.gui.impl.client.themes.dark;


import com.google.gwt.core.client.GWT;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.themes.BundleWrapper;
import ru.intertrust.cm.core.gui.impl.client.themes.GlobalThemesManager;
import ru.intertrust.cm.core.gui.model.ComponentName;
/**
 * @author Yaroslav Bondarchuk
 *         Date: 19.05.14
 *         Time: 17:15
 */
@ComponentName(GlobalThemesManager.THEME_DARK)
public class DarkThemeBundleWrapper implements BundleWrapper {
    private static final DarkThemeBundle themeBundle = GWT.create(DarkThemeBundle.class);

    @Override
    public Component createNew() {
        return new DarkThemeBundleWrapper();
    }

    @Override
    public String getName() {
        return GlobalThemesManager.THEME_DARK;
    }

    public  DarkThemeBundle getThemeBundle() {
        return themeBundle;
    }
}
