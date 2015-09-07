package ru.intertrust.cm.core.gui.impl.client.form.widget.tablebrowser;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.form.PopupTitlesHolder;
import ru.intertrust.cm.core.config.gui.form.widget.HasLinkedFormMappings;
import ru.intertrust.cm.core.config.gui.form.widget.SelectionStyleConfig;
import ru.intertrust.cm.core.gui.api.client.WidgetNavigator;
import ru.intertrust.cm.core.gui.impl.client.event.tooltip.WidgetItemRemoveEvent;
import ru.intertrust.cm.core.gui.impl.client.form.widget.BaseWidget;
import ru.intertrust.cm.core.gui.impl.client.form.widget.WidgetNavigatorImpl;
import ru.intertrust.cm.core.gui.impl.client.form.widget.hyperlink.HyperlinkClickHandler;
import ru.intertrust.cm.core.gui.impl.client.form.widget.hyperlink.HyperlinkDisplay;
import ru.intertrust.cm.core.gui.impl.client.form.widget.linkediting.AbstractWidgetDelegatedKeyDownHandler;
import ru.intertrust.cm.core.gui.impl.client.form.widget.linkediting.LinkEditingNavigationHandler;
import ru.intertrust.cm.core.gui.impl.client.form.widget.linkediting.WidgetDelegatedKeyDownHandler;
import ru.intertrust.cm.core.gui.impl.client.form.widget.panel.IdentifiedPanel;
import ru.intertrust.cm.core.gui.impl.client.form.widget.panel.WidgetCollectionPanel;
import ru.intertrust.cm.core.gui.impl.client.form.widget.tooltip.TooltipButtonClickHandler;
import ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants;
import ru.intertrust.cm.core.gui.impl.client.util.DisplayStyleBuilder;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 25.08.2014
 *         Time: 17:44
 */
public class TableBrowserItemsView extends Composite implements HyperlinkDisplay {
    private static final int INPUT_MARGIN = 40;

    private Style.Display displayStyle;
    protected TextBox filter;
    protected WidgetCollectionPanel root;
    protected Button openTooltip;
    private EventBus eventBus;
    private Map<String, PopupTitlesHolder> typeTitleMap;
    private HasLinkedFormMappings widget;

    private BaseWidget parent;
    private boolean modalWindow = true;

    public TableBrowserItemsView(SelectionStyleConfig selectionStyleConfig, EventBus eventBus,
                                 Map<String, PopupTitlesHolder> typeTitleMap, HasLinkedFormMappings widget,
                                 BaseWidget parent) {
        this.eventBus = eventBus;
        this.typeTitleMap = typeTitleMap;
        this.widget = widget;
        this.parent = parent;

        init(selectionStyleConfig);
        initWidget(root);
    }

    public TableBrowserItemsView withModalWindow(boolean modalWindow){
        this.modalWindow = modalWindow;
        return this;
    }

    private void init(SelectionStyleConfig selectionStyleConfig){
        root = new WidgetCollectionPanel();
        root.setStyleName("facebook-main-box linkedWidgetsBorderStyle");
        WidgetNavigator<IdentifiedPanel> widgetNavigator =
                new WidgetNavigatorImpl<IdentifiedPanel>(root.getChildren(), IdentifiedPanel.class);
        WidgetDelegatedKeyDownHandler<IdentifiedPanel> widgetDelegatedKeyDownHandler = new TableBrowserKeyDownHandler(widgetNavigator, eventBus);
        new LinkEditingNavigationHandler().handleNavigation(root, widgetDelegatedKeyDownHandler);
        displayStyle = DisplayStyleBuilder.getDisplayStyle(selectionStyleConfig);
    }

    public void addFocusedFilter() {
        filter = new TextBox();
        filter.setStyleName("tableBrowserFilterInput");
        root.add(filter);
        changeInputFilterWidth();
        filter.setFocus(true);

        filter.addBlurHandler(new BlurHandler() {
            @Override
            public void onBlur(BlurEvent event) {
                if (parent != null) {
                    parent.validate();
                }
            }
        });
    }

