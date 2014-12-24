package ru.intertrust.cm.core.gui.impl.client.form.widget.linkedtable;

import com.google.gwt.cell.client.*;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.*;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.config.gui.form.widget.*;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.impl.client.event.linkedtable.LinkedTableRowDeletedEvent;
import ru.intertrust.cm.core.gui.impl.client.themes.GlobalThemesManager;
import ru.intertrust.cm.core.gui.model.form.widget.RowItem;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 18.07.2014
 *         Time: 0:21
 */
public class LinkedTableUtil {

    public static final String ACTION_IMAGE_SELECTOR = "action-image";
    public static final String ACTION_TEXT_SELECTOR = "action-text";
    public static final String EDIT_BUTTON_SELECTOR = "editButton";
    public static final String DELETE_BUTTON_SELECTOR = "deleteButton";

    public static final String DEFAULT_EDIT_ACTION_COMPONENT = "default.edit.table.action";
    public static final String DEFAULT_DELETE_ACTION_COMPONENT = "default.delete.table.action";

    enum ActionTypes {
        edit, delete
    }

    public static void configureEditableTable(SummaryTableConfig summaryTableConfig, CellTable<RowItem> table,
                                              LinkedDomainObjectsTableWidget.TableFieldUpdater fieldUpdater,
                                              EventBus localEventBus) {
        configureNoneEditableTable(summaryTableConfig, table);

        //TODO build action column based on configuration
        if (summaryTableConfig.getSummaryTableActionsColumnConfig() != null) {
            table.addColumn(buildActionsColumn(summaryTableConfig.getSummaryTableActionsColumnConfig(), fieldUpdater, localEventBus));
        } else {
            //default actions
            table.addColumn(buildActionsColumn(null, fieldUpdater, localEventBus));
           /* table.addColumn(buildEditButtonColumn(fieldUpdater), "");
            table.addColumn(buildDeleteButtonColumn(localEventBus, fieldUpdater.isTooltipContent()), "");*/
        }

    }

    private static Column<RowItem, ColumnContext> buildActionsColumn(SummaryTableActionsColumnConfig summaryTableActionsColumnConfig,
                                                                     LinkedDomainObjectsTableWidget.TableFieldUpdater fieldUpdater, EventBus localEventBus) {
        ActionsColumn column = new ActionsColumn(new ActionsCell(summaryTableActionsColumnConfig, localEventBus));
        column.setFieldUpdater(fieldUpdater);
        return column;
    }

    static class ActionsCell extends AbstractCell<ColumnContext> {
        private static final String DEFAULT_EDIT_ACCESS_CHECKER = "default.edit.access.checker";
        private static final String DEFAULT_EDIT_NEWOBJECT_ACCESS_CHECKER = "default.edit.newobject.access.checker";
        private static final String DEFAULT_DELETE_ACCESS_CHECKER = "default.delete.access.checker";
        private static final String DEFAULT_DELETE_NEWOBJECT_ACCESS_CHECKER = "default.delete.newobject.access.checker";
        private SummaryTableActionsColumnConfig summaryTableActionsColumnConfig;
        private EventBus localEventBus;

        public ActionsCell(SummaryTableActionsColumnConfig summaryTableActionsColumnConfig, EventBus localEventBus) {
            super("click");
            this.summaryTableActionsColumnConfig = summaryTableActionsColumnConfig;
            this.localEventBus = localEventBus;
        }

