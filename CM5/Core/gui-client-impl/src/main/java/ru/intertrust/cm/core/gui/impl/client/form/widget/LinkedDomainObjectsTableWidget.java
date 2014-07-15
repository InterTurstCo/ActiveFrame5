package ru.intertrust.cm.core.gui.impl.client.form.widget;

import java.util.List;
import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.SummaryTableColumnConfig;
import ru.intertrust.cm.core.config.gui.form.widget.SummaryTableConfig;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.FormPlugin;
import ru.intertrust.cm.core.gui.impl.client.IWidgetStateFilter;
import ru.intertrust.cm.core.gui.impl.client.StyledDialogBox;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.FormState;
import ru.intertrust.cm.core.gui.model.form.widget.AttachmentBoxState;
import ru.intertrust.cm.core.gui.model.form.widget.IntegerBoxState;
import ru.intertrust.cm.core.gui.model.form.widget.LinkEditingWidgetState;
import ru.intertrust.cm.core.gui.model.form.widget.LinkedDomainObjectsTableState;
import ru.intertrust.cm.core.gui.model.form.widget.LinkedTableTooltipRequest;
import ru.intertrust.cm.core.gui.model.form.widget.LinkedTableTooltipResponse;
import ru.intertrust.cm.core.gui.model.form.widget.RepresentationRequest;
import ru.intertrust.cm.core.gui.model.form.widget.RepresentationResponse;
import ru.intertrust.cm.core.gui.model.form.widget.RowItem;
import ru.intertrust.cm.core.gui.model.form.widget.TextState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;


@ComponentName("linked-domain-objects-table")
public class LinkedDomainObjectsTableWidget extends LinkEditingWidget {

    public static final String STATE_KEY = "stateKey";
    private LinkedDomainObjectsTableState currentState;
    private CellTable<RowItem> table = new CellTable<>();
    private ListDataProvider<RowItem> model;
    private boolean tableConfigured = false;
    private boolean isButtonDrawn;
    @Override
    public void setCurrentState(WidgetState state) {
        currentState = (LinkedDomainObjectsTableState) state;
        currentState.getNewFormStates().clear();
        if (!tableConfigured) {
            configureTable(currentState.getLinkedDomainObjectsTableConfig().getSummaryTableConfig());
            tableConfigured = true;

        }
        List<RowItem> rowItems = currentState.getRowItems();
        model = new ListDataProvider<>();

        for (RowItem rowItem : rowItems) {
            model.getList().add(rowItem);
        }
        model.addDataDisplay(table);
        if (currentState.isShouldDrawTooltipButton() && !isButtonDrawn) {
            isButtonDrawn = true;
            Button tooltipButton = getShowTooltipButton();
            ((VerticalPanel) impl).add(tooltipButton);
        }
    }

    private void configureTable(SummaryTableConfig summaryTableConfig) {

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
        Button addButton = createAddButton();
        addButton.removeStyleName("gwt-Button");
        addButton.addStyleName("dark-button");
        hp.add(addButton);
        hp.add(table);
        return hp;
    }