    public void clearContent() {
        clearItems();
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    private void displayChosenRowItem(Map.Entry<Id, String> entry) {
        final AbsolutePanel element = new AbsolutePanel();

        element.setStyleName("facebook-element");
        Label label = new Label(entry.getValue());
        label.setStyleName("facebook-label");
        label.addStyleName("facebook-clickable-label");
        FocusPanel delBtn = new FocusPanel();
        delBtn.addStyleName("facebook-btn facebookElementDel");
        final Id id = entry.getKey();
        delBtn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                element.removeFromParent();
                eventBus.fireEvent(new WidgetItemRemoveEvent(id, false));
            }
        });
        element.add(label);
        element.add(delBtn);
        if (displayStyle.equals(Style.Display.INLINE_BLOCK)) {
            element.getElement().getStyle().setFloat(Style.Float.LEFT);
            label.getElement().getStyle().setFloat(Style.Float.LEFT);
            delBtn.getElement().getStyle().setFloat(Style.Float.LEFT);
        }
        root.add(element);
    }

    private void displayChosenRowItemAsHyperlink(Map.Entry<Id, String> entry) {
        final Id id = entry.getKey();
        final AbsolutePanel element = new IdentifiedPanel(id);

        element.setStyleName("facebook-element");
        Label label = new Label(entry.getValue());
        label.setStyleName("facebook-label");
        label.addStyleName("facebook-clickable-label");
        ClickHandler clickHandler = new HyperlinkClickHandler(id, this, eventBus, false, typeTitleMap, widget).withModalWindow(modalWindow);
        label.addClickHandler(clickHandler);
        FocusPanel delBtn = new FocusPanel();
        delBtn.addStyleName("facebook-btn facebookElementDel");
        delBtn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                element.removeFromParent();
                eventBus.fireEvent(new WidgetItemRemoveEvent(id, false));
            }
        });
        element.add(label);
        element.add(delBtn);
        if (displayStyle.equals(Style.Display.INLINE_BLOCK)) {
            element.getElement().getStyle().setFloat(Style.Float.LEFT);
            label.getElement().getStyle().setFloat(Style.Float.LEFT);
            delBtn.getElement().getStyle().setFloat(Style.Float.LEFT);
        }
        root.add(element);
    }

    public void displayItems(LinkedHashMap<Id, String> items, boolean displayTooltipButton) {
        root.clear();
        Set<Map.Entry<Id, String>> entries = items.entrySet();
        for (Map.Entry<Id, String> entry : entries) {
            displayChosenRowItem(entry);
        }
        if (displayTooltipButton) {
            addTooltipButton();
        }
        addFocusedFilter();

    }

    public void displayHyperlinks(LinkedHashMap<Id, String> items, boolean displayTooltipButton) {
        root.clear();
        Set<Map.Entry<Id, String>> entries = items.entrySet();
        for (Map.Entry<Id, String> entry : entries) {
            displayChosenRowItemAsHyperlink(entry);
        }
        if (displayTooltipButton) {
            addTooltipButton();
        }
        addFocusedFilter();

    }

    private void addTooltipButton() {
        openTooltip = new Button("...");
        openTooltip.setStyleName("tooltipButton");
        root.add(openTooltip);
        openTooltip.addClickHandler(new TooltipButtonClickHandler(eventBus));

    }

    private void changeInputFilterWidth() {
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                Widget lastWidget = getWidgetBeforeInput();
                if (lastWidget == null || lastWidget.getElement() == null) {
                    return;
                }
                int width = root.getElement().getAbsoluteRight()
                        - lastWidget.getElement().getAbsoluteRight() - INPUT_MARGIN;
                if (width > 0) {
                    filter.setWidth(width + Style.Unit.PX.name());
                }

            }
        });
    }

    private Widget getWidgetBeforeInput() {
        int count = root.getWidgetCount();
        Widget result = null;
        if (count >= 2) {
            result = root.getWidget(count - 2);
        }
        return result;
    }

    public void clearItems(){
        root.clear();
        root.add(filter);
    }

    public void display(LinkedHashMap<Id, String> listValues, boolean drawTooltipButton, boolean displayAsHyperlinks){
        if (displayAsHyperlinks) {
            displayHyperlinks(listValues, drawTooltipButton);
        } else {
            displayItems(listValues, drawTooltipButton);
        }
    }
    public String getFilterValue(){
        return filter.getValue();
    }

    public void removeTooltipButton(){
        if(openTooltip != null){
            openTooltip.removeFromParent();
        }
    }

    private class TableBrowserKeyDownHandler extends AbstractWidgetDelegatedKeyDownHandler<IdentifiedPanel> {
        public TableBrowserKeyDownHandler(WidgetNavigator<IdentifiedPanel> widgetNavigator, EventBus eventBus) {
            super(widgetNavigator, eventBus);
        }

        @Override
        public void handleBackspaceOrDeleteDown() {
            if (widgetNavigator.getCurrent() != null) {
                IdentifiedPanel lastSelectionItem = widgetNavigator.getCurrent();
                Id id = lastSelectionItem.getId();
                eventBus.fireEvent(new WidgetItemRemoveEvent(id, false));
            }
            widgetNavigator.back();
            changeHighlighting(true);

        }

        public boolean shouldHandle(){
            return BusinessUniverseConstants.EMPTY_VALUE.equalsIgnoreCase(getFilterValue());
        }
    }

}
