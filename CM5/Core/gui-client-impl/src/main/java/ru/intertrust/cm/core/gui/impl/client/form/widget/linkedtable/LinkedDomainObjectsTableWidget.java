package ru.intertrust.cm.core.gui.impl.client.form.widget.linkedtable;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.SummaryTableConfig;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.FormPlugin;
import ru.intertrust.cm.core.gui.impl.client.IWidgetStateFilter;
import ru.intertrust.cm.core.gui.impl.client.StyledDialogBox;
import ru.intertrust.cm.core.gui.impl.client.event.linkedtable.LinkedTableRowDeletedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.linkedtable.LinkedTableRowDeletedEventHandler;
import ru.intertrust.cm.core.gui.impl.client.form.widget.BaseWidget;
import ru.intertrust.cm.core.gui.impl.client.form.widget.EventBlocker;
import ru.intertrust.cm.core.gui.impl.client.form.widget.hierarchybrowser.TooltipCallback;
import ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.FormState;
import ru.intertrust.cm.core.gui.model.form.widget.*;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

import java.util.Arrays;
import java.util.List;

import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.STATE_KEY;


@ComponentName("linked-domain-objects-table")
public class LinkedDomainObjectsTableWidget extends LinkEditingWidget implements LinkedTableRowDeletedEventHandler {

    private LinkedDomainObjectsTableState currentState;
    private CellTable<RowItem> table;
    private ListDataProvider<RowItem> model;
    private Button tooltipButton;
    private LinkedDomainObjectsTableTooltip tooltip;
    private EventBus localEventBus = new SimpleEventBus();

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
        SummaryTableConfig summaryTableConfig = currentState.getLinkedDomainObjectsTableConfig().getSummaryTableConfig();
        if (isEditable()) {
            LinkedTableUtil.configureEditableTable(summaryTableConfig, table, new TableFieldUpdater(model, false),
                    localEventBus);
        } else {
            LinkedTableUtil.configureNoneEditableTable(summaryTableConfig, table);
        }

