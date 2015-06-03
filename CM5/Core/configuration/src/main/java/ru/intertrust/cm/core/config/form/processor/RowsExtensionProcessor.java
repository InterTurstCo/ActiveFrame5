package ru.intertrust.cm.core.config.form.processor;

import ru.intertrust.cm.core.config.gui.form.MarkupConfig;
import ru.intertrust.cm.core.config.gui.form.extension.markup.AddRowsConfig;
import ru.intertrust.cm.core.config.gui.form.extension.markup.DeleteRowsConfig;
import ru.intertrust.cm.core.config.gui.form.extension.markup.ReplaceRowsConfig;

import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 17.05.2015
 *         Time: 21:52
 */
public interface RowsExtensionProcessor {
    void processAddRows(MarkupConfig markupConfig, AddRowsConfig addRowsConfig, List<String> errors);
    void processDeleteRows(MarkupConfig markupConfig, DeleteRowsConfig deleteRowsConfig, List<String> errors);
    void processReplaceRows(MarkupConfig markupConfig, ReplaceRowsConfig replaceRowsConfig, List<String> errors);
}
