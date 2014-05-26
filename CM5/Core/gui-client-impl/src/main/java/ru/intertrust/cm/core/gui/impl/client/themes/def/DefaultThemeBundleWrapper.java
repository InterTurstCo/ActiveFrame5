package ru.intertrust.cm.core.gui.impl.client.themes.def;

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
@ComponentName(GlobalThemesManager.THEME_DEFAULT)
public class DefaultThemeBundleWrapper implements BundleWrapper {
    private static final DefaultThemeBundle themeBundle = GWT.create(DefaultThemeBundle.class);
    @Override
    public Component createNew() {
        return new DefaultThemeBundleWrapper();
    }
    @Override
    public String getName() {
        return GlobalThemesManager.THEME_DEFAULT;
    }

    public  DefaultThemeBundle getThemeBundle() {
        return themeBundle;
    }
}
