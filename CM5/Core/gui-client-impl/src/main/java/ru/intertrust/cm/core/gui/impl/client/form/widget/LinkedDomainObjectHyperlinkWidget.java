package ru.intertrust.cm.core.gui.impl.client.form.widget;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.form.widget.hyperlink.FormDialogBox;
import ru.intertrust.cm.core.gui.impl.client.form.widget.hyperlink.LinkedDomainObjectHyperlinkItem;
import ru.intertrust.cm.core.gui.model.ComponentName;
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
        LinkedDomainObjectHyperlinkState state = (LinkedDomainObjectHyperlinkState) currentState;
        final FormPluginConfig config = state.getConfig();
        final String domainObjectType = state.getDomainObjectType();
        hyperlinkItem.setText(state.getStringRepresentation());
        hyperlinkItem.addItemClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final FormDialogBox noneEditableFormDialogBox = new FormDialogBox(domainObjectType);
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

                        editableFormDialogBox.initButton("Изменить", new ClickHandler() {
                            @Override
                            public void onClick(ClickEvent event) {
                                    /*ActionContext ctx;
                            final Action action = ComponentRegistry.instance.get(ctx.getActionConfig().getComponent());
                            action.setInitialContext(ctx);
                            action.execute();*/
                            }
                        });
                        editableFormDialogBox.initButton("Отмена", new ClickHandler() {
                            @Override
                            public void onClick(ClickEvent event) {

                                editableFormDialogBox.hide();
                                config.getPluginState().setEditable(false);
                            }
                        });
                        editableFormDialogBox.initFormPlugin(config);
                    }

                });
                noneEditableFormDialogBox.initFormPlugin(config);

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

