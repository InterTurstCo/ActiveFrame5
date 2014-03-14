package ru.intertrust.cm.core.gui.impl.client.form.widget;

import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.SummaryTableColumnConfig;
import ru.intertrust.cm.core.config.gui.form.widget.SummaryTableConfig;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.FormPlugin;
import ru.intertrust.cm.core.gui.impl.client.IWidgetStateFilter;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.FormState;
import ru.intertrust.cm.core.gui.model.form.widget.*;

import java.util.ArrayList;
import java.util.List;


@ComponentName("linked-domain-objects-table")
public class LinkedDomainObjectsTableWidget extends LinkEditingWidget {

    public static final String STATE_KEY = "stateKey";
    LinkedDomainObjectsTableState currentState;
    ArrayList<Id> selectedIds = new ArrayList<Id>();

    private CellTable<RowItem> table = new CellTable<RowItem>();
    private ListDataProvider<RowItem> model;
    private boolean tableConfigured = false;

    @Override
    public void setCurrentState(WidgetState currentState) {
        this.currentState = (LinkedDomainObjectsTableState) currentState;
        ((LinkedDomainObjectsTableState) currentState).getNewFormStates().clear();
        if (!tableConfigured) {
            configureTable((LinkedDomainObjectsTableState) currentState);
            tableConfigured = true;
        }
        List<RowItem> rowItems = this.currentState.getRowItems();
        selectedIds.clear();
        model = new ListDataProvider<RowItem>();

        for (RowItem rowItem : rowItems) {
            selectedIds.add(rowItem.getObjectId());
            model.getList().add(rowItem);
        }
        this.currentState.setIds(selectedIds);
        model.addDataDisplay(table);

    }

    private void configureTable(LinkedDomainObjectsTableState currentState) {
        final SummaryTableConfig summaryTableConfig = currentState.getLinkedDomainObjectsTableConfig().getSummaryTableConfig();
        for (final SummaryTableColumnConfig summaryTableColumnConfig : summaryTableConfig.getSummaryTableColumnConfig()) {
            TextColumn<RowItem> column = new TextColumn<RowItem>() {
                @Override
                public String getValue(RowItem object) {
                    return object.getValueByKey(summaryTableColumnConfig.getWidgetId());
                }
            };
            table.addColumn(column, summaryTableColumnConfig.getHeader());
        }
        if (isEditable()) {
            table.addColumn(buildEditButtonColumn(), "");
            table.addColumn(buildDeleteButtonColumn(), "");
        }
    }

    @Override
    protected Widget asEditableWidget(WidgetState state) {
        VerticalPanel hp = new VerticalPanel();
        hp.getElement().setAttribute("border", "1");
        Button addButton = createAddButton();
        addButton.removeStyleName("gwt-Button");
        addButton.addStyleName("dialog-box-button");
        hp.add(addButton);
        hp.add(table);
        return hp;
    }

    @Override
    protected Widget asNonEditableWidget(WidgetState state) {
        VerticalPanel hp = new VerticalPanel();
        hp.getElement().setAttribute("border", "1");
        hp.add(table);
        return hp;
    }

    private Column<RowItem, String> buildEditButtonColumn() {
        ButtonCell editButton = new ButtonCell();
        Column<RowItem, String> editButtonColumn = new Column<RowItem, String>(editButton) {
            @Override
            public String getValue(RowItem object) {
                return "Редактировать";
            }
        };

        FieldUpdater<RowItem, String> editButtonClickHandler = new FieldUpdater<RowItem, String>() {
            @Override
            public void update(final int index, final RowItem object, String value) {
                DialogBoxAction saveAction = new DialogBoxAction() {
                    @Override
                    public void execute(FormPlugin formPlugin) {
                        String stateKey = object.getParameter(STATE_KEY);
                        FormState formState = formPlugin.getFormState(new IWidgetStateFilter() {
                            @Override
                            public boolean exclude(BaseWidget widget) {
                                return false;
                            }
                        });
                        convertFormStateAndFillRowItem(formState, object);
                        if (stateKey != null) {
                            currentState.replaceObjectState(stateKey, formState);
                            object.setParameter(STATE_KEY, stateKey);
                        } else {
                            currentState.replaceObjectState(object.getObjectId().toStringRepresentation(), formState);
                            object.setParameter(STATE_KEY, object.getObjectId().toStringRepresentation());
                        }
                        model.getList().set(index, object);
                    }
                };
                DialogBoxAction cancelAction = new DialogBoxAction() {
                    @Override
                    public void execute(FormPlugin formPlugin) {
                        // no op
                    }
                };
                // check if form plugin ever was in pool
                String pooledFormStateKey = object.getParameter(STATE_KEY);
                DialogBox db;
                if (pooledFormStateKey != null) {
                    FormState pooledFormState = currentState.getFormState(pooledFormStateKey);
                    db = new LinkedFormDialogBoxBuilder()
                            .setSaveAction(saveAction)
                            .setCancelAction(cancelAction)
                            .withObjectType(currentState.getObjectTypeName())
                            .withFormState(pooledFormState).
                                    withHeight(currentState.getLinkedDomainObjectsTableConfig().getModalHeight())
                            .withWidth(currentState.getLinkedDomainObjectsTableConfig().getModalWidth())
                            .buildDialogBox();

                } else {
                    db = new LinkedFormDialogBoxBuilder()
                            .setSaveAction(saveAction)
                            .setCancelAction(cancelAction)
                            .withId(object.getObjectId())
                            .withHeight(currentState.getLinkedDomainObjectsTableConfig().getModalHeight())
                            .withWidth(currentState.getLinkedDomainObjectsTableConfig().getModalWidth())
                            .buildDialogBox();
                }
                db.center();
                db.show();
            }
        };
        editButtonColumn.setFieldUpdater(editButtonClickHandler);
        return editButtonColumn;
    }

