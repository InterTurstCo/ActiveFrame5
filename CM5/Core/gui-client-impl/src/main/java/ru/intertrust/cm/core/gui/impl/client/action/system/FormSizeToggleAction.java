package ru.intertrust.cm.core.gui.impl.client.action.system;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.action.ToggleAction;
import ru.intertrust.cm.core.gui.impl.client.plugins.objectsurfer.DomainObjectSurferPlugin;
import ru.intertrust.cm.core.gui.model.ComponentName;

/**
 * @author Sergey.Okolot
 */
@ComponentName("formsize.toggle.action")
public class FormSizeToggleAction extends ToggleAction {

    @Override
    public void execute() {
        final boolean fullScreenMode = getImage().getUrl().endsWith(OFF_SUFFIX);
        final Plugin dosPlugin = getPlugin();
        if (!(dosPlugin instanceof  DomainObjectSurferPlugin)) {
            return;
        }
        final Plugin formPlugin = ((DomainObjectSurferPlugin) dosPlugin).getFormPlugin();
        if (fullScreenMode) {
//            dosPlugin.getOwner().closeCurrentPlugin();
        } else {
            final RootLayoutPanel rootLayout = RootLayoutPanel.get();
            final SimplePanel wrapper = new SimplePanel();
            wrapper.setStyleName("root-section");
            wrapper.setStyleName("content", true);
            wrapper.getElement().getStyle().setPosition(Style.Position.ABSOLUTE);
            wrapper.getElement().getStyle().setPadding(10, Style.Unit.PX);
            final Widget formWidget = formPlugin.getOwner().asWidget();
            final Animation formAnimation =
                    new FormSizeAnimation(wrapper, formWidget.getAbsoluteLeft(), formWidget.getAbsoluteTop());
            formWidget.removeFromParent();
            formWidget.getElement().getStyle().setBorderColor("red");
            formWidget.getElement().getStyle().setBorderWidth(1, Style.Unit.PX);
            formWidget.getElement().getStyle().setBorderStyle(Style.BorderStyle.SOLID);
            wrapper.add(formWidget);
            rootLayout.clear();
            rootLayout.add(wrapper);
            formAnimation.run(5000);
            Window.getClientWidth();
            Window.getClientHeight();
        }
        final String imageUrl = getInitialContext().getActionConfig().getImageUrl();
        if (getImage() != null && imageUrl != null) {
            if (getImage().getUrl().endsWith(OFF_SUFFIX)) {
                getImage().setUrl(imageUrl.replace(IMAGE_SUFFIX, ON_SUFFIX));
            } else {
                getImage().setUrl(imageUrl.replace(IMAGE_SUFFIX, OFF_SUFFIX));
            }
        }
    }

    @Override
    public Component createNew() {
        return new FormSizeToggleAction();
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
