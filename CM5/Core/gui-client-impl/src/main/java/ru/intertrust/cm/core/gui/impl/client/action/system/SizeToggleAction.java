package ru.intertrust.cm.core.gui.impl.client.action.system;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.Widget;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.action.ToggleAction;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.plugin.IsActive;

/**
 * @author Sergey.Okolot
 */
@ComponentName("size.toggle.action")
public class SizeToggleAction extends ToggleAction {

    @Override
    public void execute() {
        final IsActive active = (IsActive) getPlugin();
        final boolean fullScreen = active.getPluginState().isFullScreen();
        if (fullScreen) {
            active.getPluginState().setFullScreen(false);
            getPlugin().getView().setVisibleToolbar(getPlugin().displayActionToolBar());
            getImage().setUrl(getInitialContext().getActionConfig().getImageUrl().replace(IMAGE_SUFFIX, ON_SUFFIX));
        } else {
            active.getPluginState().setFullScreen(true);
            getImage().setUrl(getInitialContext().getActionConfig().getImageUrl().replace(IMAGE_SUFFIX, OFF_SUFFIX));
            getPlugin().getView().setVisibleToolbar(true);
        }
    }

    @Override
    public Component createNew() {
        return new SizeToggleAction();
    }

    private static class FormSizeAnimation extends Animation {

        private final Widget widget;
        private int left;
        private int top;

        private FormSizeAnimation(final Widget widget, final int left, final int top) {
            this.widget = widget;
            this.left = left;
            this.top = top;
        }

        @Override
        protected void onStart() {
            widget.getElement().getStyle().setProperty("left", left, Style.Unit.PX);
            widget.getElement().getStyle().setProperty("top", top, Style.Unit.PX);
            super.onStart();
        }

        @Override
        protected void onUpdate(double progress) {
            if (left > 0) {
                left -= 5;
            }
            if (top > 0) {
                top -= 5;
            }
            if (left < 0) {
                left = 0;
            }
            if (top < 0) {
                top = 0;
            }
            widget.getElement().getStyle().setProperty("left", left, Style.Unit.PX);
            widget.getElement().getStyle().setProperty("top", top, Style.Unit.PX);
            if (left == 0 && top == 0) {
                cancel();
            }
        }
    }
}
