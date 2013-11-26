package ru.intertrust.cm.core.gui.impl.client.form;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.*;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.layout.client.Layout;
import com.google.gwt.resources.client.CommonResources;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.*;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created with IntelliJ IDEA.
 * User: Timofiy Bilyi
 * Date: 21.11.13
 * Time: 23:27
 * To change this template use File | Settings | File Templates.
 */

    public class HackTabLayoutPanel extends ResizeComposite implements HasWidgets,
            ProvidesResize, IndexedPanel.ForIsWidget, AnimatedLayout,
            HasBeforeSelectionHandlers<Integer>, HasSelectionHandlers<Integer> {

        private class Tab extends SimplePanel {
            private Element inner;
            private boolean replacingWidget;

            public Tab(Widget child) {
                super(Document.get().createDivElement());
                getElement().appendChild(inner = Document.get().createDivElement());

                setWidget(child);
                setStyleName(TAB_STYLE);
                inner.setClassName(TAB_INNER_STYLE);

                getElement().addClassName(CommonResources.getInlineBlockStyle());
            }

            public HandlerRegistration addClickHandler(ClickHandler handler) {
                return addDomHandler(handler, ClickEvent.getType());
            }

            @Override
            public boolean remove(Widget w) {
                int index = tabs.indexOf(this);
                if (replacingWidget || index < 0) {
                    return super.remove(w);
                } else {
                    return HackTabLayoutPanel.this.remove(index);
                }
            }

            public void setSelected(boolean selected) {
                if (selected) {
                    addStyleDependentName("selected");
                } else {
                    removeStyleDependentName("selected");
                }
            }

            @Override
            public void setWidget(Widget w) {
                replacingWidget = true;
                super.setWidget(w);
                replacingWidget = false;
            }

            @Override
            protected com.google.gwt.user.client.Element getContainerElement() {
                return inner.cast();
            }
        }

        private class TabbedDeckLayoutPanel extends DeckLayoutPanel {

            @Override
            public void add(Widget w) {
                throw new UnsupportedOperationException(
                        "Use TabLayoutPanel.add() to alter the DeckLayoutPanel");
            }

            @Override
            public void clear() {
                throw new UnsupportedOperationException(
                        "Use TabLayoutPanel.clear() to alter the DeckLayoutPanel");
            }

            @Override
            public void insert(Widget w, int beforeIndex) {
                throw new UnsupportedOperationException(
                        "Use TabLayoutPanel.insert() to alter the DeckLayoutPanel");
            }

            @Override
            public boolean remove(Widget w) {

                return HackTabLayoutPanel.this.remove(w);
            }

            protected void insertProtected(Widget w, int beforeIndex) {
                super.insert(w, beforeIndex);
            }

            protected void removeProtected(Widget w) {
                super.remove(w);
            }
        }

        private static final String CONTENT_CONTAINER_STYLE = "gwt-TabLayoutPanelContentContainer";


        private static final String CONTENT_STYLE = "gwt-TabLayoutPanelContent gwt-TabLayoutPanel-No-Padding";
        private static final String TAB_STYLE = "gwt-TabLayoutPanelTab";

        private static final String TAB_INNER_STYLE = "gwt-TabLayoutPanelTabInner";

        private static final int BIG_ENOUGH_TO_NOT_WRAP = 16384;

        private final TabbedDeckLayoutPanel deckPanel = new TabbedDeckLayoutPanel();
        private final FlowPanel tabBar = new FlowPanel();
        private final ArrayList<Tab> tabs = new ArrayList<Tab>();
        private int selectedIndex = -1;

        public HackTabLayoutPanel(double barHeight, Style.Unit barUnit) {
            LayoutPanel panel = new LayoutPanel();
            initWidget(panel);

            // Add the tab bar to the panel.
            panel.add(tabBar);
            panel.setWidgetLeftRight(tabBar, 0, Style.Unit.PX, 0, Style.Unit.PX);
            panel.setWidgetTopHeight(tabBar, 0, Style.Unit.PX, barHeight, barUnit);
            panel.setWidgetVerticalPosition(tabBar, Layout.Alignment.END);

            // Add the deck panel to the panel.
            deckPanel.addStyleName(CONTENT_CONTAINER_STYLE);
            panel.add(deckPanel);
            panel.setWidgetLeftRight(deckPanel, 0, Style.Unit.PX, 0, Style.Unit.PX);
            panel.setWidgetTopBottom(deckPanel, barHeight, barUnit, 0, Style.Unit.PX);

            // Make the tab bar extremely wide so that tabs themselves never wrap.
            // (Its layout container is overflow:hidden)
            tabBar.getElement().getStyle().setWidth(BIG_ENOUGH_TO_NOT_WRAP, Style.Unit.PX);

            tabBar.setStyleName("gwt-TabLayoutPanelTabs");
            setStyleName("gwt-TabLayoutPanel");
        }


        public void add(IsWidget w) {
            add(asWidgetOrNull(w));
        }


        public void add(IsWidget w, IsWidget tab) {
            add(asWidgetOrNull(w), asWidgetOrNull(tab));
        }


        public void add(IsWidget w, String text) {
            add(asWidgetOrNull(w), text);
        }


        public void add(IsWidget w, String text, boolean asHtml) {
            add(asWidgetOrNull(w), text, asHtml);
        }

        public void add(Widget w) {
            insert(w, getWidgetCount());
        }


        public void add(Widget child, String text) {
            insert(child, text, getWidgetCount());
        }


        public void add(Widget child, SafeHtml html) {
            add(child, html.asString(), true);
        }


        public void add(Widget child, String text, boolean asHtml) {
            insert(child, text, asHtml, getWidgetCount());
        }


        public void add(Widget child, Widget tab) {
            insert(child, tab, getWidgetCount());
        }

        public HandlerRegistration addBeforeSelectionHandler(
                BeforeSelectionHandler<Integer> handler) {
            return addHandler(handler, BeforeSelectionEvent.getType());
        }

        public HandlerRegistration addSelectionHandler(
                SelectionHandler<Integer> handler) {
            return addHandler(handler, SelectionEvent.getType());
        }

        public void animate(int duration) {
            animate(duration, null);
        }

        public void animate(int duration, Layout.AnimationCallback callback) {
            deckPanel.animate(duration, callback);
        }

        public void clear() {
            Iterator<Widget> it = iterator();
            while (it.hasNext()) {
                it.next();
                it.remove();
            }
        }

        public void forceLayout() {
            deckPanel.forceLayout();
        }


        public int getAnimationDuration() {
            return deckPanel.getAnimationDuration();
        }


        public int getSelectedIndex() {
            return selectedIndex;
        }


        public Widget getTabWidget(int index) {
            checkIndex(index);
            return tabs.get(index).getWidget();
        }


        public Widget getTabWidget(IsWidget child) {
            return getTabWidget(asWidgetOrNull(child));
        }


        public Widget getTabWidget(Widget child) {
            checkChild(child);
            return getTabWidget(getWidgetIndex(child));
        }

        /**
         * Returns the widget at the given index.
         */
        public Widget getWidget(int index) {
            return deckPanel.getWidget(index);
        }

        /**
         * Returns the number of tabs and widgets.
         */
        public int getWidgetCount() {
            return deckPanel.getWidgetCount();
        }

        /**
         * Convenience overload to allow {@link IsWidget} to be used directly.
         */
        public int getWidgetIndex(IsWidget child) {
            return getWidgetIndex(asWidgetOrNull(child));
        }

        /**
         * Returns the index of the given child, or -1 if it is not a child.
         */
        public int getWidgetIndex(Widget child) {
            return deckPanel.getWidgetIndex(child);
        }

        /**
         * Convenience overload to allow {@link IsWidget} to be used directly.
         */
        public void insert(IsWidget child, int beforeIndex) {
            insert(asWidgetOrNull(child), beforeIndex);
        }

        /**
         * Convenience overload to allow {@link IsWidget} to be used directly.
         */
        public void insert(IsWidget child, IsWidget tab, int beforeIndex) {
            insert(asWidgetOrNull(child), asWidgetOrNull(tab), beforeIndex);
        }

        /**
         * Convenience overload to allow {@link IsWidget} to be used directly.
         */
        public void insert(IsWidget child, String text, boolean asHtml, int beforeIndex) {
            insert(asWidgetOrNull(child), text, asHtml, beforeIndex);
        }

        /**
         * Convenience overload to allow {@link IsWidget} to be used directly.
         */
        public void insert(IsWidget child, String text, int beforeIndex) {
            insert(asWidgetOrNull(child), text, beforeIndex);
        }

        /**
         * Inserts a widget into the panel. If the Widget is already attached, it will
         * be moved to the requested index.
         *
         * @param child the widget to be added
         * @param beforeIndex the index before which it will be inserted
         */
        public void insert(Widget child, int beforeIndex) {
            insert(child, "", beforeIndex);
        }

        /**
         * Inserts a widget into the panel. If the Widget is already attached, it will
         * be moved to the requested index.
         *
         * @param child the widget to be added
         * @param html the html to be shown on its tab
         * @param beforeIndex the index before which it will be inserted
         */
        public void insert(Widget child, SafeHtml html, int beforeIndex) {
            insert(child, html.asString(), true, beforeIndex);
        }

        /**
         * Inserts a widget into the panel. If the Widget is already attached, it will
         * be moved to the requested index.
         *
         * @param child the widget to be added
         * @param text the text to be shown on its tab
         * @param asHtml <code>true</code> to treat the specified text as HTML
         * @param beforeIndex the index before which it will be inserted
         */
        public void insert(Widget child, String text, boolean asHtml, int beforeIndex) {
            Widget contents;
            if (asHtml) {
                contents = new HTML(text);
            } else {
                contents = new Label(text);
            }
            insert(child, contents, beforeIndex);
        }

        /**
         * Inserts a widget into the panel. If the Widget is already attached, it will
         * be moved to the requested index.
         *
         * @param child the widget to be added
         * @param text the text to be shown on its tab
         * @param beforeIndex the index before which it will be inserted
         */
        public void insert(Widget child, String text, int beforeIndex) {
            insert(child, text, false, beforeIndex);
        }

        /**
         * Inserts a widget into the panel. If the Widget is already attached, it will
         * be moved to the requested index.
         *
         * @param child the widget to be added
         * @param tab the widget to be placed in the associated tab
         * @param beforeIndex the index before which it will be inserted
         */
        public void insert(Widget child, Widget tab, int beforeIndex) {
            insert(child, new Tab(tab), beforeIndex);
        }

        /**
         * Check whether or not transitions slide in vertically or horizontally.
         * Defaults to horizontally.
         *
         * @return true for vertical transitions, false for horizontal
         */
        public boolean isAnimationVertical() {
            return deckPanel.isAnimationVertical();
        }

        public Iterator<Widget> iterator() {
            return deckPanel.iterator();
        }

        public boolean remove(int index) {
            if ((index < 0) || (index >= getWidgetCount())) {
                return false;
            }

            Widget child = getWidget(index);
            tabBar.remove(index);
            deckPanel.removeProtected(child);
            child.removeStyleName(CONTENT_STYLE);

            Tab tab = tabs.remove(index);
            tab.getWidget().removeFromParent();

            if (index == selectedIndex) {
                // If the selected tab is being removed, select the first tab (if there
                // is one).
                selectedIndex = -1;
                if (getWidgetCount() > 0) {
                    selectTab(0);
                }
            } else if (index < selectedIndex) {
                // If the selectedIndex is greater than the one being removed, it needs
                // to be adjusted.
                --selectedIndex;
            }
            return true;
        }

        public boolean remove(Widget w) {
            int index = getWidgetIndex(w);
            if (index == -1) {
                return false;
            }

            return remove(index);
        }

        /**
         * Programmatically selects the specified tab and fires events.
         *
         * @param index the index of the tab to be selected
         */
        public void selectTab(int index) {
            selectTab(index, true);
        }

        /**
         * Programmatically selects the specified tab.
         *
         * @param index the index of the tab to be selected
         * @param fireEvents true to fire events, false not to
         */
        public void selectTab(int index, boolean fireEvents) {
            checkIndex(index);
            if (index == selectedIndex) {
                return;
            }

            // Fire the before selection event, giving the recipients a chance to
            // cancel the selection.
            if (fireEvents) {
                BeforeSelectionEvent<Integer> event = BeforeSelectionEvent.fire(this,
                        index);
                if ((event != null) && event.isCanceled()) {
                    return;
                }
            }

            // Update the tabs being selected and unselected.
            if (selectedIndex != -1) {
                tabs.get(selectedIndex).setSelected(false);
            }

            deckPanel.showWidget(index);
            tabs.get(index).setSelected(true);
            selectedIndex = index;

            // Fire the selection event.
            if (fireEvents) {
                SelectionEvent.fire(this, index);
            }
        }

        /**
         * Convenience overload to allow {@link IsWidget} to be used directly.
         */
        public void selectTab(IsWidget child) {
            selectTab(asWidgetOrNull(child));
        }

        /**
         * Convenience overload to allow {@link IsWidget} to be used directly.
         */
        public void selectTab(IsWidget child, boolean fireEvents) {
            selectTab(asWidgetOrNull(child), fireEvents);
        }

        /**
         * Programmatically selects the specified tab and fires events.
         *
         * @param child the child whose tab is to be selected
         */
        public void selectTab(Widget child) {
            selectTab(getWidgetIndex(child));
        }

        /**
         * Programmatically selects the specified tab.
         *
         * @param child the child whose tab is to be selected
         * @param fireEvents true to fire events, false not to
         */
        public void selectTab(Widget child, boolean fireEvents) {
            selectTab(getWidgetIndex(child), fireEvents);
        }

        /**
         * Set the duration of the animated transition between tabs.
         *
         * @param duration the duration in milliseconds.
         */
        public void setAnimationDuration(int duration) {
            deckPanel.setAnimationDuration(duration);
        }

        /**
         * Set whether or not transitions slide in vertically or horizontally.
         *
         * @param isVertical true for vertical transitions, false for horizontal
         */
        public void setAnimationVertical(boolean isVertical) {
            deckPanel.setAnimationVertical(isVertical);
        }

        /**
         * Sets a tab's HTML contents.
         *
         * Use care when setting an object's HTML; it is an easy way to expose
         * script-based security problems. Consider using
         * {@link #setTabHTML(int, SafeHtml)} or
         * {@link #setTabText(int, String)} whenever possible.
         *
         * @param index the index of the tab whose HTML is to be set
         * @param html the tab's new HTML contents
         */
        public void setTabHTML(int index, String html) {
            checkIndex(index);
            tabs.get(index).setWidget(new HTML(html));
        }

        /**
         * Sets a tab's HTML contents.
         *
         * @param index the index of the tab whose HTML is to be set
         * @param html the tab's new HTML contents
         */
        public void setTabHTML(int index, SafeHtml html) {
            setTabHTML(index, html.asString());
        }

        /**
         * Sets a tab's text contents.
         *
         * @param index the index of the tab whose text is to be set
         * @param text the object's new text
         */
        public void setTabText(int index, String text) {
            checkIndex(index);
            tabs.get(index).setWidget(new Label(text));
        }

        private void checkChild(Widget child) {
            assert getWidgetIndex(child) >= 0 : "Child is not a part of this panel";
        }

        private void checkIndex(int index) {
            assert (index >= 0) && (index < getWidgetCount()) : "Index out of bounds";
        }

        private void insert(final Widget child, Tab tab, int beforeIndex) {
            assert (beforeIndex >= 0) && (beforeIndex <= getWidgetCount()) : "beforeIndex out of bounds";

            // Check to see if the TabPanel already contains the Widget. If so,
            // remove it and see if we need to shift the position to the left.
            int idx = getWidgetIndex(child);
            if (idx != -1) {
                remove(child);
                if (idx < beforeIndex) {
                    beforeIndex--;
                }
            }

            deckPanel.insertProtected(child, beforeIndex);
            tabs.add(beforeIndex, tab);

            tabBar.insert(tab, beforeIndex);
            tab.addClickHandler(new ClickHandler() {
                public void onClick(ClickEvent event) {
                    selectTab(child);
                }
            });

            child.addStyleName(CONTENT_STYLE);

            if (selectedIndex == -1) {
                selectTab(0);
            } else if (selectedIndex >= beforeIndex) {
                // If we inserted before the currently selected tab, its index has just
                // increased.
                selectedIndex++;
            }
        }
    }
