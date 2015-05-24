package ru.intertrust.cm.core.config.form.processor;

import ru.intertrust.cm.core.config.gui.form.MarkupConfig;
import ru.intertrust.cm.core.config.gui.form.extension.markup.AddTabGroupsConfig;
import ru.intertrust.cm.core.config.gui.form.extension.markup.DeleteTabGroupsConfig;
import ru.intertrust.cm.core.config.gui.form.extension.markup.ReplaceTabGroupsConfig;

import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 17.05.2015
 *         Time: 21:50
 */
public interface TabGroupsExtensionProcessor {
    void processAddTabGroups(MarkupConfig markupConfig, AddTabGroupsConfig addTabGroupsConfig, List<String> errors);
    void processDeleteTabGroups(MarkupConfig markupConfig, DeleteTabGroupsConfig deleteTabGroupsConfig, List<String> errors);
    void processReplaceTabGroups(MarkupConfig markupConfig, ReplaceTabGroupsConfig replaceTabGroupsConfig, List<String> errors);
}
