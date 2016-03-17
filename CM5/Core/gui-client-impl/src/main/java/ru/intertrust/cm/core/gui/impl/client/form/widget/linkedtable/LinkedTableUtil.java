package ru.intertrust.cm.core.gui.impl.client.form.widget.linkedtable;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
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

import java.util.List;
import java.util.Map;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 18.07.2014
 *         Time: 0:21
 */
public class LinkedTableUtil {

    public static final String ACTION_IMAGE_SELECTOR = "action-image";
    public static final String ACTION_TEXT_SELECTOR = "action-text";

    public static final String DEFAULT_EDIT_ACTION_COMPONENT = "default.edit.table.action";
    public static final String DEFAULT_DELETE_ACTION_COMPONENT = "default.delete.table.action";
    public static final String DEFAULT_VIEW_ACTION_COMPONENT = "default.view.table.action";

    enum ActionTypes {

        edit("editActionStyle"), delete("deleteActionStyle"), view("viewActionStyle"), view_or_edit("viewOrEditActionStyle");
        private String styleClassName;

        ActionTypes(String styleClassName) {
            this.styleClassName = styleClassName;
        }

        public String getStyleClassName() {
            return styleClassName;
        }

        public static ActionTypes forName(String name) {
            for (ActionTypes actionTypes : ActionTypes.values()) {
                if (actionTypes.name().equalsIgnoreCase(name)) {
                    return actionTypes;
                }

            }
            return null;
        }
    }