        @Override
        public void render(Context context, ColumnContext columnContext, SafeHtmlBuilder sb) {
            HorizontalPanel container = new HorizontalPanel();
            if (summaryTableActionsColumnConfig != null) {
                for (SummaryTableActionColumnConfig summaryTableActionColumnConfig :
                        summaryTableActionsColumnConfig.getSummaryTableActionColumnConfig()) {
                    if (summaryTableActionColumnConfig.getComponentName() != null) {
                        ColumnDisplayConfig columnDisplayConfig = summaryTableActionColumnConfig.getColumnDisplayConfig();
                        if (columnDisplayConfig != null) {
                            String url = columnDisplayConfig.getColumnDisplayImageConfig().getUrl();
                            String text = columnDisplayConfig.getColumnDisplayTextConfig().getValue();
                            Image actionImage = new Image(GlobalThemesManager.getResourceFolder()
                                    + url);
                            //fill column context
                            ColumnContext nestedContext = new ColumnContext();
                            nestedContext.setAccessChecker(summaryTableActionColumnConfig.getAccessChecker());
                            nestedContext.setNewObjectsAccessChecker(summaryTableActionColumnConfig.getNewObjectsAccessChecker());
                            nestedContext.setComponentName(summaryTableActionColumnConfig.getComponentName());
                            columnContext.setNestedColumnContext(summaryTableActionColumnConfig.getComponentName(), nestedContext);

                            actionImage.addStyleName(ACTION_IMAGE_SELECTOR);
                            String componentName = summaryTableActionColumnConfig.getComponentName();
                            actionImage.addStyleName(componentName);
                            container.add(actionImage);
                            Label actionText = new Label(text);
                            actionText.addStyleName(componentName);
                            actionText.addStyleName(ACTION_TEXT_SELECTOR);
                            container.add(actionText);
                        }
                    } else if (summaryTableActionColumnConfig.getType() != null) {
                        String type = summaryTableActionColumnConfig.getType();
                        if (type.equals(ActionTypes.edit.name())) {
                            Button editButton = createEditButton();
                            container.add(editButton);
                        } else if (type.equals(ActionTypes.delete.name())) {
                            Button deleteButton = createDeleteButton();
                            container.add(deleteButton);
                        }
                    }
                }
            } else {
                container.add(createEditButton());
                container.add(createDeleteButton());
            }
            sb.appendHtmlConstant(container.toString());
        }

        @Override
        public void onBrowserEvent(final Context context, Element parent, final ColumnContext value, NativeEvent event, final ValueUpdater<ColumnContext> valueUpdater) {
            EventTarget eventTarget = event.getEventTarget();
            Element as = Element.as(eventTarget);
            String className = as.getClassName();
            if (summaryTableActionsColumnConfig != null) {
                for (SummaryTableActionColumnConfig summaryTableActionColumnConfig : summaryTableActionsColumnConfig.getSummaryTableActionColumnConfig()) {
                    if (summaryTableActionColumnConfig.getComponentName() == null) {
                        continue;
                    }
                    String componentName = summaryTableActionColumnConfig.getComponentName();
                    if ((className.contains(ACTION_IMAGE_SELECTOR) || className.contains(ACTION_TEXT_SELECTOR)) &&
                            className.contains(componentName)) {
                        LinkedTableAction action = ComponentRegistry.instance.get(componentName);
                        action.perform(value.getObjectId(), context.getIndex(),
                                summaryTableActionColumnConfig.getAccessChecker(),
                                summaryTableActionColumnConfig.getNewObjectsAccessChecker());
                    }
                }
            }
            if (className.contains(EDIT_BUTTON_SELECTOR)) {
                LinkedTableAction action = ComponentRegistry.instance.get(DEFAULT_EDIT_ACTION_COMPONENT);
                action.setCallback(new PostPerformCallback() {
                    @Override
                    public void onPerform() {
                        valueUpdater.update(value);
                    }
                });
                action.perform(value.getObjectId(), context.getIndex(),
                        DEFAULT_EDIT_ACCESS_CHECKER,
                        DEFAULT_EDIT_NEWOBJECT_ACCESS_CHECKER);

            } else if (className.contains(DELETE_BUTTON_SELECTOR)) {
                LinkedTableAction action = ComponentRegistry.instance.get(DEFAULT_DELETE_ACTION_COMPONENT);
                action.perform(value.getObjectId(), context.getIndex(),
                        DEFAULT_DELETE_ACCESS_CHECKER,
                        DEFAULT_DELETE_NEWOBJECT_ACCESS_CHECKER);
                action.setCallback(new PostPerformCallback() {
                    @Override
                    public void onPerform() {
                        FieldUpdater<RowItem, ColumnContext> deleteFieldupdater = createDeleteFieldupdater(localEventBus, false);
                        deleteFieldupdater.update(context.getIndex(), value.getRowItem(), value);
                    }
                });

            }
            super.onBrowserEvent(context, parent, value, event, valueUpdater);
        }
    }

