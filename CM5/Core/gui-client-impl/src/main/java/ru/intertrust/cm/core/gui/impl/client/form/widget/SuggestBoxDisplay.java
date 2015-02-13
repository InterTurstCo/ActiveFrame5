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
        setScrollHeight(height);
        if (scrollable) {this.getSuggestionPopup().getElement().getStyle().setTop(500, Style.Unit.PX);
           lazyLoadPanel.setVerticalScrollPosition(lastScrollPosition);
        }

    }
    public void setLazyLoadPanelHeight(int height, int lastScrollPosition, int topPosition, boolean scrollable) {
        setScrollHeight(height);
        if (scrollable) {this.getSuggestionPopup().getElement().getStyle().setTop(topPosition, Style.Unit.PX);
            lazyLoadPanel.setVerticalScrollPosition(lastScrollPosition);
        }

    }

    public void setLazyLoadHandler(SuggestBoxWidget.ScrollLazyLoadHandler handler) {
        handler.setLazyLoadPanel(lazyLoadPanel);
        lazyLoadPanel.addScrollHandler(handler);
    }

    private void setScrollHeight(int height) {
        lazyLoadPanel.getElement().getFirstChildElement().getStyle().setHeight(height, Style.Unit.PX);
    }

    @Override
    protected PopupPanel createPopup() {
        PopupPanel p = new SuggestBoxPopup(true, false);
        p.setStyleName("gwt-SuggestBoxPopup");
        p.setPreviewingAllNativeEvents(true);

        return p;

    }


    public boolean isNotShown() {
        return !getSuggestionPopup().isShowing();
    }
}
