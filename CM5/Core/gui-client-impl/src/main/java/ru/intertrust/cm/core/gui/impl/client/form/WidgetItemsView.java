package ru.intertrust.cm.core.gui.impl.client.form;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.form.PopupTitlesHolder;
import ru.intertrust.cm.core.config.gui.form.widget.HasLinkedFormMappings;
import ru.intertrust.cm.core.config.gui.form.widget.SelectionStyleConfig;
import ru.intertrust.cm.core.gui.api.client.WidgetNavigator;
import ru.intertrust.cm.core.gui.impl.client.event.tooltip.WidgetItemRemoveEvent;
import ru.intertrust.cm.core.gui.impl.client.form.widget.WidgetNavigatorImpl;
import ru.intertrust.cm.core.gui.impl.client.form.widget.hyperlink.HyperlinkClickHandler;
import ru.intertrust.cm.core.gui.impl.client.form.widget.hyperlink.HyperlinkDisplay;
import ru.intertrust.cm.core.gui.impl.client.form.widget.linkediting.AbstractWidgetDelegatedKeyDownHandler;
import ru.intertrust.cm.core.gui.impl.client.form.widget.linkediting.LinkEditingNavigationHandler;
import ru.intertrust.cm.core.gui.impl.client.form.widget.linkediting.WidgetDelegatedKeyDownHandler;
import ru.intertrust.cm.core.gui.impl.client.form.widget.panel.IdentifiedPanel;
import ru.intertrust.cm.core.gui.impl.client.form.widget.panel.WidgetCollectionPanel;
import ru.intertrust.cm.core.gui.impl.client.util.DisplayStyleBuilder;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 01.12.13
 *         Time: 16:15
 */
public class WidgetItemsView extends Composite implements HyperlinkDisplay {
    private WidgetCollectionPanel mainBoxPanel;
    protected FocusPanel container;
    private Style.Display displayStyle;
    private EventBus eventBus;
    private boolean tooltipContent;
    private Map<String, PopupTitlesHolder> typeTitleMap;
    private HasLinkedFormMappings widget;
    @Deprecated // use constructor with event bus instead
    public WidgetItemsView(SelectionStyleConfig selectionStyleConfig, Map<String, PopupTitlesHolder> typeTitleMap, HasLinkedFormMappings widget) {
        this.widget = widget;
        mainBoxPanel = new WidgetCollectionPanel();
        mainBoxPanel.setStyleName("facebook-main-box linkedWidgetsBorderStyle");
        displayStyle = DisplayStyleBuilder.getDisplayStyle(selectionStyleConfig);
        container = new FocusPanel(mainBoxPanel);

        this.typeTitleMap = typeTitleMap;
        initWidget(container);
    }
    public WidgetItemsView(SelectionStyleConfig selectionStyleConfig, Map<String, PopupTitlesHolder> typeTitleMap, HasLinkedFormMappings widget, EventBus eventBus) {
        this.widget = widget;
        this.eventBus = eventBus;
        mainBoxPanel = new WidgetCollectionPanel();
        mainBoxPanel.setStyleName("facebook-main-box linkedWidgetsBorderStyle");
        displayStyle = DisplayStyleBuilder.getDisplayStyle(selectionStyleConfig);
        container = new FocusPanel(mainBoxPanel);
        WidgetNavigator<IdentifiedPanel> widgetNavigator =
                new WidgetNavigatorImpl<IdentifiedPanel>(mainBoxPanel.getChildren(), IdentifiedPanel.class);
        WidgetDelegatedKeyDownHandler<IdentifiedPanel> widgetDelegatedKeyDownHandler = new WidgetItemsKeyDownHandler(widgetNavigator, eventBus);
        new LinkEditingNavigationHandler().handleNavigation(container, widgetDelegatedKeyDownHandler);
        this.typeTitleMap = typeTitleMap;
        initWidget(container);
    }

