package ru.intertrust.cm.core.gui.impl.client.themes.lucem;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.user.cellview.client.DataGrid;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.themes.BundleWrapper;
import ru.intertrust.cm.core.gui.impl.client.themes.GlobalThemesManager;
import ru.intertrust.cm.core.gui.impl.client.themes.def.splitter.SplitterStyles;
import ru.intertrust.cm.core.gui.impl.client.themes.lucem.datagrid.LucemDataGridResources;
import ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants;
import ru.intertrust.cm.core.gui.model.ComponentName;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 30.05.14
 *         Time: 18:27
 */
@ComponentName(GlobalThemesManager.THEME_LUCEM)
public class LucemThemeBundleWrapper implements BundleWrapper {
    private static final LucemThemeBundle themeBundle = GWT.create(LucemThemeBundle.class);
    private static final LucemDataGridResources dataGridResources = GWT.create(LucemDataGridResources.class);

    @Override
    public Component createNew() {
        return new LucemThemeBundleWrapper();
    }

    @Override
    public String getName() {
        return GlobalThemesManager.THEME_LUCEM;
    }

    @Override
    public CssResource getMainCss() {
        return themeBundle.mainCss();
    }

    public LucemThemeBundle getThemeBundle() {
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

    @Override
    public String getResourceFolder() {
        return BusinessUniverseConstants.LUCEM_THEME_FOLDER;
    }
}
