package ru.intertrust.cm.core.gui.impl.client.form.widget.linkediting;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.UIObject;
import ru.intertrust.cm.core.business.api.dto.Case;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObject;
import ru.intertrust.cm.core.business.api.dto.form.PopupTitlesHolder;
import ru.intertrust.cm.core.config.gui.form.widget.LinkEditingWidgetConfig;
import ru.intertrust.cm.core.config.gui.form.widget.linkediting.CreatedObjectConfig;
import ru.intertrust.cm.core.config.gui.form.widget.linkediting.CreatedObjectsConfig;
import ru.intertrust.cm.core.config.gui.form.widget.linkediting.LinkedFormMappingConfig;
import ru.intertrust.cm.core.gui.api.client.LocalizeUtil;
import ru.intertrust.cm.core.gui.impl.client.FormPlugin;
import ru.intertrust.cm.core.gui.impl.client.action.SaveAction;
import ru.intertrust.cm.core.gui.impl.client.event.*;
import ru.intertrust.cm.core.gui.impl.client.form.widget.SelectTypePopup;
import ru.intertrust.cm.core.gui.impl.client.form.widget.buttons.ConfiguredButton;
import ru.intertrust.cm.core.gui.impl.client.form.widget.buttons.LinkCreatingButton;
import ru.intertrust.cm.core.gui.impl.client.form.widget.hyperlink.FormDialogBox;
import ru.intertrust.cm.core.gui.impl.client.form.widget.tooltip.EditableTooltipWidget;
import ru.intertrust.cm.core.gui.impl.client.util.GuiUtil;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.form.widget.LinkCreatorWidgetState;
import ru.intertrust.cm.core.gui.model.form.widget.RepresentationRequest;
import ru.intertrust.cm.core.gui.model.form.widget.RepresentationResponse;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginConfig;
import ru.intertrust.cm.core.gui.model.util.WidgetUtil;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static ru.intertrust.cm.core.config.localization.LocalizationKeys.CANCELLATION_BUTTON_KEY;
import static ru.intertrust.cm.core.config.localization.LocalizationKeys.SAVE_BUTTON_KEY;
import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.CANCELLATION_BUTTON;
import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.SAVE_BUTTON;
import static ru.intertrust.cm.core.gui.impl.client.util.GuiUtil.createNewFormPluginConfig;
import static ru.intertrust.cm.core.gui.impl.client.util.GuiUtil.createSaveAction;
/**
 * @author Yaroslav Bondarchuk
 *         Date: 26.10.2014
 *         Time: 14:10
 */
public abstract class LinkCreatorWidget extends EditableTooltipWidget {
    protected ClickAction clickAction;

    protected ConfiguredButton getCreateButton(LinkCreatorWidgetState state) {

        LinkEditingWidgetConfig config = (LinkEditingWidgetConfig) state.getWidgetConfig();
        CreatedObjectsConfig createdObjectsConfig = config.getCreatedObjectsConfig();
        ConfiguredButton button = null;
        if (createdObjectsConfig == null) {
            return button;
        }
        List<CreatedObjectConfig> createdObjectConfigs = createdObjectsConfig.getCreateObjectConfigs();
        if (WidgetUtil.isNotEmpty(createdObjectConfigs)) {
            button = new LinkCreatingButton(config.getCreateNewButtonConfig());
            Map<String, PopupTitlesHolder> typeTitleMap = state.getTypeTitleMap();
            LinkedFormMappingConfig linkedFormMappingConfig = config.getLinkedFormMappingConfig();
            if (createdObjectConfigs.size() == 1) {
                CreatedObjectConfig createdObjectConfig = createdObjectConfigs.get(0);
                String domainObjectType = createdObjectConfig.getDomainObjectType();
                PopupTitlesHolder popupTitlesHolder = typeTitleMap != null ? typeTitleMap.get(Case.toLower(domainObjectType)) : null;
                String title = popupTitlesHolder == null ? null : popupTitlesHolder.getTitleNewObject();
                createSimpleClickAction(title, domainObjectType, linkedFormMappingConfig, state.getParentWidgetIdsForNewFormMap());
            } else {
                createPopupShowingClickAction(createdObjectsConfig, typeTitleMap, linkedFormMappingConfig, button,
                        state.getParentWidgetIdsForNewFormMap());

            }
            localEventBus.addHandler(UpdateCollectionEvent.TYPE, new UpdateCollectionEventHandler() {
                @Override
                public void updateCollection(UpdateCollectionEvent event) {
                    updateWidgetView(event.getIdentifiableObject());
                }
            });

        }

        return button;

    }

    protected ClickAction getClickAction() {
        return clickAction;

    }

