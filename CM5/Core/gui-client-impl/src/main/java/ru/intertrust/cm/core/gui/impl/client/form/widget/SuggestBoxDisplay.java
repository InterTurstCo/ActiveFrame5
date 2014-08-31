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

    public void setLazyLoadPanelHeight(int height) {
        lazyLoadPanel.getElement().getFirstChildElement().getStyle().setHeight(height, Style.Unit.PX);
    }

    public void setLazyLoadHandler(SuggestBoxWidget.ScrollLazyLoadHandler handler) {
        handler.setLazyLoadPanel(lazyLoadPanel);
        lazyLoadPanel.addScrollHandler(handler);
    }

    public void setScrollPosition(int position) {
        lazyLoadPanel.setVerticalScrollPosition(position);

    }

    @Override
    protected PopupPanel createPopup() {
        PopupPanel p = new SuggestBoxPopup(true, false);
        p.setStyleName("gwt-SuggestBoxPopup");
        p.setPreviewingAllNativeEvents(true);

        return p;

    }
}
