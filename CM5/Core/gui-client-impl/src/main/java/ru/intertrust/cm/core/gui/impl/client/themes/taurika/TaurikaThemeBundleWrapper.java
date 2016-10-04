package ru.intertrust.cm.core.gui.impl.client.themes.taurika;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.user.cellview.client.DataGrid;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.themes.BundleWrapper;
import ru.intertrust.cm.core.gui.impl.client.themes.GlobalThemesManager;
import ru.intertrust.cm.core.gui.impl.client.themes.def.splitter.SplitterStyles;
import ru.intertrust.cm.core.gui.impl.client.themes.taurika.datagrid.TaurikaDataGridResources;
import ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants;
import ru.intertrust.cm.core.gui.model.ComponentName;

/**
 * Created by IntelliJ IDEA.
 * Developer: Ravil Abdulkhairov
 * Date: 04.10.2016
 * Time: 14:58
 * To change this template use File | Settings | File and Code Templates.
 */
@ComponentName(GlobalThemesManager.THEME_TAURIKA)
public class TaurikaThemeBundleWrapper implements BundleWrapper {
    private static final TaurikaThemeBundle themeBundle = GWT.create(TaurikaThemeBundle.class);
    private static final TaurikaDataGridResources dataGridResources = GWT.create(TaurikaDataGridResources.class);

    @Override
    public Component createNew() {
        return new TaurikaThemeBundleWrapper();
    }

    @Override
    public String getName() {
        return GlobalThemesManager.THEME_TAURIKA;
    }

    @Override
    public CssResource getMainCss() {
        return themeBundle.mainCss();
    }

    public TaurikaThemeBundle getThemeBundle() {
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
        return BusinessUniverseConstants.TAURIKA_THEME_FOLDER;
    }
}
