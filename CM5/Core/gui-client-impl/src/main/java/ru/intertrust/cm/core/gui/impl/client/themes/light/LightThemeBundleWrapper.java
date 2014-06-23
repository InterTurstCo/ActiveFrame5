package ru.intertrust.cm.core.gui.impl.client.themes.light;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.user.cellview.client.DataGrid;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.themes.BundleWrapper;
import ru.intertrust.cm.core.gui.impl.client.themes.GlobalThemesManager;
import ru.intertrust.cm.core.gui.impl.client.themes.def.datagrid.DataGridResources;
import ru.intertrust.cm.core.gui.impl.client.themes.def.splitter.SplitterStyles;
import ru.intertrust.cm.core.gui.impl.client.themes.light.datagrid.LightDataGridResources;
import ru.intertrust.cm.core.gui.model.ComponentName;
/**
 * @author Yaroslav Bondarchuk
 *         Date: 30.05.14
 *         Time: 18:27
 */
@ComponentName(GlobalThemesManager.THEME_LIGHT)
    public class LightThemeBundleWrapper implements BundleWrapper {
    private static final LightThemeBundle themeBundle = GWT.create(LightThemeBundle.class);
    private static final LightDataGridResources dataGridResources = GWT.create(LightDataGridResources.class);

    @Override
    public Component createNew() {
        return new LightThemeBundleWrapper();
    }

    @Override
    public String getName() {
        return GlobalThemesManager.THEME_LIGHT;
    }

    @Override
    public CssResource getMainCss() {
        return themeBundle.mainCss();
    }
    public  LightThemeBundle getThemeBundle() {
        return themeBundle;
    }

    @Override
    public DataGrid.Resources getDataGridResources() {
        return dataGridResources;
    }

    @Override
    public SplitterStyles getSplitterStyles() {
        return themeBundle.splitterCss();
    }

    @Override
    public CssResource getNavigationTreeCss() {
        return themeBundle.navigationTreeCss();
    }
}
