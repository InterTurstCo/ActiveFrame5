package ru.intertrust.cm.core.gui.impl.client.form.widget;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.impl.client.FormPlugin;
import ru.intertrust.cm.core.gui.impl.client.action.SaveAction;
import ru.intertrust.cm.core.gui.impl.client.event.ActionSuccessListener;
import ru.intertrust.cm.core.gui.impl.client.event.CollectionRowSelectedEvent;
import ru.intertrust.cm.core.gui.impl.client.form.widget.hyperlink.FormDialogBox;
import ru.intertrust.cm.core.gui.impl.client.form.widget.hyperlink.LinkedDomainObjectHyperlinkItem;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.SaveActionContext;
import ru.intertrust.cm.core.gui.model.form.widget.LinkedDomainObjectHyperlinkState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginConfig;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 14.01.14
 *         Time: 10:25
 */
@ComponentName("linked-domain-object-hyperlink")
public class LinkedDomainObjectHyperlinkWidget extends BaseWidget {
    private LinkedDomainObjectHyperlinkItem hyperlinkItem;

    @Override
    public Component createNew() {
        return new LinkedDomainObjectHyperlinkWidget();
    }

    public void setCurrentState(WidgetState currentState) {
        final LinkedDomainObjectHyperlinkState state = (LinkedDomainObjectHyperlinkState) currentState;
        final FormPluginConfig config = state.getConfig();
        final String domainObjectType = state.getDomainObjectType();
        final Id id = state.getId();
        final Id parentId = state.getParentId();
        hyperlinkItem.setText(state.getStringRepresentation());
        hyperlinkItem.addItemClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final FormDialogBox noneEditableFormDialogBox = new FormDialogBox(domainObjectType);
                noneEditableFormDialogBox.initFormPlugin(config);
                noneEditableFormDialogBox.initButton("Открыть в полном окне", new ClickHandler() {

                    @Override
                    public void onClick(ClickEvent event) {
                        //To change body of implemented methods use File | Settings | File Templates.
                    }
                });
                noneEditableFormDialogBox.initButton("Изменить", new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        noneEditableFormDialogBox.hide();
                        config.getPluginState().setEditable(true);
                        final FormDialogBox editableFormDialogBox =
                                new FormDialogBox("Редактирование " + domainObjectType);
                        final FormPlugin formPluginEditable = editableFormDialogBox.initFormPlugin(config);
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
                                        eventBus.fireEvent(new CollectionRowSelectedEvent(parentId));
                                    }
                                });
                                action.execute();

                            }
                        });
                        editableFormDialogBox.initButton("Отмена", new ClickHandler() {
                            @Override
                            public void onClick(ClickEvent event) {

                                editableFormDialogBox.hide();
                                config.getPluginState().setEditable(false);
                            }
                        });

                    }

                });

            }
        });
    }

    @Override
    public WidgetState getCurrentState() {
        LinkedDomainObjectHyperlinkState state = new LinkedDomainObjectHyperlinkState();
        return state;
    }

    @Override
    protected Widget asEditableWidget() {
        hyperlinkItem = new LinkedDomainObjectHyperlinkItem();
        return hyperlinkItem.asWidget();
    }

    @Override
    protected Widget asNonEditableWidget() {
        return asEditableWidget();
    }


}

