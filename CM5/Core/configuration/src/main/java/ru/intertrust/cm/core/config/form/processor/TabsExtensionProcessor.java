package ru.intertrust.cm.core.config.form.processor;

import ru.intertrust.cm.core.config.gui.form.MarkupConfig;
import ru.intertrust.cm.core.config.gui.form.extension.markup.AddTabsConfig;
import ru.intertrust.cm.core.config.gui.form.extension.markup.DeleteTabsConfig;
import ru.intertrust.cm.core.config.gui.form.extension.markup.ReplaceTabsConfig;

import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 17.05.2015
 *         Time: 21:41
 */
public interface TabsExtensionProcessor {
    void processAddTabs(MarkupConfig markupConfig, AddTabsConfig addTabsConfig, List<String> errors);
    void processDeleteTabs(MarkupConfig markupConfig, DeleteTabsConfig deleteTabsConfig, List<String> errors);
    void processReplaceTabs(MarkupConfig markupConfig, ReplaceTabsConfig replaceTabsConfig, List<String> errors);

}