        model.addDataDisplay(table);
        drawTooltipButtonIfRequired();
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
        localEventBus.addHandler(LinkedTableRowDeletedEvent.TYPE, this);
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
                convertFormStateAndFillRowItem(formState);

            }
        };
        DialogBoxAction cancelAction = new DialogBoxAction() {
            @Override
            public void execute(FormPlugin formPlugin) {
                // no op
            }
        };
        LinkedFormDialogBoxBuilder lfb = linkedFormDialogBoxBuilder
                .setSaveAction(saveAction)
                .setCancelAction(cancelAction)
                .withHeight(currentState.getLinkedDomainObjectsTableConfig().getModalHeight())
                .withWidth(currentState.getLinkedDomainObjectsTableConfig().getModalWidth())
                .withObjectType(currentState.getObjectTypeName()).buildDialogBox();
        lfb.display();

    }

    private void insertInCorrectModel(RowItem rowItem) {
        if (currentState.shouldDrawTooltipButton()) {
            if (tooltip == null) {
                tooltip = new LinkedDomainObjectsTableTooltip(Arrays.asList(rowItem));
                drawTooltipButtonIfRequired();
            } else {
                tooltip.getTooltipModel().getList().add(rowItem);
                drawTooltipButtonIfRequired();
            }
        } else {
            model.getList().add(rowItem);
        }

    }

    private void drawTooltipButtonIfRequired() {
        VerticalPanel view = (VerticalPanel) impl;
        if (tooltipButton != null) {
            view.remove(tooltipButton);
        }
        if (currentState.shouldDrawTooltipButton()) {
            initTooltipButton();
            view.add(tooltipButton);
        }
    }

    private void convertFormStateAndFillRowItem(final FormState formState) {
        SummaryTableConfig summaryTableConfig = currentState.getLinkedDomainObjectsTableConfig().getSummaryTableConfig();
        RepresentationRequest request = new RepresentationRequest(formState, summaryTableConfig);

        Command command = new Command("convertFormStateToRowItem", getName(), request);
        BusinessUniverseServiceAsync.Impl.executeCommand(command, new AsyncCallback<Dto>() {
            @Override
            public void onSuccess(Dto result) {
                RowItem rowItem = (RowItem) result;
                String stateKey = currentState.addNewFormState(formState);
                rowItem.setParameter(STATE_KEY, stateKey);
                insertInCorrectModel(rowItem);

            }

            @Override
            public void onFailure(Throwable caught) {
                GWT.log("something was going wrong while obtaining hyperlink");
            }
        });
    }

    private void convertFormStateAndUpdateRowItem(final FormState formState, final Integer index, final boolean tooltipContent) {
        SummaryTableConfig summaryTableConfig = currentState.getLinkedDomainObjectsTableConfig().getSummaryTableConfig();
        RepresentationRequest request = new RepresentationRequest(formState, summaryTableConfig);
        if (index != null && currentState.getIds().size() > index) {
            List<Id> ids = Arrays.asList(currentState.getIds().get(index));
            request.setIds(ids);
        }
        Command command = new Command("convertFormStateToRowItem", getName(), request);
        BusinessUniverseServiceAsync.Impl.executeCommand(command, new AsyncCallback<Dto>() {
            @Override
            public void onSuccess(Dto result) {
                RowItem rowItem = (RowItem) result;
                if (tooltipContent) {
                    tooltip.getTooltipModel().getList().set(index, rowItem);
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
        if (tooltip != null) {
            tooltip.showRelativeTo(impl);
            return;
        }
        LinkedTableTooltipRequest request = new LinkedTableTooltipRequest(currentState.getLinkedDomainObjectsTableConfig(),
                currentState.getIds());
        Command command = new Command("fetchWidgetItems", getName(), request);
        BusinessUniverseServiceAsync.Impl.executeCommand(command, new AsyncCallback<Dto>() {
            @Override
            public void onSuccess(Dto result) {
                LinkedTableTooltipResponse response = (LinkedTableTooltipResponse) result;
                List<RowItem> rowItems = response.getRowItems();
                tooltip = new LinkedDomainObjectsTableTooltip(rowItems);
                tooltip.showRelativeTo(impl);
            }

            @Override
            public void onFailure(Throwable caught) {
                GWT.log("something was going wrong while obtaining hyperlink");
            }
        });
    }

    private void getWidgetItems(final TooltipCallback tooltipCallback) {
        LinkedTableTooltipRequest request = new LinkedTableTooltipRequest(currentState.getLinkedDomainObjectsTableConfig(),
                currentState.getIds());
        Command command = new Command("fetchWidgetItems", getName(), request);
        final HandlerRegistration handlerRegistration = Event.addNativePreviewHandler(new EventBlocker(impl));
        BusinessUniverseServiceAsync.Impl.executeCommand(command, new AsyncCallback<Dto>() {
            @Override
            public void onSuccess(Dto result) {
                handlerRegistration.removeHandler();
                LinkedTableTooltipResponse response = (LinkedTableTooltipResponse) result;
                List<RowItem> rowItems = response.getRowItems();
                tooltip = new LinkedDomainObjectsTableTooltip(rowItems);
                tooltipCallback.perform();
            }

            @Override
            public void onFailure(Throwable caught) {
                GWT.log("something was going wrong while obtaining hyperlink");
                handlerRegistration.removeHandler();
            }
        });
    }

    @Override
    public void onLinkedTableRowDeletedEvent(LinkedTableRowDeletedEvent event) {
        RowItem rowItem = event.getRowItem();
        String stateKey = rowItem.getParameter(BusinessUniverseConstants.STATE_KEY);
        boolean tooltipContent = event.isTooltipContent();
        if (tooltipContent) {
            removeFromTooltipContent(rowItem);
        } else {
            removeFromContent(rowItem);
        }

        if (stateKey != null) {
            currentState.removeNewObjectState(stateKey);
            currentState.removeEditedObjectState(stateKey);

        } else {
            // объекта нет в пуле, значит помечаем его для физического удаления
            if (rowItem.getObjectId() != null) {
                currentState.getIds().remove(rowItem.getObjectId());
                currentState.getRowItems().remove(rowItem);

            }
        }


    }

    private void removeFromTooltipContent(RowItem rowItem) {
        tooltip.getTooltipModel().getList().remove(rowItem);
        if (tooltip.getTooltipModel().getList().isEmpty()) {
            tooltip.hide();
            tooltipButton.removeFromParent();
            tooltipButton = null;
        }
    }

    private void removeFromContent(RowItem rowItem) {
        model.getList().remove(rowItem);
        if (currentState.shouldDrawTooltipButton()) {
            if (tooltip == null) {
                getWidgetItems(new TooltipCallback() {
                    @Override
                    public void perform() {
                        tryPoolFromTooltipContent();
                    }
                });
            } else {
                tryPoolFromTooltipContent();
            }
        }
    }

    private void tryPoolFromTooltipContent() {
        List<RowItem> tooltipItems = tooltip.getTooltipModel().getList();
        RowItem rowItemFromTooltip = tooltipItems.get(0);
        model.getList().add(rowItemFromTooltip);
        tooltipItems.remove(rowItemFromTooltip);
        if (tooltipItems.isEmpty()) {
            tooltipButton.removeFromParent();
        }
    }


    private class LinkedDomainObjectsTableTooltip extends PopupPanel {
        private List<RowItem> rowItems;
        private ListDataProvider<RowItem> tooltipModel;

        private LinkedDomainObjectsTableTooltip(List<RowItem> rowItems) {
            super(true);
            this.rowItems = rowItems;
            init();
        }

        public ListDataProvider<RowItem> getTooltipModel() {
            return tooltipModel;
        }

        private void init() {
            CellTable<RowItem> table = new CellTable<>();
            tooltipModel = new ListDataProvider<>();
            for (RowItem rowItem : rowItems) {
                tooltipModel.getList().add(rowItem);
            }
            SummaryTableConfig summaryTableConfig = currentState.getLinkedDomainObjectsTableConfig().getSummaryTableConfig();
            if (isEditable()) {
                LinkedTableUtil.configureEditableTable(summaryTableConfig, table, new TableFieldUpdater(tooltipModel, true),
                        localEventBus);

            } else {
                LinkedTableUtil.configureNoneEditableTable(summaryTableConfig, table);
            }
            tooltipModel.addDataDisplay(table);
            this.add(table);
            this.setStyleName("tooltip-popup");

        }

    }

    public void initTooltipButton() {
        tooltipButton = new Button("..");
        tooltipButton.setStyleName("tooltipButton");
        tooltipButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                getWidgetItems();
            }
        });

    }

    public class TableFieldUpdater implements FieldUpdater<RowItem, String> {
        private ListDataProvider<RowItem> model;
        private boolean tooltipContent;

        private TableFieldUpdater(ListDataProvider<RowItem> model, boolean tooltipContent) {
            this.model = model;
            this.tooltipContent = tooltipContent;
        }

        public ListDataProvider<RowItem> getModel() {
            return model;
        }

        public boolean isTooltipContent() {
            return tooltipContent;
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
                    convertFormStateAndUpdateRowItem(formState, index, tooltipContent);
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
            LinkedFormDialogBoxBuilder lfb;
            if (pooledEditedFormState != null) {
                lfb = new LinkedFormDialogBoxBuilder()
                        .setSaveAction(saveAction)
                        .setCancelAction(cancelAction)
                        .withObjectType(currentState.getObjectTypeName())
                        .withFormState(pooledEditedFormState).
                                withHeight(currentState.getLinkedDomainObjectsTableConfig().getModalHeight())
                        .withWidth(currentState.getLinkedDomainObjectsTableConfig().getModalWidth())
                        .buildDialogBox();

            } else {
                lfb = new LinkedFormDialogBoxBuilder()
                        .setSaveAction(saveAction)
                        .setCancelAction(cancelAction)
                        .withId(object.getObjectId())
                        .withHeight(currentState.getLinkedDomainObjectsTableConfig().getModalHeight())
                        .withWidth(currentState.getLinkedDomainObjectsTableConfig().getModalWidth())
                        .buildDialogBox();
            }
            lfb.display();
        }
    }
}
