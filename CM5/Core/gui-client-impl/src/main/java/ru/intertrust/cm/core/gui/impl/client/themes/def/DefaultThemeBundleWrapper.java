package ru.intertrust.cm.core.gui.impl.client.themes.def;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.user.cellview.client.DataGrid;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.themes.BundleWrapper;
import ru.intertrust.cm.core.gui.impl.client.themes.GlobalThemesManager;
import ru.intertrust.cm.core.gui.impl.client.themes.ThemeBundle;
import ru.intertrust.cm.core.gui.impl.client.themes.def.datagrid.DataGridResources;
import ru.intertrust.cm.core.gui.impl.client.themes.def.splitter.SplitterStyles;
import ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants;
import ru.intertrust.cm.core.gui.model.ComponentName;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 19.05.14
 *         Time: 17:15
 */
@ComponentName(GlobalThemesManager.THEME_DEFAULT)
public class DefaultThemeBundleWrapper implements BundleWrapper {
    private static final DefaultThemeBundle themeBundle = GWT.create(DefaultThemeBundle.class);
    private static final DataGridResources dataGridResources = GWT.create(DataGridResources.class);

    @Override
    public Component createNew() {
        return new DefaultThemeBundleWrapper();
    }

    @Override
    public String getName() {
        return GlobalThemesManager.THEME_DEFAULT;
    }


    @Override
    public CssResource getMainCss() {
        return themeBundle.mainCss();
    }

    @Override
    public ThemeBundle getThemeBundle() {
        return themeBundle;
    }

    public DataGrid.Resources getDataGridResources() {
        return dataGridResources;
    }

    public SplitterStyles getSplitterStyles() {
        return themeBundle.splitterCss();
    }

    @Override
    public CssResource getNavigationTreeCss() {
        return themeBundle.navigationTreeCss();
    }

    @Override
    public String getResourceFolder() {
        return BusinessUniverseConstants.DEFAULT_THEME_FOLDER;
    }
}
