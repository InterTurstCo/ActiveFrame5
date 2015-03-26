package ru.intertrust.cm.core.gui.impl.client;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;
import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.config.gui.action.ActionDisplayType;
import ru.intertrust.cm.core.config.gui.action.ActionGroupConfig;
import ru.intertrust.cm.core.gui.impl.client.action.ToggleAction;
import ru.intertrust.cm.core.gui.impl.client.themes.CommonCssResource;
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
    public static final String RIGHT_ID = "right_pnl";
    public static final String FOOTER_ID = "footer_pnl";
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
            final String imageClass = getImageClass(actionConfig.getImageClass(), context);
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

    public static SafeHtml createActionGroupHtmlItem(final ActionContext context) {
        final ActionGroupConfig actionConfig = context.getActionConfig();
        final SimplePanel wrapper = new SimplePanel();
        if (actionConfig.getImageUrl() != null) {
            final String imageUrl = GlobalThemesManager.getResourceFolder() + getImageAttr(actionConfig.getImageUrl(), context);
            final Image image = new Image(imageUrl);
            if (actionConfig.getImageClass() != null) {
                image.setStyleName(actionConfig.getImageClass());
            }
            DOM.appendChild(wrapper.getElement(), image.getElement());
        } else if (actionConfig.getImageClass() != null) {
            final String imageClass = getImageClass(actionConfig.getImageClass(), context);
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

    private static String getImageClass(final String imageClass, ActionContext context) {
        final ActionDisplayType type = ((ActionConfig) context.getActionConfig()).getDisplay();
        String result = ToggleAction.NO_IMAGE_ACTION_STYLE_NAME;
        if (ActionDisplayType.toggleButton == type) {
            CommonCssResource currentCommonCss = GlobalThemesManager.getCurrentTheme().commonCss();
            final ToggleActionContext toggleActionContext = (ToggleActionContext) context;
            switch (imageClass) {
                case ToggleAction.FAVORITE_PANEL_ACTION_STYLE_NAME:
                    result = toggleActionContext.isPushed() ? currentCommonCss.favoritePanelOff()
                            : currentCommonCss.favoritePanelOn();
                    break;
                case ToggleAction.FORM_FULL_SIZE_ACTION_STYLE_NAME:
                    result = toggleActionContext.isPushed() ? currentCommonCss.formFullSizeOff()
                            : currentCommonCss.formFullSizeOn();
                    break;
                case ToggleAction.CONFIGURATION_UPLOAD_ACTION_STYLE_NAME:
                    result = currentCommonCss.configurationUploader();
                    break;
            }
        } else {
            result = imageClass;
        }
        return result;
    }
}