    public static void configureEditableTable(SummaryTableConfig summaryTableConfig, CellTable<RowItem> table,
                                              LinkedDomainObjectsTableWidget.TableFieldUpdater fieldUpdater,
                                              EventBus localEventBus, LinkedDomainObjectsTableState currentState) {
        configureTable(summaryTableConfig, table, currentState, fieldUpdater, localEventBus);
        table.addColumn(buildActionsColumn(summaryTableConfig, currentState, fieldUpdater, localEventBus));


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

    private static Column<RowItem, ColumnContext> buildActionsColumn(SummaryTableConfig summaryTableConfig,
                                                                     LinkedDomainObjectsTableState currentState,
                                                                     LinkedDomainObjectsTableWidget.TableFieldUpdater fieldUpdater,
                                                                     EventBus localEventBus) {
        ActionsColumn column = new ActionsColumn(new ActionsCell(summaryTableConfig, localEventBus, currentState));
        column.setFieldUpdater(fieldUpdater);
        return column;
    }

    static class ActionsCell extends AbstractCell<ColumnContext> {
        private static final String DEFAULT_EDIT_ACCESS_CHECKER = "default.edit.access.checker";
        private static final String DEFAULT_DELETE_ACCESS_CHECKER = "default.delete.access.checker";
        private static final String DEFAULT_VIEW_ACCESS_CHECKER = "default.view.access.checker";
        private SummaryTableActionsColumnConfig summaryTableActionsColumnConfig;
        private List<SummaryTableColumnConfig> summaryTableColumnConfigs;
        private EventBus localEventBus;
        private LinkedDomainObjectsTableState currentState;
        private boolean editable;

        public ActionsCell(SummaryTableConfig summaryTableConfig, EventBus localEventBus, LinkedDomainObjectsTableState currentState) {
            super("click");
            this.summaryTableColumnConfigs = summaryTableConfig.getSummaryTableColumnConfigList();
            this.summaryTableActionsColumnConfig = summaryTableConfig.getSummaryTableActionsColumnConfig();
            this.localEventBus = localEventBus;
            this.currentState = currentState;
            editable = localEventBus != null;
        }

        @Override
        public void render(Context context, ColumnContext columnContext, SafeHtmlBuilder sb) {
            final HorizontalPanel container = new HorizontalPanel();
            container.addStyleName("columnWrapper");
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
                        if ((type.equals(ActionTypes.edit.name()) || type.equals(ActionTypes.view_or_edit.name()))
                                && editable) {
                            addEditButton(container, columnContext);
                        } else if (type.equals(ActionTypes.delete.name()) && editable) {
                            addDeleteButton(container, columnContext);
                        } else if (type.equals(ActionTypes.view.name())
                                || (type.equals(ActionTypes.view_or_edit.name()) && !editable)) {
                            addViewButton(container, columnContext);
                        }
                    }
                }
            } else {
                //default actions
                if (shouldDrawDefaultActionButton(ActionTypes.edit, editable)) {
                    addEditButton(container, columnContext);
                }
                if (shouldDrawDefaultActionButton(ActionTypes.delete, editable)) {
                    addDeleteButton(container, columnContext);
                }
            }
            sb.appendHtmlConstant(container.toString());
        }

        private boolean shouldDrawDefaultActionButton(ActionTypes actionType, boolean editable) {
            boolean result = editable;
            if (editable) {
                for (SummaryTableColumnConfig summaryTableColumnConfig : summaryTableColumnConfigs) {
                    SummaryTableActionColumnConfig summaryTableActionColumnConfig = summaryTableColumnConfig.getSummaryTableActionColumnConfig();
                    if (summaryTableActionColumnConfig != null && actionType.name().equalsIgnoreCase(summaryTableActionColumnConfig.getType())) {
                        result = false;
                        break;
                    }
                }
            }

            return result;
        }

        private void displayAction(ColumnDisplayConfig columnDisplayConfig, SummaryTableActionColumnConfig summaryTableActionColumnConfig, HorizontalPanel container) {

            String text = columnDisplayConfig.getColumnDisplayTextConfig().getValue();
            String url = columnDisplayConfig.getColumnDisplayImageConfig().getUrl();
            String componentName = summaryTableActionColumnConfig.getComponentName();
            ActionTypes type = ActionTypes.forName(summaryTableActionColumnConfig.getType());
            if (url != null) {
                Image actionImage = new Image(GlobalThemesManager.getResourceFolder() + url);
                String styleName = type == null ? ACTION_IMAGE_SELECTOR : type.getStyleClassName();
                actionImage.addStyleName(styleName);
                actionImage.addStyleName("linkedTableActionImage");
                actionImage.addStyleName(componentName);
                Panel imageWrapper = new AbsolutePanel();
                imageWrapper.add(actionImage);
                imageWrapper.setStyleName("linkedTableActionImageWrapper");
                container.add(imageWrapper);
            }
            if (text != null) {
                Label actionText = new Label(text);
                actionText.addStyleName(componentName);
                String styleName = type == null ? ACTION_TEXT_SELECTOR : type.getStyleClassName();
                actionText.addStyleName(styleName);
                actionText.addStyleName("linkedTableActionLabel");
                container.add(actionText);
            }

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

        private void addViewButton(final HorizontalPanel container, ColumnContext columnContext) {
            withCheckAccess(DEFAULT_VIEW_ACCESS_CHECKER, columnContext, new CheckAccessCallback() {
                @Override
                public void onSuccess() {
                    container.add(createViewButton());
                }

                @Override
                public void onDenied() {

                }
            });
        }

        @Override
        public void onBrowserEvent(final Context context, Element parent, final ColumnContext columnContext, NativeEvent event,
                                   final ValueUpdater<ColumnContext> valueUpdater) {
            EventTarget eventTarget = event.getEventTarget();
            Element as = Element.as(eventTarget);
            String className = as.getClassName();
            if (summaryTableActionsColumnConfig != null) {
                for (SummaryTableActionColumnConfig summaryTableActionColumnConfig : summaryTableActionsColumnConfig.getSummaryTableActionColumnConfig()) {
                    if (summaryTableActionColumnConfig.getComponentName() == null) {
                        if (as.getClassName().contains(ActionTypes.view.name())
                                || (as.getClassName().contains(ActionTypes.view_or_edit.getStyleClassName()) && !editable)) {
                            createDefaultViewAction(context, columnContext, currentState);
                        }
                    } else if (editable && (as.getClassName().contains(ActionTypes.edit.getStyleClassName())
                            || as.getClassName().contains(ActionTypes.view_or_edit.getStyleClassName()))) {
                        createDefaultEditAction(context, columnContext, currentState, valueUpdater);
                    } else if (as.getClassName().contains(ActionTypes.delete.getStyleClassName())) {
                        createDefaultDeleteAction(context, columnContext, currentState, localEventBus);
                    } else {
                        String componentName = summaryTableActionColumnConfig.getComponentName();
                        if ((className.contains(ACTION_IMAGE_SELECTOR) || className.contains(ACTION_TEXT_SELECTOR)) &&
                                className.contains(componentName)) {
                            LinkedTableAction action = ComponentRegistry.instance.get(componentName);
                            action.perform((columnContext.getObjectId()==null)?columnContext.getRowItem().getObjectId():columnContext.getObjectId(), context.getIndex()
                            );
                        }
                    }
                }
            }
            if (className.contains(ActionTypes.edit.getStyleClassName())) {
                createDefaultEditAction(context, columnContext, currentState, valueUpdater);

            } else if (className.contains(ActionTypes.delete.getStyleClassName())) {
                createDefaultDeleteAction(context, columnContext, currentState, localEventBus);

            }
            super.onBrowserEvent(context, parent, columnContext, event, valueUpdater);
        }


    }

    private static void createDefaultEditAction(final Cell.Context context, final ColumnContext columnContext,
                                                LinkedDomainObjectsTableState state, final ValueUpdater<ColumnContext> valueUpdater) {
        DefaultEditLinkedTableAction action = ComponentRegistry.instance.get(DEFAULT_EDIT_ACTION_COMPONENT);
        FormState rowFormState = obtainFormStateForRow(columnContext, state);
        action.setRowFormState(rowFormState);
        action.setState(state);
        action.setCallback(new PostPerformCallback() {
            @Override
            public void onPerform() {
                valueUpdater.update(columnContext);

            }
        });
        action.perform(columnContext.getObjectId(), context.getIndex());

    }

    private static void createDefaultViewAction(final Cell.Context context, final ColumnContext columnContext,
                                                LinkedDomainObjectsTableState state) {
        DefaultViewLinkedTableAction action = ComponentRegistry.instance.get(DEFAULT_VIEW_ACTION_COMPONENT);
        FormState rowFormState = obtainFormStateForRow(columnContext, state);
        action.setRowFormState(rowFormState);
        action.setState(state);
        action.perform(columnContext.getObjectId(), context.getIndex());

    }

    private static void createDefaultDeleteAction(final Cell.Context context, final ColumnContext columnContext,
                                                  LinkedDomainObjectsTableState state, final EventBus eventBus) {
        LinkedTableAction action = ComponentRegistry.instance.get(DEFAULT_DELETE_ACTION_COMPONENT);
        FormState rowFormState = obtainFormStateForRow(columnContext, state);
        action.setRowFormState(rowFormState);
        action.setCallback(new PostPerformCallback() {
            @Override
            public void onPerform() {
                FieldUpdater<RowItem, ColumnContext> deleteFieldUpdater = createDeleteFieldUpdater(eventBus, false);
                deleteFieldUpdater.update(context.getIndex(), columnContext.getRowItem(), columnContext);
            }
        });
        action.perform(columnContext.getObjectId(), context.getIndex());
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
        deleteButton.addStyleName(ActionTypes.delete.getStyleClassName());
        return deleteButton;
    }

    private static Button createEditButton() {
        Button editButton = new Button();
        editButton.setStyleName(GlobalThemesManager.getCurrentTheme().commonCss().editButton());
        editButton.addStyleName(ActionTypes.edit.getStyleClassName());
        return editButton;
    }

    private static Button createViewButton() {
        Button viewButton = new Button();
        viewButton.setStyleName(GlobalThemesManager.getCurrentTheme().commonCss().viewButton());
        viewButton.addStyleName(ActionTypes.view.getStyleClassName());
        return viewButton;
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
        private EventBus eventBus;
        private boolean editable;

        public MixedCell(SummaryTableColumnConfig summaryTableColumnConfig, LinkedDomainObjectsTableState currentState, EventBus eventBus) {
            super("click");
            this.summaryTableColumnConfig = summaryTableColumnConfig;
            this.currentState = currentState;
            this.eventBus = eventBus;
            editable = eventBus != null;
        }

        @Override
        public void render(Context context, final ColumnContext columnContext, SafeHtmlBuilder safeHtmlBuilder) {
            final HorizontalPanel container = new HorizontalPanel();
            container.addStyleName("columnWrapper");
            final SummaryTableActionColumnConfig summaryTableActionColumnConfig = summaryTableColumnConfig.getSummaryTableActionColumnConfig();
            if (summaryTableActionColumnConfig != null) {
                if (summaryTableActionColumnConfig.getColumnDisplayConfig() != null) {
                    withCheckAccessDo(columnContext, new CheckAccessCallback() {
                        @Override
                        public void onSuccess() {
                            drawColumnAction(summaryTableActionColumnConfig, container, columnContext);
                        }

                        @Override
                        public void onDenied() {

                        }
                    }, summaryTableActionColumnConfig.getAccessChecker());
                } else if (editable || (!ActionTypes.edit.name().equalsIgnoreCase(summaryTableActionColumnConfig.getType())
                        && !ActionTypes.delete.name().equalsIgnoreCase(summaryTableActionColumnConfig.getType()))) {
                    withCheckAccessDo(columnContext, new CheckAccessCallback() {
                        @Override
                        public void onSuccess() {
                            Widget actionableCellText = new Label(columnContext.getValue());
                            ActionTypes type = ActionTypes.forName(summaryTableActionColumnConfig.getType());
                            String styleName = type == null ? ACTION_TEXT_SELECTOR : type.getStyleClassName();
                            actionableCellText.setStyleName("linkedTableActionLabel");
                            actionableCellText.addStyleName(styleName);
                            container.add(actionableCellText);
                        }

                        @Override
                        public void onDenied() {
                            drawNonClickableCell(columnContext.renderRow(), container);
                        }
                    }, summaryTableActionColumnConfig.getAccessChecker());
                } else {
                    drawNonClickableCell(columnContext.renderRow(), container);
                }
            } else {
                drawNonClickableCell(columnContext.renderRow(), container);

            }
            safeHtmlBuilder.appendHtmlConstant(container.toString());
        }

        private void drawNonClickableCell(String text, Panel container) {
            HTML htmlCellText = new HTML(text);
            container.add(htmlCellText);
        }

        private void drawColumnAction(SummaryTableActionColumnConfig summaryTableActionColumnConfig,
                                      HorizontalPanel container, ColumnContext columnContext) {
            ColumnDisplayConfig columnDisplayConfig = summaryTableActionColumnConfig.getColumnDisplayConfig();
            String url = columnDisplayConfig.getColumnDisplayImageConfig() == null ? null
                    : columnDisplayConfig.getColumnDisplayImageConfig().getUrl();
            ActionTypes type = ActionTypes.forName(summaryTableActionColumnConfig.getType());
            if (url != null) {
                Image actionImage = new Image(GlobalThemesManager.getResourceFolder() + url);
                String styleName = type == null ? ACTION_IMAGE_SELECTOR : type.getStyleClassName();
                actionImage.addStyleName(styleName);
                actionImage.addStyleName("linkedTableActionImage");
                Panel imageWrapper = new AbsolutePanel();
                imageWrapper.add(actionImage);
                imageWrapper.setStyleName("linkedTableActionImageWrapper");
                container.add(imageWrapper);
            }
            String text = columnDisplayConfig.getColumnDisplayTextConfig() == null ? null
                    : columnDisplayConfig.getColumnDisplayTextConfig().getValue();
            if (text != null) {
                Label actionText = new Label(text);
                actionText.setStyleName("linkedTableActionLabel");
                String styleName = type == null ? ACTION_TEXT_SELECTOR : type.getStyleClassName();
                actionText.addStyleName(styleName);
                container.add(actionText);
            }
            HTML htmlCellText = new HTML(columnContext.renderRow());
            if (ColumnDisplayConfig.Position.before.name().equals(columnDisplayConfig.getPosition())) {
                container.insert(htmlCellText, 0);
            } else {
                container.add(htmlCellText);
            }

        }

        @Override
        public void onBrowserEvent(Context context, Element parent, ColumnContext columnContext, NativeEvent event,
                                   ValueUpdater<ColumnContext> valueUpdater) {
            EventTarget eventTarget = event.getEventTarget();
            Element as = Element.as(eventTarget);
            if (as.getClassName().contains(ActionTypes.view.getStyleClassName())
                    || (as.getClassName().contains(ActionTypes.view_or_edit.getStyleClassName()) && !editable)) {
                createDefaultViewAction(context, columnContext, currentState);

            } else if (editable && (as.getClassName().contains(ActionTypes.edit.getStyleClassName())
                    || as.getClassName().contains(ActionTypes.view_or_edit.getStyleClassName()))) {
                createDefaultEditAction(context, columnContext, currentState, valueUpdater);
            } else if (editable && as.getClassName().contains(ActionTypes.delete.getStyleClassName())) {
                createDefaultDeleteAction(context, columnContext, currentState, eventBus);
            } else if (as.getClassName().contains(ACTION_IMAGE_SELECTOR) || as.getClassName().contains(ACTION_TEXT_SELECTOR)) {
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
            final String columnId = columnConfig.getColumnId();
            String valueByKey = rowItem.getValueByKey(columnId);
            if (valueByKey == null) {
                valueByKey = "";
            }
            columnContext.setObjectId(rowItem.getObjectId());
            columnContext.setValue(valueByKey);
            columnContext.setRowItem(rowItem);
            return columnContext;
        }
    }

    public static void configureTable(SummaryTableConfig summaryTableConfig, CellTable<RowItem> table,
                                      LinkedDomainObjectsTableState currentState,
                                      LinkedDomainObjectsTableWidget.TableFieldUpdater fieldUpdater,
                                      EventBus eventBus) {
        for (final SummaryTableColumnConfig summaryTableColumnConfig : summaryTableConfig.getSummaryTableColumnConfigList()) {
            MixedContentColumn mixedContentColumn = new MixedContentColumn(new MixedCell(summaryTableColumnConfig, currentState, eventBus),
                    summaryTableColumnConfig);
            mixedContentColumn.setFieldUpdater(fieldUpdater);
            table.addColumn(mixedContentColumn, summaryTableColumnConfig.getHeader());
        }
    }


    public static void configureNoneEditableTable(SummaryTableConfig summaryTableConfig, CellTable<RowItem> table,
                                                  LinkedDomainObjectsTableState currentState) {
        configureTable(summaryTableConfig, table, currentState, null, null);
        table.addColumn(buildActionsColumn(summaryTableConfig, currentState, null, null));
    }

    private static FieldUpdater<RowItem, ColumnContext> createDeleteFieldUpdater(final EventBus localEventBus, final boolean tooltipContent) {
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
