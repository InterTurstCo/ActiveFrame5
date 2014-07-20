package ru.intertrust.cm.core.gui.impl.client;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;
import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.config.gui.action.ActionDisplayType;
import ru.intertrust.cm.core.gui.impl.client.action.ToggleAction;
import ru.intertrust.cm.core.gui.impl.client.themes.GlobalThemesManager;
import ru.intertrust.cm.core.gui.model.action.ActionContext;
import ru.intertrust.cm.core.gui.model.action.ToggleActionContext;

/**
 * @author Sergey.Okolot
 */
public final class ComponentHelper {

    public static final String HEADER_ID = "header_pnl";
    public static final String LEFT_ID = "left_pnl";
    public static final String CENTER_ID = "center_pnl";
    public static final String DOMAIN_ID = "domain_pnl";

    /**
     * Don't create instance of helper class.
     */
    private ComponentHelper() {
    }

    public static SafeHtml createActionHtmlItem(final ActionContext context) {
        final ActionConfig actionConfig = context.getActionConfig();
        final SimplePanel wrapper = new SimplePanel();
        if (actionConfig.getImageUrl() != null) {
            final String imageUrl = GlobalThemesManager.getResourceFolder() + getImageAttr(actionConfig.getImageUrl(), context);
            final Image image = new Image(imageUrl);
            if (actionConfig.getImageClass() != null) {
                image.setStyleName(actionConfig.getImageClass());
            }
            DOM.appendChild(wrapper.getElement(), image.getElement());
        } else if (actionConfig.getImageClass() != null) {
            final String imageClass = getImageAttr(actionConfig.getImageClass(), context);
            final SimplePanel panel = new SimplePanel();
            panel.setStyleName(imageClass);
            DOM.appendChild(wrapper.getElement(), panel.getElement());
        }
        if (actionConfig.getText() != null) {
            final Anchor anchor = new Anchor(actionConfig.getText());
            anchor.setStyleName("action-bar-button");
            DOM.appendChild(wrapper.getElement(), anchor.getElement());
        }
        final SafeHtml safeHtml = new SafeHtml() {
            @Override
            public String asString() {
                return wrapper.getElement().getInnerHTML();
            }
        };
        return safeHtml;
    }

    private static String getImageAttr(final String attrValue, ActionContext context) {
        final ActionDisplayType type = ((ActionConfig) context.getActionConfig()).getDisplay();
        final String result;
        if (ActionDisplayType.toggleButton == type) {
            final ToggleActionContext toggleActionContext = (ToggleActionContext) context;
            result = attrValue.replace(ToggleAction.IMAGE_SUFFIX,
                    toggleActionContext.isPushed() ? ToggleAction.OFF_SUFFIX : ToggleAction.ON_SUFFIX);
        } else {
            result = attrValue;
        }
        return result;
    }
}
