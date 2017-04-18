package ru.intertrust.cm.core.config.form.processor.impl;

import ru.intertrust.cm.core.config.form.processor.*;
import ru.intertrust.cm.core.config.gui.form.MarkupConfig;
import ru.intertrust.cm.core.config.gui.form.extension.FormExtensionOperation;
import ru.intertrust.cm.core.config.gui.form.extension.markup.*;

import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 09.05.2015
 *         Time: 18:58
 */
public class MarkupExtensionProcessor implements FormExtensionProcessor {
    private TabsExtensionProcessor tabsExtensionProcessor;
    private TabGroupsExtensionProcessor tabGroupsExtensionProcessor;
    private RowsExtensionProcessor rowsExtensionProcessor;
    private CellsExtensionProcessor cellsExtensionProcessor;

    private MarkupConfig markupConfig;
    private List<String> errors;

    private AddTabsConfig addTabsConfig;
    private DeleteTabsConfig deleteTabsConfig;
    private ReplaceTabsConfig replaceTabsConfig;

    private AddTabGroupsConfig addTabGroupsConfig;
    private DeleteTabGroupsConfig deleteTabGroupsConfig;
    private ReplaceTabGroupsConfig replaceTabGroupsConfig;

    private AddRowsConfig addRowsConfig;
    private DeleteRowsConfig deleteRowsConfig;
    private ReplaceRowsConfig replaceRowsConfig;

    private AddCellsConfig addCellsConfig;
    private DeleteCellsConfig deleteCellsConfig;
    private ReplaceCellsConfig replaceCellsConfig;

    public MarkupExtensionProcessor() {
    }

    public MarkupExtensionProcessor(MarkupConfig markupConfig, List<String> errors) {
        this.markupConfig = markupConfig;
        this.errors = errors;
    }

    public MarkupExtensionProcessor(MarkupConfig markupConfig, FormExtensionOperation formExtensionOperation, List<String> errors) {
        this(markupConfig, errors);
        final Class<? extends FormExtensionOperation> clazz = formExtensionOperation.getClass();
        if (clazz.equals(AddTabsConfig.class)) {
            this.addTabsConfig = (AddTabsConfig) formExtensionOperation;
        } else if (clazz.equals(DeleteTabsConfig.class)) {
            this.deleteTabsConfig = (DeleteTabsConfig) formExtensionOperation;
        } else if (clazz.equals(ReplaceTabsConfig.class)) {
            this.replaceTabsConfig = (ReplaceTabsConfig) formExtensionOperation;
        } else if (clazz.equals(AddTabGroupsConfig.class)) {
            this.addTabGroupsConfig = (AddTabGroupsConfig) formExtensionOperation;
        } else if (clazz.equals(DeleteTabGroupsConfig.class)) {
            this.deleteTabGroupsConfig = (DeleteTabGroupsConfig) formExtensionOperation;
        } else if (clazz.equals(ReplaceTabGroupsConfig.class)) {
            this.replaceTabGroupsConfig = (ReplaceTabGroupsConfig) formExtensionOperation;
        } else if (clazz.equals(AddRowsConfig.class)) {
            this.addRowsConfig = (AddRowsConfig) formExtensionOperation;
        } else if (clazz.equals(DeleteRowsConfig.class)) {
            this.deleteRowsConfig = (DeleteRowsConfig) formExtensionOperation;
        } else if (clazz.equals(ReplaceRowsConfig.class)) {
            this.replaceRowsConfig = (ReplaceRowsConfig) formExtensionOperation;
        } else if (clazz.equals(AddCellsConfig.class)) {
            this.addCellsConfig = (AddCellsConfig) formExtensionOperation;
        } else if (clazz.equals(DeleteCellsConfig.class)) {
            this.deleteCellsConfig = (DeleteCellsConfig) formExtensionOperation;
        } else if (clazz.equals(ReplaceCellsConfig.class)) {
            this.replaceCellsConfig = (ReplaceCellsConfig) formExtensionOperation;
        }
        tabsExtensionProcessor = new TabsExtensionProcessorImpl();
        tabGroupsExtensionProcessor = new TabGroupsExtensionProcessorImpl();
        rowsExtensionProcessor = new RowsExtensionProcessorImpl();
        cellsExtensionProcessor = new CellsExtensionProcessorImpl();
    }

    public MarkupExtensionProcessor(MarkupConfig markupConfig, AddTabsConfig addTabsConfig,
                                    List<String> errors) {
        this(markupConfig, errors);
        this.addTabsConfig = addTabsConfig;
    }

