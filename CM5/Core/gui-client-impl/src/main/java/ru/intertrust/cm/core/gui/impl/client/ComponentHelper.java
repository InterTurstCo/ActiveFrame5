package ru.intertrust.cm.core.gui.impl.client;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.IsWidget;
import ru.intertrust.cm.core.config.gui.ActionConfig;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.impl.client.action.Action;
import ru.intertrust.cm.core.gui.impl.client.action.ToggleAction;
import ru.intertrust.cm.core.gui.model.action.ActionContext;
import ru.intertrust.cm.core.gui.model.action.ToggleActionContext;

/**
 * @author Sergey.Okolot
 */
public final class ComponentHelper {

    /**
     * Don't create instance of helper class.
     */
    private ComponentHelper() {
    }

    public static IsWidget createToolbarBtn(final ActionContext ctx, final Plugin plugin, boolean showLabel) {
        final FocusPanel container = new FocusPanel();
        container.getElement().getStyle().setFloat(Style.Float.LEFT);
        final ActionConfig config = ctx.getActionConfig();
        if (config.getShortDesc() != null) {
            container.setTitle(config.getShortDesc());
        }
        Image image = null;
        Anchor anchor = null;
        if (config.getImageUrl() != null) {
            final String imageUrl;
            if (config.isToggle()) {
                final ToggleActionContext toggleCtx = (ToggleActionContext) ctx;
                imageUrl = config.getImageUrl().replace(ToggleAction.IMAGE_SUFFIX,
                        toggleCtx.isPushed() ? ToggleAction.OFF_SUFFIX : ToggleAction.ON_SUFFIX);
            } else {
                imageUrl = config.getImageUrl();
            }
            image = new Image(imageUrl);
            DOM.appendChild(container.getElement(), image.getElement());
        }
        if (showLabel && config.getText() != null) {
            anchor = new Anchor(config.getText());
            anchor.setStyleName("action-bar-button");
            if (config.getShortDesc() != null) {
                anchor.setTitle(config.getShortDesc());
            }
            DOM.appendChild(container.getElement(), anchor.getElement());
        }
        container.addClickHandler(createActionClickHandler(ctx, plugin, image, anchor));
        return container;
    }

    private static ClickHandler createActionClickHandler(final ActionContext context, final Plugin plugin,
                                                         final Image image, final Anchor anchor) {
        final ClickHandler handler = new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                String component = context.getActionConfig().getComponent();
                if (component == null) {
                    component = "generic.workflow.action";
                }
                final Action action = ComponentRegistry.instance.get(component);
                action.setInitialContext(context);
                action.setPlugin(plugin);
                if (context.getActionConfig().isToggle()) {
                    ((ToggleAction) action).setImage(image);
                    ((ToggleAction) action).setAnchor(anchor);
                }
                action.execute();
            }
        };
        return handler;
    }
}
