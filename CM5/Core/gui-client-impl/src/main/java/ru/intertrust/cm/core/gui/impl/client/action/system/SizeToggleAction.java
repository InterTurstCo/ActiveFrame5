package ru.intertrust.cm.core.gui.impl.client.action.system;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Widget;
import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.ComponentHelper;
import ru.intertrust.cm.core.gui.impl.client.FormPlugin;
import ru.intertrust.cm.core.gui.impl.client.action.ToggleAction;
import ru.intertrust.cm.core.gui.impl.client.event.PluginPanelSizeChangedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.PluginPanelSizeChangedEventHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.plugin.IsActive;
import ru.intertrust.cm.core.gui.model.plugin.PluginState;

/**
 * @author Sergey.Okolot
 */
@ComponentName("size.toggle.action")
public class SizeToggleAction extends ToggleAction {

    @Override
    public void execute() {
        final IsActive active = (IsActive) getPlugin();
        final PluginState state = active.getPluginState();
        final boolean fullScreen = state.isFullScreen();
        final String imageUrl;
        if (fullScreen) {
            imageUrl = getInitialContext().getActionConfig().getImageUrl().replace(IMAGE_SUFFIX, ON_SUFFIX);
        } else {
            final Element header = DOM.getElementById(ComponentHelper.HEADER_ID);
            final Element left = DOM.getElementById(ComponentHelper.LEFT_ID);
            state.setHeaderHeight(header.getOffsetHeight());
            state.setLeftWidth(left.getOffsetWidth());
            imageUrl = getInitialContext().getActionConfig().getImageUrl().replace(IMAGE_SUFFIX, OFF_SUFFIX);
        }
        state.setFullScreen(!fullScreen);
        getImage().setUrl(imageUrl);
        active.setPluginState(state);
        updateSize(state);
    }

    private void updateSize(final PluginState state) {
        final int leftPosition, headerHeight, leftWidth;
        if (state.isFullScreen()) {
            leftPosition = headerHeight = leftWidth = 0;
        } else {
            leftPosition = 130;
            headerHeight = state.getHeaderHeight();
            leftWidth = state.getLeftWidth();
        }
        final Element header = DOM.getElementById(ComponentHelper.HEADER_ID);
        final Element left = DOM.getElementById(ComponentHelper.LEFT_ID);
        header.getStyle().setHeight(headerHeight, Style.Unit.PX);
        left.getStyle().setWidth(leftWidth, Style.Unit.PX);
        getPlugin().getView().asWidget().getParent().getParent()
                .getElement().getStyle().setLeft(leftPosition, Style.Unit.PX);
        final int clientWidth = Window.getClientWidth() - leftWidth;
        final int clientHeight = Window.getClientHeight() - headerHeight;
        getPlugin().getView().asWidget().getElement().getStyle().setWidth(clientWidth, Style.Unit.PX);
        getPlugin().getView().asWidget().getElement().getStyle().setHeight(clientHeight, Style.Unit.PX);
        getPlugin().getOwner().setVisibleWidth(clientWidth);
        getPlugin().getOwner().setVisibleHeight(clientHeight);
        if (getPlugin() instanceof PluginPanelSizeChangedEventHandler) {
            if (getPlugin() instanceof FormPlugin) {
                ((FormPlugin) getPlugin()).setTemporaryWidth(clientWidth);
                ((FormPlugin) getPlugin()).setTemporaryHeight(clientHeight);
            }
            ((PluginPanelSizeChangedEventHandler) getPlugin()).updateSizes();
        }
    }

    @Override
    public Component createNew() {
        return new SizeToggleAction();
    }

//    private static class FormSizeAnimation extends Animation {
//
//        private final Widget widget;
//        private final IsActive active;
//
//        private FormSizeAnimation(final Widget widget, final IsActive active) {
//            this.widget = widget;
//            this.active = active;
//        }
//
//        @Override
//        protected void onStart() {
//            widget.getElement().getStyle().setProperty("left", left, Style.Unit.PX);
//            widget.getElement().getStyle().setProperty("top", top, Style.Unit.PX);
//            super.onStart();
//        }
//
//        @Override
//        protected void onUpdate(double progress) {
//            if (left > 0) {
//                left -= 5;
//            }
//            if (top > 0) {
//                top -= 5;
//            }
//            if (left < 0) {
//                left = 0;
//            }
//            if (top < 0) {
//                top = 0;
//            }
//            widget.getElement().getStyle().setProperty("left", left, Style.Unit.PX);
//            widget.getElement().getStyle().setProperty("top", top, Style.Unit.PX);
//            if (left == 0 && top == 0) {
//                cancel();
//            }
//        }
//    }
}