    private void createSimpleClickAction(final String title, final String domainObjectType,
                                         final LinkedFormMappingConfig mappingConfig,
                                         final Map<String, Collection<String>> parentWidgetIdsForNewFormMap) {
        clickAction = new ClickAction() {
            @Override
            public void perform() {
                createAndShowFormDialogBox(title, domainObjectType, mappingConfig, parentWidgetIdsForNewFormMap);
            }
        };

    }

    private void createPopupShowingClickAction(final CreatedObjectsConfig createdObjectsConfig,
                                               final Map<String, PopupTitlesHolder> typeTitleMap,
                                               final LinkedFormMappingConfig linkedFormMappingConfig,
                                               final UIObject uiObject,
                                               final Map<String, Collection<String>> parentWidgetIdsForNewFormMap) {
        clickAction = new ClickAction() {
            @Override
            public void perform() {
                final SelectTypePopup selectTypePopup = new SelectTypePopup(createdObjectsConfig, localEventBus);
                localEventBus.addHandler(DomainObjectTypeSelectedEvent.TYPE, new DomainObjectTypeSelectedEventHandler() {
                    @Override
                    public void onDomainObjectTypeSelected(DomainObjectTypeSelectedEvent event) {
                        if (event.getSource().equals(selectTypePopup)) {
                            String domainObjectType = event.getDomainObjectType();
                            PopupTitlesHolder popupTitlesHolder = typeTitleMap != null ? typeTitleMap.get(domainObjectType) : null;
                            String title = popupTitlesHolder == null ? null : popupTitlesHolder.getTitleNewObject();
                            createAndShowFormDialogBox(title, domainObjectType, linkedFormMappingConfig, parentWidgetIdsForNewFormMap);
                        }
                    }
                });
                selectTypePopup.showRelativeTo(uiObject);

            }
        };

    }

    private void createAndShowFormDialogBox(final String title, final String domainObjectType,
                                            final LinkedFormMappingConfig mappingConfig,
                                            Map<String, Collection<String>> parentWidgetIdsForNewFormMap) {
        String width = GuiUtil.getModalWidth(domainObjectType, mappingConfig, null);
        String height = GuiUtil.getModalHeight(domainObjectType, mappingConfig, null);
        boolean resizable = GuiUtil.isFormResizable(domainObjectType, mappingConfig, null);
        final FormDialogBox createItemDialogBox = new FormDialogBox(title, width, height, resizable);



        FormPluginConfig config = createNewFormPluginConfig(domainObjectType, mappingConfig, getContainer(),
                parentWidgetIdsForNewFormMap);
        final FormPlugin createFormPlugin = createItemDialogBox.createFormPlugin(config, localEventBus, getContainer());
        createItemDialogBox.initButton(LocalizeUtil.get(SAVE_BUTTON_KEY, SAVE_BUTTON), new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final SaveAction action = createSaveAction(createFormPlugin, null, false);
                action.addActionSuccessListener(new ActionSuccessListener() {
                    @Override
                    public void onSuccess() {
                        createItemDialogBox.hide();

                    }
                });
                action.perform();

            }
        });
        String cancelButtonText = LocalizeUtil.get(CANCELLATION_BUTTON_KEY, CANCELLATION_BUTTON) ;
        createItemDialogBox.initButton(cancelButtonText, new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                createItemDialogBox.hide();
            }
        });
    }
    //TODO selection-filters applying
    protected void updateWidgetView(IdentifiableObject identifiableObject) {
        LinkCreatorWidgetState state = getInitialData();
        LinkEditingWidgetConfig config = (LinkEditingWidgetConfig) state.getWidgetConfig();
        String selectionPattern = config.getSelectionPatternConfig().getValue();
        String collectionName = config.getCollectionRefConfig() == null ? null : config.getCollectionRefConfig().getName();
        List<Id> ids = Arrays.asList(identifiableObject.getId());
        RepresentationRequest request = new RepresentationRequest(ids, selectionPattern, collectionName, config.getFormattingConfig());
        Command command = new Command("getRepresentationForOneItem", getName(), request);
        BusinessUniverseServiceAsync.Impl.executeCommand(command, new AsyncCallback<Dto>() {
            @Override
            public void onSuccess(Dto result) {
                RepresentationResponse response = (RepresentationResponse) result;
                Id id = response.getId();
                String representation = response.getRepresentation();
                handleNewCreatedItem(id, representation);

            }

            @Override
            public void onFailure(Throwable caught) {
                GWT.log("something was going wrong while obtaining hyperlink", caught);
            }
        });
    }

    protected abstract void handleNewCreatedItem(Id id, String representation);

    protected interface ClickAction {
        void perform();
    }

}
