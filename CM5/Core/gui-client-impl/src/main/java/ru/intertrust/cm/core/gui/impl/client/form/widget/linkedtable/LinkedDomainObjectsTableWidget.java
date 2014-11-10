package ru.intertrust.cm.core.gui.impl.client.form.widget.linkedtable;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.LinkedFormConfig;
import ru.intertrust.cm.core.config.gui.form.widget.SummaryTableConfig;
import ru.intertrust.cm.core.config.gui.form.widget.linkediting.CreatedObjectConfig;
import ru.intertrust.cm.core.config.gui.form.widget.linkediting.CreatedObjectsConfig;
import ru.intertrust.cm.core.config.gui.form.widget.linkediting.LinkedFormMappingConfig;
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
import ru.intertrust.cm.core.gui.model.form.widget.LinkedDomainObjectsTableState;
import ru.intertrust.cm.core.gui.model.form.widget.LinkedTableTooltipRequest;
import ru.intertrust.cm.core.gui.model.form.widget.LinkedTableTooltipResponse;
import ru.intertrust.cm.core.gui.model.form.widget.RepresentationRequest;
import ru.intertrust.cm.core.gui.model.form.widget.RowItem;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

import java.util.Arrays;
import java.util.LinkedHashMap;
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
    private Button addButton;
    private boolean hasRemovedItems;

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
        if (addButton != null) {
            addHandlersToAddButton(addButton);
        }
    }

    @Override
    protected boolean isChanged() {

        LinkedHashMap<String, FormState> editedNestedFormStates = currentState.getEditedNestedFormStates();
        LinkedHashMap<String, FormState> newFormStates = currentState.getNewFormStates();

        return hasRemovedItems || !editedNestedFormStates.isEmpty()
                || !newFormStates.isEmpty();
    }
        @Override
        protected Widget asEditableWidget (WidgetState state){
            VerticalPanel hp = new VerticalPanel();
            addButton = createAddButton();
            addButton.removeStyleName("gwt-Button");
            addButton.addStyleName("lightButton ldotCreate");
            hp.add(addButton);
            localEventBus.addHandler(LinkedTableRowDeletedEvent.TYPE, this);
            return hp;
        }

        @Override
        protected Widget asNonEditableWidget (WidgetState state){
            VerticalPanel hp = new VerticalPanel();
            return hp;
        }

        @Override
        protected WidgetState createNewState () {
            return currentState;
        }

    private Button createAddButton() {
        Button button = new Button(""); // была прописана клавиша - Добавить
        return button;
    }

    private void addHandlersToAddButton(Button button) {
        final CreatedObjectsConfig createdObjectsConfig = currentState.getLinkedDomainObjectsTableConfig().getCreatedObjectsConfig();
        if (createdObjectsConfig != null && !createdObjectsConfig.getCreateObjectConfigs().isEmpty()) {
            button.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    SelectDomainObjectTypePopup selectDomainObjectTypePopup = new SelectDomainObjectTypePopup(createdObjectsConfig);
                    selectDomainObjectTypePopup.show();
                }
            });
        } else {
            button.addClickHandler(new OpenFormClickHandler(currentState.getObjectTypeName(), null));
        }
    }

    class OpenFormClickHandler implements ClickHandler {
        private String domainObjectType;
        private PopupPanel sourcePopup;

        OpenFormClickHandler(String domainObjectType, PopupPanel sourcePopup) {
            this.domainObjectType = domainObjectType;
            this.sourcePopup = sourcePopup;
        }

        @Override
        public void onClick(ClickEvent event) {
            if (currentState.isSingleChoice() && model.getList().size() >= 1) {
                final StyledDialogBox rewriteAlertDialog =
                        new StyledDialogBox("Текущий обьект будет перезаписан\n Продолжить?");
                rewriteAlertDialog.addOkButtonClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        rewriteAlertDialog.hide();
                        showNewForm(domainObjectType);
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
                showNewForm(domainObjectType);
            }
            if (sourcePopup != null) {
                sourcePopup.hide();
            }
        }
    }

    class SelectDomainObjectTypePopup extends PopupPanel {
        SelectDomainObjectTypePopup(CreatedObjectsConfig createdObjectsConfig) {
            super(true, false);
            this.setPopupPosition(addButton.getAbsoluteLeft() - 48, addButton.getAbsoluteTop() + 40);
            AbsolutePanel header = new AbsolutePanel();
            header.setStyleName("srch-corner");
            final VerticalPanel body = new VerticalPanel();
            AbsolutePanel container = new AbsolutePanel();
            container.setStyleName("settings-popup");
            container.getElement().getStyle().clearOverflow();

            for (CreatedObjectConfig createdObjectConfig : createdObjectsConfig.getCreateObjectConfigs()) {
                final AbsolutePanel menuItemContainer = new AbsolutePanel();
                menuItemContainer.setStyleName("settings-item");
                menuItemContainer.add(new Label(createdObjectConfig.getText()));
                menuItemContainer.addDomHandler(new OpenFormClickHandler(createdObjectConfig.getDomainObjectType(), this), ClickEvent.getType());
                body.add(menuItemContainer);
            }
            container.add(header);
            container.add(body);
            this.add(container);
        }
    }

    private void showNewForm(String domainObjectType) {
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
                .withHeight(getModalHeight(domainObjectType))
                .withWidth(getModalWidth(domainObjectType))
                .withObjectType(domainObjectType)
                .withLinkedFormMapping(currentState.getLinkedDomainObjectsTableConfig().getLinkedFormMappingConfig())
                .withPopupTitlesHolder(currentState.getPopupTitlesHolder()).buildDialogBox();

        lfb.display();

    }

    private String getModalHeight(String domainObjectType) {
        LinkedFormConfig linkedFormConfig = currentState.getLinkedDomainObjectsTableConfig().getLinkedFormConfig();
        if (linkedFormConfig != null && linkedFormConfig.getModalHeight() != null) {
            return linkedFormConfig.getModalHeight();
        }
        if (domainObjectType != null) {
            linkedFormConfig = getLinkedFormConfig(domainObjectType, currentState.getLinkedDomainObjectsTableConfig().getLinkedFormMappingConfig());
            if (linkedFormConfig != null && linkedFormConfig.getModalHeight() != null) {
                return linkedFormConfig.getModalHeight();
            }
        }
        return currentState.getLinkedDomainObjectsTableConfig().getModalHeight();
    }

    private String getModalWidth(String domainObjectType) {
        LinkedFormConfig linkedFormConfig = currentState.getLinkedDomainObjectsTableConfig().getLinkedFormConfig();
        if (linkedFormConfig != null && linkedFormConfig.getModalHeight() != null) {
            return linkedFormConfig.getModalWidth();
        }
        if (domainObjectType != null) {
            linkedFormConfig = getLinkedFormConfig(domainObjectType, currentState.getLinkedDomainObjectsTableConfig().getLinkedFormMappingConfig());
            if (linkedFormConfig != null && linkedFormConfig.getModalWidth() != null) {
                return linkedFormConfig.getModalWidth();
            }
        }
        return currentState.getLinkedDomainObjectsTableConfig().getModalWidth();
    }

    private LinkedFormConfig getLinkedFormConfig(String domainObjectType, LinkedFormMappingConfig mappingConfig) {
        if (mappingConfig != null) {
            for (LinkedFormConfig linkedFormConfig : mappingConfig.getLinkedFormConfigs()) {
                if (domainObjectType.equals(linkedFormConfig.getDomainObjectType())) {
                    return linkedFormConfig;
                }
            }
        }
        return null;
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
        LinkedFormMappingConfig linkedFormMappingConfig = currentState.getLinkedDomainObjectsTableConfig().getLinkedFormMappingConfig();
        String linkedFormName = findLinkedFormName(formState, linkedFormMappingConfig);
        request.setLinkedFormName(linkedFormName);
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
                GWT.log("something was going wrong while obtaining hyperlink", caught);
            }
        });
    }

    private String findLinkedFormName(FormState formState, LinkedFormMappingConfig linkedFormMappingConfig) {
        if (linkedFormMappingConfig != null) {
            for (LinkedFormConfig linkedFormConfig : linkedFormMappingConfig.getLinkedFormConfigs()) {
                if (linkedFormConfig.getDomainObjectType().equalsIgnoreCase(formState.getRootDomainObjectType())) {
                    return linkedFormConfig.getName();
                }
            }
        }
        return null;
    }

    private void convertFormStateAndUpdateRowItem(final FormState formState, final Integer index, final boolean tooltipContent) {
        SummaryTableConfig summaryTableConfig = currentState.getLinkedDomainObjectsTableConfig().getSummaryTableConfig();
        RepresentationRequest request = new RepresentationRequest(formState, summaryTableConfig);
        request.setLinkedFormName(findLinkedFormName(formState, currentState.getLinkedDomainObjectsTableConfig().getLinkedFormMappingConfig()));
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
                GWT.log("something was going wrong while obtaining hyperlink", caught);
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
                GWT.log("something was going wrong while obtaining hyperlink", caught);
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
                GWT.log("something was going wrong while obtaining hyperlink", caught);
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
                hasRemovedItems = true;

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
            this.setStyleName("tooltipPopup");

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
                        .withObjectType(pooledEditedFormState.getRootDomainObjectType())
                        .withFormState(pooledEditedFormState).
                                withHeight(getModalHeight(pooledEditedFormState.getRootDomainObjectType()))
                        .withWidth(getModalWidth(pooledEditedFormState.getRootDomainObjectType()))
                        .withPopupTitlesHolder(currentState.getPopupTitlesHolder())
                        .withLinkedFormMapping(currentState.getLinkedDomainObjectsTableConfig().getLinkedFormMappingConfig())
                        .buildDialogBox();

            } else {
                lfb = new LinkedFormDialogBoxBuilder()
                        .setSaveAction(saveAction)
                        .setCancelAction(cancelAction)
                        .withId(object.getObjectId())
                        .withHeight(getModalHeight(null))
                        .withWidth(getModalWidth(null))
                        .withPopupTitlesHolder(currentState.getPopupTitlesHolder())
                        .withLinkedFormMapping(currentState.getLinkedDomainObjectsTableConfig().getLinkedFormMappingConfig())
                        .buildDialogBox();
            }
            lfb.display();
        }
    }

}
