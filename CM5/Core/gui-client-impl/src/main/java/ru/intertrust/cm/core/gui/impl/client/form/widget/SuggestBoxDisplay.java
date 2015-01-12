package ru.intertrust.cm.core.gui.impl.client.form.widget;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.Widget;
import ru.intertrust.cm.core.gui.impl.client.form.widget.suggestbox.SuggestBoxPopup;


/**
 * @author Yaroslav Bondacrhuk
 *         Date: 24.08.2014
 *         Time: 15:29
 */
public class SuggestBoxDisplay extends SuggestBox.DefaultSuggestionDisplay {
    private static final int SCROLL_HEIGHT_MARGIN = 40;
    private ScrollPanel lazyLoadPanel;

    public SuggestBoxDisplay() {
        setSuggestionListHiddenWhenEmpty(true);

    }

    public PopupPanel getSuggestionPopup() {
        return this.getPopupPanel();
    }

    @Override
    protected Widget decorateSuggestionList(Widget suggestionList) {
        lazyLoadPanel = new ScrollPanel();
        lazyLoadPanel.add(suggestionList);
        return super.decorateSuggestionList(lazyLoadPanel);
    }

    @Deprecated //use setLazyLoadPanelHeight(int height, int lastScrollPosition, boolean scrollable) instead
    public void setLazyLoadPanelHeight(int height) {
        lazyLoadPanel.getElement().getFirstChildElement().getStyle().setHeight(height, Style.Unit.PX);
    }

    public void setLazyLoadPanelHeight(int height, int lastScrollPosition, boolean scrollable) {
        if (scrollable) {
            makeScrollVisible(height, lastScrollPosition);
        } else {
            setScrollHeight(height);
        }

    }

    public void setLazyLoadHandler(SuggestBoxWidget.ScrollLazyLoadHandler handler) {
        handler.setLazyLoadPanel(lazyLoadPanel);
        lazyLoadPanel.addScrollHandler(handler);
    }

    private void setScrollHeight(int height) {
        lazyLoadPanel.getElement().getFirstChildElement().getStyle().setHeight(height, Style.Unit.PX);
    }

    public void clearScrollHeight() {
        lazyLoadPanel.getElement().getFirstChildElement().getStyle().clearHeight();
    }

    @Override
    protected PopupPanel createPopup() {
        PopupPanel p = new SuggestBoxPopup(true, false);
        p.setStyleName("gwt-SuggestBoxPopup");
        p.setPreviewingAllNativeEvents(true);

        return p;

    }

    private int getScrollHeight() {
        return lazyLoadPanel.getElement().getFirstChildElement().getOffsetHeight();
    }

    public void makeScrollVisible(final int calculatedHeight, final int lastScrollPos) {
        int scrollHeight = getScrollHeight();
        if (scrollHeight != 0 && scrollHeight <= calculatedHeight){
            setScrollHeight(scrollHeight -  SCROLL_HEIGHT_MARGIN);
        } else {
            setScrollHeight(calculatedHeight);
        }
        lazyLoadPanel.setVerticalScrollPosition(lastScrollPos);

    }

    public boolean isNotShown() {
        return !getSuggestionPopup().isShowing();
    }
}
