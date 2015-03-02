package ru.intertrust.cm.core.gui.impl.client.form.widget.hierarchybrowser;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.config.gui.form.widget.NodeCollectionDefConfig;
import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.api.client.LocalizeUtil;
import ru.intertrust.cm.core.gui.impl.client.FormPlugin;
import ru.intertrust.cm.core.gui.impl.client.action.SaveAction;
import ru.intertrust.cm.core.gui.impl.client.event.ActionSuccessListener;
import ru.intertrust.cm.core.gui.impl.client.event.CentralPluginChildOpeningRequestedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.hierarchybrowser.HierarchyBrowserCloseDialogEvent;
import ru.intertrust.cm.core.gui.impl.client.event.hierarchybrowser.HierarchyBrowserHyperlinkStateUpdatedEvent;
import ru.intertrust.cm.core.gui.impl.client.form.widget.hyperlink.FormDialogBox;
import ru.intertrust.cm.core.gui.impl.client.util.GuiUtil;
import ru.intertrust.cm.core.gui.model.action.SaveActionContext;
import ru.intertrust.cm.core.gui.model.form.widget.HierarchyBrowserItem;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginConfig;

import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.CANCELLATION_BUTTON;
import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.CANCEL_BUTTON;
import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.CHANGE_BUTTON;
import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.OPEN_IN_FULL_WINDOW;
/**
 * @author Yaroslav Bondarchuk
 *         Date: 03.01.14
 *         Time: 13:15
 */
@Deprecated
/*
* HierarchyBrowserWidget handles clicks instead
 */
public class HierarchyBrowserHyperlinkClickHandler implements ClickHandler {

    private EventBus eventBus;
    private HierarchyBrowserItem item;
    private HierarchyBrowserDisplay hyperlinkDisplay;
    private boolean tooltipContent;
    private NodeCollectionDefConfig nodeConfig;

    public HierarchyBrowserHyperlinkClickHandler(HierarchyBrowserItem item, EventBus eventBus, NodeCollectionDefConfig nodeConfig,
                                                 HierarchyBrowserDisplay hyperlinkDisplay, boolean tooltipContent) {
        this.item = item;
        this.eventBus = eventBus;
        this.hyperlinkDisplay = hyperlinkDisplay;
        this.tooltipContent = tooltipContent;
        this.nodeConfig = nodeConfig;

    }

    @Override
    public void onClick(ClickEvent event) {

        final FormDialogBox noneEditableFormDialogBox = new FormDialogBox(item.getPopupTitle());
        final FormPluginConfig config = GuiUtil.createFormPluginConfig(item.getId(), nodeConfig, item.getDomainObjectType(), false);
        final FormPlugin plugin = noneEditableFormDialogBox.createFormPlugin(config, eventBus);
        noneEditableFormDialogBox.initButton(LocalizeUtil.get(OPEN_IN_FULL_WINDOW), new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                plugin.setLocalEventBus(eventBus);
                plugin.setDisplayActionToolBar(true);
                Application.getInstance().getEventBus().fireEvent(new CentralPluginChildOpeningRequestedEvent(plugin));
                noneEditableFormDialogBox.hide();
                eventBus.fireEvent(new HierarchyBrowserCloseDialogEvent());
            }
        });
        noneEditableFormDialogBox.initButton(LocalizeUtil.get(CHANGE_BUTTON), new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                noneEditableFormDialogBox.hide();


                config.getPluginState().setEditable(true);
                final FormDialogBox editableFormDialogBox =
                        new FormDialogBox(item.getPopupTitle());
                final FormPlugin formPluginEditable = editableFormDialogBox.createFormPlugin(config, eventBus);
                editableFormDialogBox.initButton(LocalizeUtil.get(CHANGE_BUTTON), new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        final SaveAction action = ComponentRegistry.instance.get("save.action");
                        SaveActionContext saveActionContext = new SaveActionContext();
                        saveActionContext.setRootObjectId(item.getId());
                        action.setInitialContext(saveActionContext);
                        action.setPlugin(formPluginEditable);
                        action.addActionSuccessListener(new ActionSuccessListener() {
                            @Override
                            public void onSuccess() {
                                editableFormDialogBox.hide();
                                eventBus.fireEvent(new HierarchyBrowserHyperlinkStateUpdatedEvent(item.getId(),
                                        item.getNodeCollectionName(), hyperlinkDisplay, tooltipContent));

                            }
                        });
                        action.perform();

                    }
                });
                editableFormDialogBox.initButton(LocalizeUtil.get(CANCEL_BUTTON), new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        editableFormDialogBox.hide();
                    }
                });

            }

        });
        noneEditableFormDialogBox.initButton(LocalizeUtil.get(CANCELLATION_BUTTON), new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                noneEditableFormDialogBox.hide();
            }
        });

    }
}