    private Column<RowItem, String> buildDeleteButtonColumn() {
        ButtonCell deleteButton = new ButtonCell();
        Column<RowItem, String> deleteButtonColumn = new Column<RowItem, String>(deleteButton) {
            @Override
            public String getValue(RowItem object) {
                return "Удалить";
            }
        };
        deleteButtonColumn.setFieldUpdater(new FieldUpdater<RowItem, String>() {
            @Override
            public void update(int index, RowItem object, String value) {
                String stateKey = object.getParameter(STATE_KEY);
                if (stateKey != null) {
                    currentState.removeObjectState(stateKey);
                    model.getList().remove(index);
                    if (object.getObjectId() != null) {
                        currentState.addIdForDeletion(object.getObjectId());
                    }
                } else {
                    // объекта нет в пуле, значит помечаем его для физического удаления
                    if (object.getObjectId() != null) {
                        model.getList().remove(object);
                        selectedIds.remove(object.getObjectId());
                    }
                }
                table.redraw();
            }
        });
        return deleteButtonColumn;
    }

    @Override
    protected WidgetState createNewState() {
        return this.currentState;
    }

    private Button createAddButton() {
        Button button = new Button("Добавить");
        button.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                LinkedFormDialogBoxBuilder linkedFormDialogBoxBuilder = new LinkedFormDialogBoxBuilder();
                DialogBoxAction saveAction = new DialogBoxAction() {
                    @Override
                    public void execute(FormPlugin formPlugin) {
                        FormState formState = formPlugin.getFormState(new IWidgetStateFilter() {
                            @Override
                            public boolean exclude(BaseWidget widget) {
                                return false;
                            }
                        });
                        String stateKey = currentState.addObjectState(formState);
                        RowItem rowItem = new RowItem();
                        convertFormStateAndFillRowItem(formState, rowItem);
                        rowItem.setParameter(STATE_KEY, stateKey);
                        model.getList().add(rowItem);
                    }
                };
                DialogBoxAction cancelAction = new DialogBoxAction() {
                    @Override
                    public void execute(FormPlugin formPlugin) {
                        // no op
                    }
                };
                DialogBox db = linkedFormDialogBoxBuilder
                        .setSaveAction(saveAction)
                        .setCancelAction(cancelAction)
                        .withHeight(currentState.getLinkedDomainObjectsTableConfig().getModalHeight())
                        .withWidth(currentState.getLinkedDomainObjectsTableConfig().getModalWidth())
                        .withObjectType(currentState.getObjectTypeName()).buildDialogBox();

                db.center();
                db.show();

            }
        });
        return button;
    }

    private void convertFormStateAndFillRowItem(FormState createdObjectState, RowItem item) {
        for (SummaryTableColumnConfig summaryTableColumnConfig : currentState.getLinkedDomainObjectsTableConfig()
                .getSummaryTableConfig().getSummaryTableColumnConfig()) {
            WidgetState widgetState = createdObjectState.getFullWidgetsState().get(summaryTableColumnConfig.getWidgetId());
            if (widgetState != null) {
                if (widgetState instanceof TextState) {
                    TextState textBoxState = (TextState) widgetState;
                    item.setValueByKey(summaryTableColumnConfig.getWidgetId(), textBoxState.getText());
                    ;
                } else if (widgetState instanceof IntegerBoxState) {
                    IntegerBoxState integerBoxState = (IntegerBoxState) widgetState;
                    item.setValueByKey(summaryTableColumnConfig.getWidgetId(), integerBoxState.getNumber().toString());
                }

            }
        }
    }

    @Override
    public Component createNew() {
        return new LinkedDomainObjectsTableWidget();
    }

}