    public MarkupExtensionProcessor(MarkupConfig markupConfig, DeleteTabsConfig deleteTabsConfig,
                                    List<String> errors) {
        this(markupConfig, errors);
        this.deleteTabsConfig = deleteTabsConfig;
    }

    public MarkupExtensionProcessor(MarkupConfig markupConfig, ReplaceTabsConfig replaceTabsConfig,
                                    List<String> errors) {
        this(markupConfig, errors);
        this.replaceTabsConfig = replaceTabsConfig;
    }

    public MarkupExtensionProcessor(MarkupConfig markupConfig, AddTabGroupsConfig addTabGroupsConfig,
                                    List<String> errors) {
        this(markupConfig, errors);
        this.addTabGroupsConfig = addTabGroupsConfig;
    }

    public MarkupExtensionProcessor(MarkupConfig markupConfig, DeleteTabGroupsConfig deleteTabGroupsConfig,
                                    List<String> errors) {
        this(markupConfig, errors);
        this.deleteTabGroupsConfig = deleteTabGroupsConfig;
    }

    public MarkupExtensionProcessor(MarkupConfig markupConfig, ReplaceTabGroupsConfig replaceTabGroupsConfig,
                                    List<String> errors) {
        this(markupConfig, errors);
        this.replaceTabGroupsConfig = replaceTabGroupsConfig;
    }

    public MarkupExtensionProcessor(MarkupConfig markupConfig, AddRowsConfig addRowsConfig,
                                    List<String> errors) {
        this(markupConfig, errors);
        this.addRowsConfig = addRowsConfig;
    }

    public MarkupExtensionProcessor(MarkupConfig markupConfig, DeleteRowsConfig deleteRowsConfig,
                                    List<String> errors) {
        this(markupConfig, errors);
        this.deleteRowsConfig = deleteRowsConfig;
    }

    public MarkupExtensionProcessor(MarkupConfig markupConfig, ReplaceRowsConfig replaceRowsConfig,
                                    List<String> errors) {
        this(markupConfig, errors);
        this.replaceRowsConfig = replaceRowsConfig;
    }

    public MarkupExtensionProcessor(MarkupConfig markupConfig, AddCellsConfig addCellsConfig,
                                    List<String> errors) {
        this(markupConfig, errors);
        this.addCellsConfig = addCellsConfig;
    }

    public MarkupExtensionProcessor(MarkupConfig markupConfig, DeleteCellsConfig deleteCellsConfig,
                                    List<String> errors) {
        this(markupConfig, errors);
        this.deleteCellsConfig = deleteCellsConfig;
    }

    public MarkupExtensionProcessor(MarkupConfig markupConfig, ReplaceCellsConfig replaceCellsConfig,
                                    List<String> errors) {
        this(markupConfig, errors);
        this.replaceCellsConfig = replaceCellsConfig;
    }

    @Override
    public void processFormExtension() {
        if (addTabsConfig != null) {
            tabsExtensionProcessor.processAddTabs(markupConfig, addTabsConfig, errors);
        } else if (deleteTabsConfig != null) {
            tabsExtensionProcessor.processDeleteTabs(markupConfig, deleteTabsConfig, errors);
        } else if (replaceTabsConfig != null) {
            tabsExtensionProcessor.processReplaceTabs(markupConfig, replaceTabsConfig, errors);
        } else if (addTabGroupsConfig != null) {
            tabGroupsExtensionProcessor.processAddTabGroups(markupConfig, addTabGroupsConfig, errors);
        } else if (deleteTabGroupsConfig != null) {
            tabGroupsExtensionProcessor.processDeleteTabGroups(markupConfig, deleteTabGroupsConfig, errors);
        } else if (replaceTabGroupsConfig != null) {
            tabGroupsExtensionProcessor.processReplaceTabGroups(markupConfig, replaceTabGroupsConfig, errors);
        } else if (addRowsConfig != null) {
           rowsExtensionProcessor.processAddRows(markupConfig, addRowsConfig, errors);
        } else if (deleteRowsConfig != null) {
            rowsExtensionProcessor.processDeleteRows(markupConfig, deleteRowsConfig, errors);
        } else if (replaceRowsConfig != null) {
            rowsExtensionProcessor.processReplaceRows(markupConfig, replaceRowsConfig, errors);
        } else if (addCellsConfig != null) {
            cellsExtensionProcessor.processAddCells(markupConfig, addCellsConfig, errors);
        } else if (deleteCellsConfig != null) {
            cellsExtensionProcessor.processDeleteCells(markupConfig, deleteCellsConfig, errors);
        } else if (replaceCellsConfig != null) {
            cellsExtensionProcessor.processReplaceCells(markupConfig, replaceCellsConfig, errors);
        }

    }


}
