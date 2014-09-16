package ru.intertrust.cm.core.gui.impl.client.form.widget.linkedtable;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.ListDataProvider;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.SummaryTableConfig;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.FormPlugin;
import ru.intertrust.cm.core.gui.impl.client.IWidgetStateFilter;
import ru.intertrust.cm.core.gui.impl.client.StyledDialogBox;
import ru.intertrust.cm.core.gui.impl.client.form.widget.BaseWidget;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.FormState;
import ru.intertrust.cm.core.gui.model.form.widget.*;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

import java.util.Arrays;
import java.util.List;

import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.STATE_KEY;


@ComponentName("linked-domain-objects-table")
public class LinkedDomainObjectsTableWidget extends LinkEditingWidget {

    private LinkedDomainObjectsTableState currentState;
    private CellTable<RowItem> table;
    private ListDataProvider<RowItem> model;
    private Button tooltipButton;

    @Override
    public void setCurrentState(WidgetState state) {
        currentState = (LinkedDomainObjectsTableState) state;


        model = new ListDataProvider<>();
        List<RowItem> rowItems = currentState.getRowItems();
        for (RowItem rowItem : rowItems) {
            model.getList().add(rowItem);
        }
        VerticalPanel view = (VerticalPanel) impl;
        if (table != null) {
            view.remove(table);
        }

        table = new CellTable<RowItem>();
        view.add(table);
        if (isEditable()) {
            LinkedTableUtil.configureEditableTable(currentState, table, model, new TableFieldUpdater(model));
        } else {
            LinkedTableUtil.configureNoneEditableTable(currentState, table);
        }

        model.addDataDisplay(table);
        if (tooltipButton != null) {
            view.remove(tooltipButton);
        }
        if (currentState.isShouldDrawTooltipButton()) {

            tooltipButton = getShowTooltipButton();
            view.add(tooltipButton);
        }
    }

    @Override
    protected boolean isChanged() {
        final List<Id> initialValue = ((LinkedDomainObjectsTableState) getInitialData()).getIds();
        final List<Id> currentValue = currentState.getIds();
        return initialValue == null ? currentValue != null : !initialValue.equals(currentValue);
    }

    @Override
    protected Widget asEditableWidget(WidgetState state) {
        VerticalPanel hp = new VerticalPanel();
        Button addButton = createAddButton();
        addButton.removeStyleName("gwt-Button");
        addButton.addStyleName("light-button ldotCreate");
        hp.add(addButton);

        return hp;
    }

    @Override
    protected Widget asNonEditableWidget(WidgetState state) {
        VerticalPanel hp = new VerticalPanel();
        return hp;
    }

    @Override
    protected WidgetState createNewState() {
        return currentState;
    }

    private Button createAddButton() {
        Button button = new Button(""); // была прописана клавиша - Добавить
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

                convertFormStateAndFillRowItem(formState, model, null);

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

    private void convertFormStateAndFillRowItem(final FormState formState, final ListDataProvider<RowItem> model, final Integer index) {
        SummaryTableConfig summaryTableConfig = currentState.getLinkedDomainObjectsTableConfig().getSummaryTableConfig();

        RepresentationRequest request = new RepresentationRequest(formState, summaryTableConfig);
        if(index != null){
            List<Id> ids = Arrays.asList(currentState.getIds().get(index));
            request.setIds(ids);
        }
        Command command = new Command("convertFormStateToRowItem", getName(), request);
        BusinessUniverseServiceAsync.Impl.executeCommand(command, new AsyncCallback<Dto>() {
            @Override
            public void onSuccess(Dto result) {
                RowItem rowItem = (RowItem) result;

                if (index == null) {
                    model.getList().add(rowItem);
                 String stateKey = currentState.addNewFormState(formState);
                 rowItem.setParameter(STATE_KEY, stateKey);
                } else {
                    model.getList().set(index, rowItem);
                }

            }

            @Override
            public void onFailure(Throwable caught) {
                GWT.log("something was going wrong while obtaining hyperlink");
            }
        });
    }

    @Override
    public Component createNew() {
        return new LinkedDomainObjectsTableWidget();
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
            tooltipModel = new ListDataProvider<>();
            for (RowItem rowItem : rowItems) {
                tooltipModel.getList().add(rowItem);
            }
            if (isEditable()) {
                LinkedTableUtil.configureEditableTable(currentState, table, tooltipModel,
                        new TableFieldUpdater(tooltipModel));
            } else {
                LinkedTableUtil.configureNoneEditableTable(currentState, table);
            }
            tooltipModel.addDataDisplay(table);
            this.add(table);
            this.setStyleName("tooltip-popup");

        }

    }

    public Button getShowTooltipButton() {
        Button openTooltip = new Button("..");
        openTooltip.setStyleName("tooltipButton");
        openTooltip.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                getWidgetItems();
            }
        });
        return openTooltip;

    }

    public class TableFieldUpdater implements FieldUpdater<RowItem, String> {
        private ListDataProvider<RowItem> model;

        private TableFieldUpdater(ListDataProvider<RowItem> model) {
            this.model = model;
        }

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
                    convertFormStateAndFillRowItem(formState, model, index);
                    Id id = object.getObjectId();
                    if (id != null) {
                        currentState.putEditedFormState(id.toStringRepresentation(), formState);
                        object.setParameter(STATE_KEY, id.toStringRepresentation());
                    } else {
                        String stateKey = object.getParameter(STATE_KEY);
                        currentState.rewriteNewFormState(stateKey, formState);
                    }

                }
            };
            DialogBoxAction cancelAction = new DialogBoxAction() {
                @Override
                public void execute(FormPlugin formPlugin) {
                    // no op
                }
            };
            String pooledFormStateKey = object.getParameter(STATE_KEY);
            FormState pooledEditedFormState = null;
            Id id = object.getObjectId();
            if (id != null) {
                pooledEditedFormState = currentState.getFromEditedStates(id.toStringRepresentation());
            } else if (pooledFormStateKey != null) {
                pooledEditedFormState = currentState.getFromNewStates(pooledFormStateKey);
            }
            DialogBox db;
            if (pooledEditedFormState != null) {

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
    }
}
