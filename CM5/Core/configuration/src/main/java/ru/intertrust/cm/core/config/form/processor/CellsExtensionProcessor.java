package ru.intertrust.cm.core.config.form.processor;

import ru.intertrust.cm.core.config.gui.form.MarkupConfig;
import ru.intertrust.cm.core.config.gui.form.extension.markup.AddCellsConfig;
import ru.intertrust.cm.core.config.gui.form.extension.markup.DeleteCellsConfig;
import ru.intertrust.cm.core.config.gui.form.extension.markup.ReplaceCellsConfig;

import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 17.05.2015
 *         Time: 21:55
 */
public interface CellsExtensionProcessor {
    void processAddCells(MarkupConfig markupConfig, AddCellsConfig addCellsConfig, List<String> errors);
    void processDeleteCells(MarkupConfig markupConfig, DeleteCellsConfig deleteCellsConfig, List<String> errors);
    void processReplaceCells(MarkupConfig markupConfig, ReplaceCellsConfig replaceCellsConfig, List<String> errors);
}