    private static Button createDeleteButton() {
        Button deleteButton = new Button();
        deleteButton.setStyleName(GlobalThemesManager.getCurrentTheme().commonCss().deleteButton());
        deleteButton.addStyleName(DELETE_BUTTON_SELECTOR);
        return deleteButton;
    }

    private static Button createEditButton() {
        Button editButton = new Button();
        editButton.setStyleName(GlobalThemesManager.getCurrentTheme().commonCss().editButton());
        editButton.addStyleName(EDIT_BUTTON_SELECTOR);
        return editButton;
    }

    static class ActionsColumn extends Column<RowItem, ColumnContext> {
        public ActionsColumn(Cell<ColumnContext> cell) {
            super(cell);
        }

        @Override
        public ColumnContext getValue(RowItem object) {
            ColumnContext columnContext = new ColumnContext();
            columnContext.setRowItem(object);
            return columnContext;
        }
    }

    static class MixedCell extends AbstractCell<ColumnContext> {
        private SummaryTableColumnConfig summaryTableColumnConfig;

        public MixedCell(SummaryTableColumnConfig summaryTableColumnConfig) {
            super("click");
            this.summaryTableColumnConfig = summaryTableColumnConfig;
        }

        @Override
        public void render(Context context, ColumnContext columnContext, SafeHtmlBuilder safeHtmlBuilder) {
            HorizontalPanel container = new HorizontalPanel();
            SummaryTableActionColumnConfig summaryTableActionColumnConfig = summaryTableColumnConfig.getSummaryTableActionColumnConfig();

            Widget actionableCellText;
            if (summaryTableActionColumnConfig != null) {
                ColumnDisplayConfig columnDisplayConfig = summaryTableActionColumnConfig.getColumnDisplayConfig();
                if (columnDisplayConfig != null) {
                    Image actionImage = new Image(GlobalThemesManager.getResourceFolder()
                            + columnDisplayConfig.getColumnDisplayImageConfig().getUrl());
                    actionImage.addStyleName(ACTION_IMAGE_SELECTOR);
                    container.add(actionImage);
                    Label actionText = new Label(columnDisplayConfig.getColumnDisplayTextConfig().getValue());
                    actionText.addStyleName(ACTION_TEXT_SELECTOR);
                    container.add(actionText);
                    HTML htmlCellText = new HTML("<div>" + columnContext.renderRow() + "</div>");
                    if (ColumnDisplayConfig.Position.before.name().equals(columnDisplayConfig.getPosition())) {
                        container.insert(htmlCellText, 0);
                    } else {
                        container.add(htmlCellText);
                    }
                } else {
                    actionableCellText = new Hyperlink(columnContext.renderRow(), null);
                    actionableCellText.addStyleName(ACTION_TEXT_SELECTOR);
                    container.add(actionableCellText);
                }
            } else {
                container.add(new HTML(columnContext.renderRow()));

            }
            safeHtmlBuilder.appendHtmlConstant(container.toString());
        }

        @Override
        public void onBrowserEvent(Context context, Element parent, ColumnContext value, NativeEvent event, ValueUpdater<ColumnContext> valueUpdater) {
            EventTarget eventTarget = event.getEventTarget();
            Element as = Element.as(eventTarget);
            if (as.getClassName().contains(ACTION_IMAGE_SELECTOR) || as.getClassName().contains(ACTION_TEXT_SELECTOR)) {
                String componentName = summaryTableColumnConfig.getSummaryTableActionColumnConfig().getComponentName();
                LinkedTableAction action = ComponentRegistry.instance.get(componentName);
                action.perform(value.getObjectId(), context.getIndex(), summaryTableColumnConfig.getSummaryTableActionColumnConfig().getAccessChecker(), summaryTableColumnConfig.getSummaryTableActionColumnConfig().getNewObjectsAccessChecker());
            }
            super.onBrowserEvent(context, parent, value, event, valueUpdater);
        }
    }

