package ru.intertrust.cm.core.gui.impl.client.form.widget.hyperlink;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.impl.client.FormPlugin;
import ru.intertrust.cm.core.gui.impl.client.action.SaveAction;
import ru.intertrust.cm.core.gui.impl.client.event.ActionSuccessListener;
import ru.intertrust.cm.core.gui.impl.client.event.CentralPluginChildOpeningRequestedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.HyperlinkStateChangedEvent;
import ru.intertrust.cm.core.gui.model.action.SaveActionContext;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginConfig;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 03.01.14
 *         Time: 13:15
 */
public class HyperlinkClickHandler implements ClickHandler {
    private static final String TITLE = "Елемент";
    private Id id;
    private EventBus eventBus;
    private HyperlinkDisplay hyperlinkDisplay;
    private boolean tooltipContent;

    public HyperlinkClickHandler(Id id, HyperlinkDisplay hyperlinkDisplay, EventBus eventBus, boolean tooltipContent) {
        this.id = id;
        this.hyperlinkDisplay = hyperlinkDisplay;
        this.eventBus = eventBus;
        this.tooltipContent = tooltipContent;
    }

    @Override
    public void onClick(ClickEvent event) {
        onClick();

    }

    public void onClick() {
        final FormDialogBox noneEditableFormDialogBox = new FormDialogBox();
        final FormPluginConfig config = new FormPluginConfig();
        config.setDomainObjectId(id);
        config.getPluginState().setEditable(false);
        final FormPlugin plugin = noneEditableFormDialogBox.createFormPlugin(config, eventBus);
        noneEditableFormDialogBox.initButton("Открыть в полном окне", new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                plugin.setLocalEventBus(eventBus);
                plugin.setDisplayActionToolBar(true);
                Application.getInstance().getEventBus()
                        .fireEvent(new CentralPluginChildOpeningRequestedEvent(plugin));
                noneEditableFormDialogBox.hide();
            }
        });
        noneEditableFormDialogBox.initButton("Изменить", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                noneEditableFormDialogBox.hide();

                final FormPluginConfig config = new FormPluginConfig();
                config.setDomainObjectId(id);
                config.getPluginState().setEditable(true);
                final FormDialogBox editableFormDialogBox =  new FormDialogBox();

                final FormPlugin formPluginEditable = editableFormDialogBox.createFormPlugin(config, eventBus);
                editableFormDialogBox.initButton("Изменить", new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        final SaveAction action = getSaveAction(formPluginEditable, id);
                        action.addActionSuccessListener(new ActionSuccessListener() {
                            @Override
                            public void onSuccess() {
                                editableFormDialogBox.hide();
                                eventBus.fireEvent(new HyperlinkStateChangedEvent(id, hyperlinkDisplay, tooltipContent));
                            }
                        });
                        action.perform();

                    }
                });
                editableFormDialogBox.initButton("Отмена", new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        editableFormDialogBox.hide();
                    }
                });

            }

        });
        noneEditableFormDialogBox.initButton("Отмена", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                noneEditableFormDialogBox.hide();
            }
        });
    }

    private SaveAction getSaveAction(final FormPlugin formPlugin, final Id rootObjectId) {
        SaveActionContext saveActionContext = new SaveActionContext();
        saveActionContext.setRootObjectId(rootObjectId);
        final ActionConfig actionConfig = new ActionConfig("save.action");
        saveActionContext.setActionConfig(actionConfig);

        final SaveAction action = ComponentRegistry.instance.get(actionConfig.getComponentName());
        action.setInitialContext(saveActionContext);
        action.setPlugin(formPlugin);
        return action;
    }
}