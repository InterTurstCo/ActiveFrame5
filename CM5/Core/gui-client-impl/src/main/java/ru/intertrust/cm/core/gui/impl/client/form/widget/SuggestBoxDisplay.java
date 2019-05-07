package ru.intertrust.cm.core.gui.impl.client.form.widget;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.MenuItem;
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


    @Override
    protected void moveSelectionDown() {
        super.moveSelectionDown();
        scrollSelectedItemIntoView();
    }

    @Override
    protected void moveSelectionUp() {
        super.moveSelectionUp();
        scrollSelectedItemIntoView();
    }

    private void scrollSelectedItemIntoView() {
        getSelectedMenuItem().getElement().scrollIntoView();
    }

    private native MenuItem getSelectedMenuItem() /*-{
        var menu = this.@com.google.gwt.user.client.ui.SuggestBox.DefaultSuggestionDisplay::suggestionMenu;
        return menu.@com.google.gwt.user.client.ui.MenuBar::selectedItem;
    }-*/;


    @Deprecated //use setLazyLoadPanelHeight(int height, int lastScrollPosition, boolean scrollable) instead
    public void setLazyLoadPanelHeight(int height) {
        lazyLoadPanel.getElement().getFirstChildElement().getStyle().setHeight(height, Style.Unit.PX);
    }

    public void setLazyLoadPanelHeight(int height, int lastScrollPosition, int topPosition, boolean scrollable) {
        setScrollHeight(height);
        this.getSuggestionPopup().getElement().getStyle().setTop(topPosition, Style.Unit.PX);
        if (scrollable) {
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
    public  boolean isHorizontalScrollNotVisible() {
        int scrollMinHorizontal = lazyLoadPanel.getMinimumHorizontalScrollPosition();
        int scrollMaxHorizontal = lazyLoadPanel.getMaximumHorizontalScrollPosition();
        return scrollMinHorizontal == scrollMaxHorizontal;
    }


    public boolean isNotShown() {
        return !getSuggestionPopup().isShowing();
    }

    public ScrollPanel getLazyLoadPanel() {
        return lazyLoadPanel;
    }
}
