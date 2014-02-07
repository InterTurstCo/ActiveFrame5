package ru.intertrust.cm.core.gui.impl.client.form.widget;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.impl.client.FormPlugin;
import ru.intertrust.cm.core.gui.impl.client.action.SaveAction;
import ru.intertrust.cm.core.gui.impl.client.event.ActionSuccessListener;
import ru.intertrust.cm.core.gui.impl.client.event.CentralPluginChildOpeningRequestedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.HierarchyBrowserHyperlinkStateUpdatedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.hierarchybrowser.HierarchyBrowserCloseDialogEvent;
import ru.intertrust.cm.core.gui.impl.client.form.widget.hyperlink.FormDialogBox;
import ru.intertrust.cm.core.gui.model.action.SaveActionContext;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginConfig;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 03.01.14
 *         Time: 13:15
 */
public class HierarchyBrowserHyperlinkClickHandler implements ClickHandler{
private String title;
private Id id;
private EventBus eventBus;
private String collectionName;
        public HierarchyBrowserHyperlinkClickHandler(String title, Id id, String collectionName, EventBus eventBus) {
            this.title = title;
            this.id = id;
            this.collectionName = collectionName;
            this.eventBus = eventBus;

        }

        @Override
        public void onClick(ClickEvent event) {

            final FormDialogBox noneEditableFormDialogBox = new FormDialogBox(title);
            final FormPluginConfig config = new FormPluginConfig();
            config.setDomainObjectId(id);
            config.getPluginState().setToggleEdit(true);
            config.getPluginState().setInCentralPanel(true);
            final FormPlugin plugin = noneEditableFormDialogBox.createFormPlugin(config);
            noneEditableFormDialogBox.initButton("Открыть в полном окне", new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {
                    plugin.setLocalEventBus(eventBus);
                    plugin.setDisplayActionToolBar(true);
                    Application.getInstance().getEventBus().fireEvent(new CentralPluginChildOpeningRequestedEvent(plugin));
                    noneEditableFormDialogBox.hide();
                    eventBus.fireEvent(new HierarchyBrowserCloseDialogEvent());
                }
            });
            noneEditableFormDialogBox.initButton("Изменить", new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    noneEditableFormDialogBox.hide();

                    final FormPluginConfig config = new FormPluginConfig();
                    config.setDomainObjectId(id);
                    config.getPluginState().setEditable(true);
                    final FormDialogBox editableFormDialogBox =
                            new FormDialogBox("Редактирование " + title);
                    final FormPlugin formPluginEditable = editableFormDialogBox.createFormPlugin(config);
                    editableFormDialogBox.initButton("Изменить", new ClickHandler() {
                        @Override
                        public void onClick(ClickEvent event) {
                            final SaveAction action = ComponentRegistry.instance.get("save.action");
                            SaveActionContext saveActionContext = new SaveActionContext();
                            saveActionContext.setRootObjectId(id);
                            action.setInitialContext(saveActionContext);
                            action.setPlugin(formPluginEditable);
                            action.addActionSuccessListener(new ActionSuccessListener() {
                                @Override
                                public void onSuccess() {
                                    editableFormDialogBox.hide();
                                    eventBus.fireEvent(new HierarchyBrowserHyperlinkStateUpdatedEvent(id, collectionName));
                                }
                            });
                            action.execute();

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
}
