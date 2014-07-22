package ru.intertrust.cm.core.gui.impl.client.themes;

import com.google.gwt.resources.client.CssResource;
import com.google.gwt.user.cellview.client.DataGrid;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.themes.def.splitter.SplitterStyles;


/**
 * @author Yaroslav Bondarchuk
 *         Date: 14.05.14
 *         Time: 10:25
 */
public interface BundleWrapper extends Component {

    public CssResource getMainCss();

    public ThemeBundle getThemeBundle();

    public DataGrid.Resources getDataGridResources();

    public SplitterStyles getSplitterStyles();

    public CssResource getNavigationTreeCss();

    public String getResourceFolder();

}
