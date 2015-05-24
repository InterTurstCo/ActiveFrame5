package ru.intertrust.cm.core.config.form;

import ru.intertrust.cm.core.config.gui.form.HeaderConfig;
import ru.intertrust.cm.core.config.gui.form.RowConfig;
import ru.intertrust.cm.core.config.gui.form.TabGroupConfig;

import java.util.Collections;
import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 11.05.2015
 *         Time: 18:03
 */
public class MarkupUtils {
    public static List<RowConfig> getHeaderRows(HeaderConfig headerConfig){
        return headerConfig.getTableLayout() == null ? Collections.<RowConfig>emptyList() : headerConfig.getTableLayout().getRows();
    }
    public static List<RowConfig> getTabGroupRows(TabGroupConfig tabGroupConfig){
        return tabGroupConfig.getLayout() == null ? Collections.<RowConfig>emptyList() : tabGroupConfig.getLayout().getRows();
    }
}
