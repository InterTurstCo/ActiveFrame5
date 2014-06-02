package ru.intertrust.cm.core.gui.impl.client.themes.dark;


import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.user.cellview.client.DataGrid;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.themes.BundleWrapper;
import ru.intertrust.cm.core.gui.impl.client.themes.GlobalThemesManager;
import ru.intertrust.cm.core.gui.impl.client.themes.ThemeBundle;
import ru.intertrust.cm.core.gui.impl.client.themes.def.datagrid.DataGridResources;
import ru.intertrust.cm.core.gui.impl.client.themes.def.splitter.SplitterStyles;
import ru.intertrust.cm.core.gui.model.ComponentName;
/**
 * @author Yaroslav Bondarchuk
 *         Date: 19.05.14
 *         Time: 17:15
 */
@ComponentName(GlobalThemesManager.THEME_DARK)
public class DarkThemeBundleWrapper implements BundleWrapper {
    private static final DarkThemeBundle themeBundle = GWT.create(DarkThemeBundle.class);
    private static final DataGridResources dataGridResources = GWT.create(DataGridResources.class);

    @Override
    public Component createNew() {
        return new DarkThemeBundleWrapper();
    }

    @Override
    public String getName() {
        return GlobalThemesManager.THEME_DARK;
    }


    @Override
    public CssResource getMainCss() {
        return themeBundle.mainCss();
    }

    @Override
    public ThemeBundle getThemeBundle() {
        return themeBundle;
    }

    @Override
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
}