    static class MixedContentColumn extends Column<RowItem, ColumnContext> {
        private Cell<ColumnContext> cell;
        private SummaryTableColumnConfig columnConfig;

        public MixedContentColumn(Cell<ColumnContext> cell, SummaryTableColumnConfig columnConfig) {
            super(cell);
            this.cell = cell;
            this.columnConfig = columnConfig;
        }

        @Override
        public ColumnContext getValue(RowItem rowItem) {
            ColumnContext columnContext = new ColumnContext();
            String valueByKey = rowItem.getValueByKey(columnConfig.getWidgetId());
            columnContext.setObjectId(rowItem.getObjectId());
            columnContext.setValue(valueByKey);
            return columnContext;
        }
    }


    public static void configureNoneEditableTable(SummaryTableConfig summaryTableConfig, CellTable<RowItem> table) {
        for (final SummaryTableColumnConfig summaryTableColumnConfig : summaryTableConfig.getSummaryTableColumnConfigList()) {
            MixedContentColumn mixedContentColumn = new MixedContentColumn(new MixedCell(summaryTableColumnConfig), summaryTableColumnConfig);
            //TextColumn<RowItem> column = createTextColumn(summaryTableColumnConfig);
            table.addColumn(mixedContentColumn, summaryTableColumnConfig.getHeader());
        }
    }

    private static TextColumn<RowItem> createTextColumn(final SummaryTableColumnConfig summaryTableColumnConfig) {
        return new TextColumn<RowItem>() {
            @Override
            public String getValue(RowItem object) {
                return object.getValueByKey(summaryTableColumnConfig.getWidgetId());
            }
        };
    }

    private static Column<RowItem, ColumnContext> buildEditButtonColumn(LinkedDomainObjectsTableWidget.TableFieldUpdater fieldUpdater) {
        Cell<ColumnContext> editButton = new StyledButtonCell(GlobalThemesManager.getCurrentTheme().commonCss().editButton());
        Column<RowItem, ColumnContext> editButtonColumn = new Column<RowItem, ColumnContext>(editButton) {
            @Override
            public ColumnContext getValue(RowItem object) {
                return new ColumnContext();
            }

        };
        editButtonColumn.setCellStyleNames(GlobalThemesManager.getCurrentTheme().commonCss().editColumn());
        editButtonColumn.setFieldUpdater(fieldUpdater);
        return editButtonColumn;
    }

    private static Column<RowItem, ColumnContext> buildDeleteButtonColumn(final EventBus localEventBus, final boolean tooltipContent) {
        Cell<ColumnContext> deleteButton = new StyledButtonCell(GlobalThemesManager.getCurrentTheme().commonCss().deleteButton());
        Column<RowItem, ColumnContext> deleteButtonColumn = new Column<RowItem, ColumnContext>(deleteButton) {
            @Override
            public ColumnContext getValue(RowItem object) {
                return null;
            }
        };
        FieldUpdater<RowItem, ColumnContext> deleteFieldUpdater = createDeleteFieldupdater(localEventBus, tooltipContent);
        deleteButtonColumn.setFieldUpdater(deleteFieldUpdater);
        deleteButtonColumn.setCellStyleNames(GlobalThemesManager.getCurrentTheme().commonCss().deleteColumn());

        return deleteButtonColumn;
    }

    private static FieldUpdater<RowItem, ColumnContext> createDeleteFieldupdater(final EventBus localEventBus, final boolean tooltipContent) {
        return new FieldUpdater<RowItem, ColumnContext>() {
            @Override
            public void update(int index, RowItem object, ColumnContext value) {
                localEventBus.fireEvent(new LinkedTableRowDeletedEvent(object, tooltipContent));

            }
        };
    }
}
