package ru.intertrust.cm.core.gui.impl.client.plugins.stickerpanel;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import ru.intertrust.cm.core.gui.impl.client.PluginView;


public class StickerPluginView extends PluginView {
    public StickerPluginView(StickerPlugin stickerPlugin) {
        super(stickerPlugin);
    }
    @Override
    protected IsWidget getViewWidget() {


        AnimatedFlowPanel widget = new AnimatedFlowPanel(100);
        FlowPanel panel = new FlowPanel();
        panel.getElement().getStyle().setBackgroundColor("red");

        panel.getElement().getStyle().setProperty("marginLeft", "5px");
        panel.getElement().getStyle().setProperty("marginRight", "5px");
        return panel;
    }
    private class AnimatedFlowPanel extends Animation implements IsWidget {
        private int moveStart;
        private int moveWidth;
        private FlowPanel panel;
        private  Element e;

        public AnimatedFlowPanel(int moveWidth){

            this.panel = new FlowPanel();

            this.moveWidth = moveWidth;
            init();

        }

        private void init() {
            e = panel.getElement();

            panel.getElement().getStyle().setBackgroundColor("red");
            panel.setStyleName("sticker");
            panel.getElement().getStyle().setProperty("marginLeft", "5px");
            panel.getElement().getStyle().setProperty("marginRight", "5px");

        }

        public void moveTo(int position, int milliseconds) {

            moveStart = e.getOffsetLeft();
            moveWidth = position;

            run(milliseconds);
        }

        @Override
        protected void onUpdate(double progress) {
            double position = moveStart + (progress * (moveStart - moveWidth));
            e.getStyle().setLeft(position - moveWidth, Style.Unit.PX);
        }
        @Override
        protected void onComplete() {
            e.getStyle().setLeft(Window.getClientWidth() - moveWidth, Style.Unit.PX);
        }

        @Override
        public Widget asWidget() {
            return panel;
        }
    }

}