    @Deprecated
    public void setTooltipContent(boolean tooltipContent) {
        this.tooltipContent = tooltipContent;
    }
    @Deprecated
    public EventBus getEventBus() {
        return eventBus;
    }
    @Deprecated
    public void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public void displayChosenRowItem(Map.Entry<Id, String> entry) {
        final Id id = entry.getKey();
        final AbsolutePanel element = new IdentifiedPanel(id);

        element.setStyleName("facebook-element");
        Label label = new Label(entry.getValue());
        label.setStyleName("facebook-label");
        label.addStyleName("facebook-clickable-label");
        FocusPanel delBtn = new FocusPanel();
        delBtn.addStyleName("facebook-btn facebookElementDel");

        delBtn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                element.removeFromParent();
                eventBus.fireEvent(new WidgetItemRemoveEvent(id, tooltipContent));

            }
        });
        element.add(label);
        element.add(delBtn);
        if (displayStyle.equals(Style.Display.INLINE_BLOCK)) {
            element.getElement().getStyle().setFloat(Style.Float.LEFT);
            label.getElement().getStyle().setFloat(Style.Float.LEFT);

            delBtn.getElement().getStyle().setFloat(Style.Float.LEFT);
        }
        mainBoxPanel.add(element);
    }

    private void displayChosenRowItemAsHyperlink(Map.Entry<Id, String> entry) {
        final Id id = entry.getKey();
        final AbsolutePanel element = new IdentifiedPanel(id);
        element.setStyleName("facebook-element");
        Label label = new Label(entry.getValue());
        label.setStyleName("facebook-label");
        label.addStyleName("facebook-clickable-label");

        label.addClickHandler(new HyperlinkClickHandler(id, this, eventBus, tooltipContent, typeTitleMap, widget));
        FocusPanel delBtn = new FocusPanel();
        delBtn.addStyleName("facebook-btn facebookElementDel");
        delBtn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                element.removeFromParent();
                eventBus.fireEvent(new WidgetItemRemoveEvent(id, tooltipContent));

            }
        });
        element.add(label);
        element.add(delBtn);
        if (displayStyle.equals(Style.Display.INLINE_BLOCK)) {
            element.getElement().getStyle().setFloat(Style.Float.LEFT);
            label.getElement().getStyle().setFloat(Style.Float.LEFT);
            delBtn.getElement().getStyle().setFloat(Style.Float.LEFT);
        }
        mainBoxPanel.add(element);
    }

    public void displayItems(LinkedHashMap<Id, String> listValues, boolean drawTooltipButton) {
        mainBoxPanel.clear();
        Set<Map.Entry<Id, String>> entries = listValues.entrySet();
        for (Map.Entry<Id, String> entry : entries) {
            displayChosenRowItem(entry);
        }


    }

    public void displayHyperlinks(LinkedHashMap<Id, String> listValues, boolean drawTooltipButton) {
        mainBoxPanel.clear();
        Set<Map.Entry<Id, String>> entries = listValues.entrySet();
        for (Map.Entry<Id, String> entry : entries) {
            displayChosenRowItemAsHyperlink(entry);
        }


    }

    public boolean isEmpty() {
        return mainBoxPanel.getWidgetCount() == 0;
    }

    private class WidgetItemsKeyDownHandler extends AbstractWidgetDelegatedKeyDownHandler<IdentifiedPanel> {
        public WidgetItemsKeyDownHandler(WidgetNavigator<IdentifiedPanel> widgetNavigator, EventBus eventBus) {
            super(widgetNavigator, eventBus);
        }

        @Override
        public void handleBackspaceOrDeleteDown() {
            if (widgetNavigator.getCurrent() != null) {
                IdentifiedPanel lastSelectionItem = widgetNavigator.getCurrent();
                lastSelectionItem.removeFromParent();
                Id id = lastSelectionItem.getId();
                eventBus.fireEvent(new WidgetItemRemoveEvent(id, true));
            }
            widgetNavigator.back();
            changeHighlighting(true);

        }

    }

}