    @Override
    protected Widget asNonEditableWidget(WidgetState state) {
        VerticalPanel hp = new VerticalPanel();
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
                        FormState formState = formPlugin.getFormState(new IWidgetStateFilter() {
                            @Override
                            public boolean exclude(BaseWidget widget) {
                                return false;
                            }
                        }, true);
                        convertFormStateAndFillRowItem(formState, object, model);
                        Id id = object.getObjectId();
                        if (id != null) {
                            currentState.putEditedFormState(id.toStringRepresentation(), formState);
                            object.setParameter(STATE_KEY, id.toStringRepresentation());
                        } else {
                            String stateKey = object.getParameter(STATE_KEY);
                            currentState.rewriteNewFormState(stateKey, formState);
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
                String pooledFormStateKey = object.getParameter(STATE_KEY);
                DialogBox db;
                if (pooledFormStateKey != null) {
                    FormState pooledEditedFormState = currentState.getFromNewStates(pooledFormStateKey);
                    db = new LinkedFormDialogBoxBuilder()
                            .setSaveAction(saveAction)
                            .setCancelAction(cancelAction)
                            .withObjectType(currentState.getObjectTypeName())
                            .withFormState(pooledEditedFormState).
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
                    currentState.removeNewObjectState(stateKey);
                    currentState.removeEditedObjectState(stateKey);
                    model.getList().remove(index);
                } else {
                    // объекта нет в пуле, значит помечаем его для физического удаления
                    if (object.getObjectId() != null) {
                        currentState.getIds().remove(object.getObjectId());
                        model.getList().remove(object);
                    }
                }
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
                if (currentState.isSingleChoice() && model.getList().size() >= 1) {
                    final StyledDialogBox rewriteAlertDialog =
                            new StyledDialogBox("Текущий обьект будет перезаписан\n Продолжить?");
                    rewriteAlertDialog.addOkButtonClickHandler(new ClickHandler() {
                        @Override
                        public void onClick(ClickEvent event) {
                            rewriteAlertDialog.hide();
                            showNewForm();
                        }
                    });
                    rewriteAlertDialog.addCancelButtonClickHandler(new ClickHandler() {
                        @Override
                        public void onClick(ClickEvent event) {
                            rewriteAlertDialog.hide();
                        }
                    });
                    rewriteAlertDialog.center();
                } else {
                    showNewForm();
                }
            }
        });
        return button;
    }

    private void showNewForm() {
        LinkedFormDialogBoxBuilder linkedFormDialogBoxBuilder = new LinkedFormDialogBoxBuilder();
        DialogBoxAction saveAction = new DialogBoxAction() {
            @Override
            public void execute(FormPlugin formPlugin) {
                if (currentState.isSingleChoice() && model.getList().size() >= 1) {
                    currentState.clearPreviousStates();
                    model.getList().clear();

                }
                FormState formState = formPlugin.getFormState(new IWidgetStateFilter() {
                    @Override
                    public boolean exclude(BaseWidget widget) {
                        return false;
                    }
                }, true);
                String stateKey = currentState.addNewFormState(formState);
                RowItem rowItem = new RowItem();
                convertFormStateAndFillRowItem(formState, rowItem, model);
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

    private void convertFormStateAndFillRowItem(FormState createdObjectState, RowItem item, ListDataProvider<RowItem> model) {
        for (SummaryTableColumnConfig summaryTableColumnConfig : currentState.getLinkedDomainObjectsTableConfig()
                .getSummaryTableConfig().getSummaryTableColumnConfig()) {
            WidgetState widgetState = createdObjectState.getFullWidgetsState().get(summaryTableColumnConfig.getWidgetId());
            if (widgetState != null) {
                if (widgetState instanceof TextState) {
                    TextState textBoxState = (TextState) widgetState;
                    String text = textBoxState.getText();
                    if (text != null) {
                        item.setValueByKey(summaryTableColumnConfig.getWidgetId(), text);
                    }

                } else if (widgetState instanceof IntegerBoxState) {
                    IntegerBoxState integerBoxState = (IntegerBoxState) widgetState;
                    Long number = integerBoxState.getNumber();
                    if (number != null) {
                        item.setValueByKey(summaryTableColumnConfig.getWidgetId(), number.toString());
                    }
                } else if (widgetState instanceof LinkEditingWidgetState && !(widgetState instanceof AttachmentBoxState)) {
                    LinkEditingWidgetState linkEditingWidgetState = (LinkEditingWidgetState) widgetState;
                    List<Id> ids = linkEditingWidgetState.getIds();
                    String selectionPattern = summaryTableColumnConfig.getPatternConfig().getValue();
                    getRepresentation(item, summaryTableColumnConfig.getWidgetId(), ids, selectionPattern, model);
                }
            }
        }
    }

    @Override
    public Component createNew() {
        return new LinkedDomainObjectsTableWidget();
    }

    private void getRepresentation(final RowItem item, final String widgetId, List<Id> ids,
                                   String selectionPattern, final ListDataProvider<RowItem> model) {
        SummaryTableConfig summaryTableConfig = currentState.getLinkedDomainObjectsTableConfig().getSummaryTableConfig();
        RepresentationRequest request = new RepresentationRequest(ids, selectionPattern, summaryTableConfig);
        Command command = new Command("getRepresentation", "representation-updater", request);
        BusinessUniverseServiceAsync.Impl.executeCommand(command, new AsyncCallback<Dto>() {
            @Override
            public void onSuccess(Dto result) {
                RepresentationResponse response = (RepresentationResponse) result;
                String representation = response.getRepresentation();
                item.setValueByKey(widgetId, representation);
                model.refresh();
            }

            @Override
            public void onFailure(Throwable caught) {
                GWT.log("something was going wrong while obtaining hyperlink");
            }
        });
    }

    private void getWidgetItems() {
        LinkedTableTooltipRequest request = new LinkedTableTooltipRequest(currentState.getLinkedDomainObjectsTableConfig(),
                currentState.getIds());
        Command command = new Command("fetchWidgetItems", getName(), request);
        BusinessUniverseServiceAsync.Impl.executeCommand(command, new AsyncCallback<Dto>() {
            @Override
            public void onSuccess(Dto result) {
                LinkedTableTooltipResponse response = (LinkedTableTooltipResponse) result;
                List<RowItem> rowItems = response.getRowItems();
                LinkedDomainObjectsTableTooltip tooltip = new LinkedDomainObjectsTableTooltip(rowItems);
                tooltip.showRelativeTo(impl);
            }

            @Override
            public void onFailure(Throwable caught) {
                GWT.log("something was going wrong while obtaining hyperlink");
            }
        });
    }

    private class LinkedDomainObjectsTableTooltip extends PopupPanel {
        private List<RowItem> rowItems;
        private ListDataProvider<RowItem> tooltipModel;

        private LinkedDomainObjectsTableTooltip(List<RowItem> rowItems) {

            super(true);
            this.rowItems = rowItems;

            init();
        }

        private void init() {
            CellTable<RowItem> table = new CellTable<>();
            configureTable(currentState.getLinkedDomainObjectsTableConfig().getSummaryTableConfig(), table);
            tooltipModel = new ListDataProvider<>();
            for (RowItem rowItem : rowItems) {
                tooltipModel.getList().add(rowItem);
            }
            tooltipModel.addDataDisplay(table);
            this.add(table);
            this.setStyleName("tooltip-popup");

        }

        private void configureTable(SummaryTableConfig summaryTableConfig, CellTable<RowItem> table) {
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

                    currentState.getIds().remove(object.getObjectId());
                    tooltipModel.getList().remove(object);

                }
            });
            return deleteButtonColumn;
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
                            FormState formState = formPlugin.getFormState(new IWidgetStateFilter() {
                                @Override
                                public boolean exclude(BaseWidget widget) {
                                    return false;
                                }
                            }, true);
                            convertFormStateAndFillRowItem(formState, object, tooltipModel);
                            Id id = object.getObjectId();

                            currentState.putEditedFormState(id.toStringRepresentation(), formState);
                            object.setParameter(STATE_KEY, id.toStringRepresentation());
                            tooltipModel.getList().set(index, object);
                        }
                    };
                    DialogBoxAction cancelAction = new DialogBoxAction() {
                        @Override
                        public void execute(FormPlugin formPlugin) {
                            // no op
                        }
                    };
                    String pooledFormStateKey = object.getParameter(STATE_KEY);


                    FormState pooledEditedFormState = currentState.getFromNewStates(pooledFormStateKey);
                    DialogBox db = new LinkedFormDialogBoxBuilder()
                            .setSaveAction(saveAction)
                            .setCancelAction(cancelAction)
                            .withObjectType(currentState.getObjectTypeName())
                            .withFormState(pooledEditedFormState).
                                    withHeight(currentState.getLinkedDomainObjectsTableConfig().getModalHeight())
                            .withWidth(currentState.getLinkedDomainObjectsTableConfig().getModalWidth())
                            .buildDialogBox();


                    db.center();
                    db.show();
                }
            };
            editButtonColumn.setFieldUpdater(editButtonClickHandler);
            return editButtonColumn;
        }
    }

    public Button getShowTooltipButton() {
        Button openTooltip = new Button("..");
        openTooltip.setStyleName("tooltip-button");
        openTooltip.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                getWidgetItems();
            }
        });
        return openTooltip;

    }
}
