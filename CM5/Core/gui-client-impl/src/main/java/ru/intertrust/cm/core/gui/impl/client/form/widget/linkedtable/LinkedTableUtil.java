package ru.intertrust.cm.core.gui.impl.client.form.widget.linkedtable;

import com.google.gwt.cell.client.*;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.*;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.impl.client.event.linkedtable.LinkedTableRowDeletedEvent;
import ru.intertrust.cm.core.gui.impl.client.themes.GlobalThemesManager;
import ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.action.CheckAccessRequest;
import ru.intertrust.cm.core.gui.model.form.FormState;
import ru.intertrust.cm.core.gui.model.form.widget.LinkedDomainObjectsTableState;
import ru.intertrust.cm.core.gui.model.form.widget.RowItem;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

import java.util.Map;

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
                                              EventBus localEventBus, LinkedDomainObjectsTableState currentState) {
        configureNoneEditableTable(summaryTableConfig, table, currentState);

        //TODO build action column based on configuration
        if (summaryTableConfig.getSummaryTableActionsColumnConfig() != null) {
            table.addColumn(buildActionsColumn(summaryTableConfig.getSummaryTableActionsColumnConfig(), fieldUpdater, localEventBus, currentState));
        } else {
            //default actions
            table.addColumn(buildActionsColumn(null, fieldUpdater, localEventBus, currentState));
           /* table.addColumn(buildEditButtonColumn(fieldUpdater), "");
            table.addColumn(buildDeleteButtonColumn(localEventBus, fieldUpdater.isTooltipContent()), "");*/
        }

    }

    private static void withCheckAccess(String checkerComponentName, ColumnContext columnContext, final CheckAccessCallback callback) {
        Id objectId = columnContext.getRowItem().getObjectId();
        if (objectId != null) {
            Map<String, Boolean> accessMatrix = columnContext.getRowItem().getAccessMatrix();
            if (accessMatrix != null && accessMatrix.containsKey(checkerComponentName)) {
                if (accessMatrix.get(checkerComponentName)) {
                    callback.onSuccess();
                } else {
                    callback.onDenied();
                }
            } else {
                callback.onSuccess();
            }
        } else {
            //TODO to server check for new row
            callback.onSuccess();
        }
    }

    private static void withCheckAccessOnServer(String checkerComponentName, final CheckAccessCallback callback) {
        CheckAccessRequest request = new CheckAccessRequest();
        request.setAccessCheckerName(checkerComponentName);
        Command command = new Command("checkAccess", "checkAccessHandler", request);
        BusinessUniverseServiceAsync.Impl.executeCommand(command, new AsyncCallback<Dto>() {
            @Override
            public void onSuccess(Dto result) {
                // CheckAccessResponse response = (CheckAccessResponse) result;
                //  if (response.isAccessGranted()) {
                callback.onSuccess();
                //  } else {
                callback.onDenied();
                //   }
            }

            @Override
            public void onFailure(Throwable caught) {
            }
        });
    }

    private static Column<RowItem, ColumnContext> buildActionsColumn(SummaryTableActionsColumnConfig summaryTableActionsColumnConfig,
                                                                     LinkedDomainObjectsTableWidget.TableFieldUpdater fieldUpdater, EventBus localEventBus, LinkedDomainObjectsTableState currentState) {
        ActionsColumn column = new ActionsColumn(new ActionsCell(summaryTableActionsColumnConfig, localEventBus, currentState));
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
        private LinkedDomainObjectsTableState currentState;

        public ActionsCell(SummaryTableActionsColumnConfig summaryTableActionsColumnConfig, EventBus localEventBus, LinkedDomainObjectsTableState currentState) {
            super("click");
            this.summaryTableActionsColumnConfig = summaryTableActionsColumnConfig;
            this.localEventBus = localEventBus;
            this.currentState = currentState;
        }

        @Override
        public void render(Context context, ColumnContext columnContext, SafeHtmlBuilder sb) {
            final HorizontalPanel container = new HorizontalPanel();
            if (summaryTableActionsColumnConfig != null) {
                for (final SummaryTableActionColumnConfig summaryTableActionColumnConfig :
                        summaryTableActionsColumnConfig.getSummaryTableActionColumnConfig()) {
                    if (summaryTableActionColumnConfig.getComponentName() != null) {
                        final ColumnDisplayConfig columnDisplayConfig = summaryTableActionColumnConfig.getColumnDisplayConfig();
                        if (columnDisplayConfig != null) {
                            withCheckAccessDo(columnContext, new CheckAccessCallback() {
                                        @Override
                                        public void onSuccess() {
                                            displayAction(columnDisplayConfig, summaryTableActionColumnConfig, container);
                                        }

                                        @Override
                                        public void onDenied() {
                                        }
                                    },
                                    summaryTableActionColumnConfig.getAccessChecker());
                        }
                    } else if (summaryTableActionColumnConfig.getType() != null) {
                        String type = summaryTableActionColumnConfig.getType();
                        if (type.equals(ActionTypes.edit.name())) {
                            addEditButton(container, columnContext);
                        } else if (type.equals(ActionTypes.delete.name())) {
                            addDeleteButton(container, columnContext);
                        }
                    }
                }
            } else {
                //default actions
                addEditButton(container, columnContext);
                addDeleteButton(container, columnContext);
            }
            sb.appendHtmlConstant(container.toString());
        }

        private void displayAction(ColumnDisplayConfig columnDisplayConfig, SummaryTableActionColumnConfig summaryTableActionColumnConfig, HorizontalPanel container) {
            String url = columnDisplayConfig.getColumnDisplayImageConfig().getUrl();
            String text = columnDisplayConfig.getColumnDisplayTextConfig().getValue();
            Image actionImage = new Image(GlobalThemesManager.getResourceFolder()
                    + url);
            actionImage.addStyleName(ACTION_IMAGE_SELECTOR);
            String componentName = summaryTableActionColumnConfig.getComponentName();
            actionImage.addStyleName(componentName);
            container.add(actionImage);
            Label actionText = new Label(text);
            actionText.addStyleName(componentName);
            actionText.addStyleName(ACTION_TEXT_SELECTOR);
            container.add(actionText);
        }

        private void addDeleteButton(final HorizontalPanel container, ColumnContext columnContext) {
            withCheckAccess(DEFAULT_DELETE_ACCESS_CHECKER, columnContext, new CheckAccessCallback() {
                @Override
                public void onSuccess() {
                    container.add(createDeleteButton());
                }

                @Override
                public void onDenied() {

                }
            });
        }

        private void addEditButton(final HorizontalPanel container, ColumnContext columnContext) {
            withCheckAccess(DEFAULT_EDIT_ACCESS_CHECKER, columnContext, new CheckAccessCallback() {
                @Override
                public void onSuccess() {
                    container.add(createEditButton());
                }

                @Override
                public void onDenied() {

                }
            });
        }

        @Override
        public void onBrowserEvent(final Context context, Element parent, final ColumnContext columnContext, NativeEvent event, final ValueUpdater<ColumnContext> valueUpdater) {
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
                        action.perform(columnContext.getObjectId(), context.getIndex()
                        );
                    }
                }
            }
            if (className.contains(EDIT_BUTTON_SELECTOR)) {
                LinkedTableAction action = ComponentRegistry.instance.get(DEFAULT_EDIT_ACTION_COMPONENT);
                FormState rowFormState = obtainFormStateForRow(columnContext, currentState);
                action.setRowFormState(rowFormState);
                action.setCallback(new PostPerformCallback() {
                    @Override
                    public void onPerform() {
                        valueUpdater.update(columnContext);
                    }
                });
                action.perform(columnContext.getObjectId(), context.getIndex()
                );

            } else if (className.contains(DELETE_BUTTON_SELECTOR)) {
                LinkedTableAction action = ComponentRegistry.instance.get(DEFAULT_DELETE_ACTION_COMPONENT);
                FormState rowFormState = obtainFormStateForRow(columnContext, currentState);
                action.perform(columnContext.getObjectId(), context.getIndex()
                );
                action.setRowFormState(rowFormState);
                action.setCallback(new PostPerformCallback() {
                    @Override
                    public void onPerform() {
                        FieldUpdater<RowItem, ColumnContext> deleteFieldupdater = createDeleteFieldupdater(localEventBus, false);
                        deleteFieldupdater.update(context.getIndex(), columnContext.getRowItem(), columnContext);
                    }
                });

            }
            super.onBrowserEvent(context, parent, columnContext, event, valueUpdater);
        }


    }

    private static void withCheckAccessDo(ColumnContext columnContext, CheckAccessCallback callback, String accessChecker) {
        withCheckAccess(accessChecker, columnContext, callback);
    }

    private static FormState obtainFormStateForRow(ColumnContext columnContext, LinkedDomainObjectsTableState currentState) {
        FormState rowFormState;
        RowItem rowItem = columnContext.getRowItem();
        Id objectId = rowItem.getObjectId();
        if (objectId != null) {
            rowFormState = currentState.getFromEditedStates(objectId.toStringRepresentation());

        } else {
            rowFormState = currentState.getFromNewStates(rowItem.getParameter(BusinessUniverseConstants.STATE_KEY));
        }
        return rowFormState;
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
        private LinkedDomainObjectsTableState currentState;

        public MixedCell(SummaryTableColumnConfig summaryTableColumnConfig, LinkedDomainObjectsTableState currentState) {
            super("click");
            this.summaryTableColumnConfig = summaryTableColumnConfig;
            this.currentState = currentState;
        }

        @Override
        public void render(Context context, final ColumnContext columnContext, SafeHtmlBuilder safeHtmlBuilder) {
            final HorizontalPanel container = new HorizontalPanel();
            SummaryTableActionColumnConfig summaryTableActionColumnConfig = summaryTableColumnConfig.getSummaryTableActionColumnConfig();
            if (summaryTableActionColumnConfig != null) {
                final ColumnDisplayConfig columnDisplayConfig = summaryTableActionColumnConfig.getColumnDisplayConfig();
                if (columnDisplayConfig != null) {
                    withCheckAccessDo(columnContext, new CheckAccessCallback() {
                        @Override
                        public void onSuccess() {
                            drawColumnAction(columnDisplayConfig, container, columnContext);
                        }

                        @Override
                        public void onDenied() {

                        }
                    }, summaryTableActionColumnConfig.getAccessChecker());
                } else {
                    withCheckAccessDo(columnContext, new CheckAccessCallback() {
                        @Override
                        public void onSuccess() {
                            Widget actionableCellText;
                            actionableCellText = new Hyperlink(columnContext.renderRow(), null);
                            actionableCellText.addStyleName(ACTION_TEXT_SELECTOR);
                            container.add(actionableCellText);
                        }

                        @Override
                        public void onDenied() {
                            container.add(new HTML(columnContext.renderRow()));
                        }
                    }, summaryTableActionColumnConfig.getAccessChecker());
                }
            } else {
                container.add(new HTML(columnContext.renderRow()));

            }
            safeHtmlBuilder.appendHtmlConstant(container.toString());
        }

        private void drawColumnAction(ColumnDisplayConfig columnDisplayConfig, HorizontalPanel container, ColumnContext columnContext) {
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
        }

        @Override
        public void onBrowserEvent(Context context, Element parent, ColumnContext columnContext, NativeEvent event, ValueUpdater<ColumnContext> valueUpdater) {
            EventTarget eventTarget = event.getEventTarget();
            Element as = Element.as(eventTarget);
            if (as.getClassName().contains(ACTION_IMAGE_SELECTOR) || as.getClassName().contains(ACTION_TEXT_SELECTOR)) {
                String componentName = summaryTableColumnConfig.getSummaryTableActionColumnConfig().getComponentName();
                LinkedTableAction action = ComponentRegistry.instance.get(componentName);
                FormState rowFormState = obtainFormStateForRow(columnContext, currentState);
                action.setRowFormState(rowFormState);
                action.perform(columnContext.getObjectId(), context.getIndex());
            }
            super.onBrowserEvent(context, parent, columnContext, event, valueUpdater);
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
            columnContext.setRowItem(rowItem);
            return columnContext;
        }
    }


    public static void configureNoneEditableTable(SummaryTableConfig summaryTableConfig, CellTable<RowItem> table, LinkedDomainObjectsTableState currentState) {
        for (final SummaryTableColumnConfig summaryTableColumnConfig : summaryTableConfig.getSummaryTableColumnConfigList()) {
            MixedContentColumn mixedContentColumn = new MixedContentColumn(new MixedCell(summaryTableColumnConfig, currentState), summaryTableColumnConfig);
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

    private static abstract class CheckAccessCallback {
        public abstract void onSuccess();

        public abstract void onDenied();
    }
}
