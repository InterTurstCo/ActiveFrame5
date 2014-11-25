package ru.intertrust.cm.core.gui.impl.client.util;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.InlineHyperlink;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.config.gui.form.title.AbstractTitleRepresentationConfig;
import ru.intertrust.cm.core.config.gui.form.title.TitleConfig;
import ru.intertrust.cm.core.config.gui.form.widget.NodeCollectionDefConfig;
import ru.intertrust.cm.core.config.gui.form.widget.linkediting.LinkedFormMappingConfig;
import ru.intertrust.cm.core.config.gui.form.widget.linkediting.LinkedFormViewerConfig;
import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.api.client.Predicate;
import ru.intertrust.cm.core.gui.impl.client.FormPlugin;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.action.SaveAction;
import ru.intertrust.cm.core.gui.impl.client.event.CentralPluginChildOpeningRequestedEvent;
import ru.intertrust.cm.core.gui.impl.client.form.WidgetsContainer;
import ru.intertrust.cm.core.gui.impl.client.form.widget.BaseWidget;
import ru.intertrust.cm.core.gui.model.action.SaveActionContext;
import ru.intertrust.cm.core.gui.model.filters.ComplicatedFiltersParams;
import ru.intertrust.cm.core.gui.model.filters.WidgetIdComponentName;
import ru.intertrust.cm.core.gui.model.form.FormDisplayData;
import ru.intertrust.cm.core.gui.model.form.widget.LabelState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginConfig;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginData;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginState;
import ru.intertrust.cm.core.gui.model.plugin.calendar.CalendarItemData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 27.09.2014
 *         Time: 10:41
 */
public final class GuiUtil {

    public static final String[] MONTHS = {"январь", "февраль", "март", "апрель", "май", "июнь", "июль", "август",
            "сентябрь", "октябрь", "ноябрь", "декабрь"};
    public static final String[] WEEK_DAYS = {"воскресенье", "понедельник", "вторник", "среда", "четверг", "пятница",
            "суббота"};

    private GuiUtil(){}

    public static boolean isChildClicked(ClickEvent event, String id) {
        NodeList<Element> checkBoxes = Document.get().getElementById(id).getElementsByTagName("input");
        Element target = Element.as(event.getNativeEvent().getEventTarget());
        for (int i = 0; i < checkBoxes.getLength(); i++) {
            if (checkBoxes.getItem(i).isOrHasChild(target)) {
                return true;
            }
        }
        NodeList<Element> links = Document.get().getElementById(id).getElementsByTagName("div");
        for (int i = 0; i < links.getLength(); i++) {
            if (links.getItem(i).isOrHasChild(target)) {
                return true;
            }
        }
        return false;
    }

    public static String getConfiguredTitle(FormPlugin formPlugin, boolean isNewObjectForm) {
        FormPluginData formPluginData = formPlugin.getInitialData();
        FormDisplayData displayData = formPluginData.getFormDisplayData();
        TitleConfig titleConfig = displayData.getMarkup().getTitle();
        if (titleConfig == null) {
            return BusinessUniverseConstants.EMPTY_VALUE;
        }
        AbstractTitleRepresentationConfig config = isNewObjectForm ? titleConfig.getNewObjectConfig()
                : titleConfig.getExistingObjectConfig();
        LabelState labelState = (LabelState) displayData.getFormState().getWidgetState(config.getLabelWidgetId());
        return labelState.getLabel();

    }

    public static FormPluginConfig createFormPluginConfig(Id id, NodeCollectionDefConfig nodeConfig,
                                                          String domainObjectType, boolean editable) {

        FormPluginConfig result = new FormPluginConfig();
        result.setDomainObjectId(id);
        if (id == null) {
            result.setDomainObjectTypeToCreate(domainObjectType);
        }
        result.getPluginState().setEditable(editable);
        LinkedFormMappingConfig mappingConfig = nodeConfig.getLinkedFormMappingConfig();
        if (mappingConfig != null) {
            LinkedFormViewerConfig formViewerConfig = new LinkedFormViewerConfig();
            formViewerConfig.setLinkedFormConfig(mappingConfig.getLinkedFormConfigs());
            result.setFormViewerConfig(formViewerConfig);

        }
        return result;
    }

    public static FormPluginConfig createFormPluginConfig(LinkedFormMappingConfig mappingConfig, String domainObjectType) {
        FormPluginConfig result = new FormPluginConfig();
        result.setDomainObjectTypeToCreate(domainObjectType);
        result.getPluginState().setEditable(true);
        if (mappingConfig != null) {
            LinkedFormViewerConfig formViewerConfig = new LinkedFormViewerConfig();
            formViewerConfig.setLinkedFormConfig(mappingConfig.getLinkedFormConfigs());
            result.setFormViewerConfig(formViewerConfig);

        }
        return result;
    }

    public static SaveAction createSaveAction(final FormPlugin formPlugin, final Id rootObjectId, boolean dirtySensitivity) {
        SaveActionContext saveActionContext = new SaveActionContext();
        saveActionContext.setRootObjectId(rootObjectId);
        final ActionConfig actionConfig = new ActionConfig("save.action");
        actionConfig.setDirtySensitivity(dirtySensitivity);
        saveActionContext.setActionConfig(actionConfig);
        final SaveAction action = ComponentRegistry.instance.get(actionConfig.getComponentName());
        action.setInitialContext(saveActionContext);
        action.setPlugin(formPlugin);
        return action;
    }
    public static ComplicatedFiltersParams createComplicatedFiltersParams(String filterValue, String filterName, WidgetsContainer container,
                                                                          Collection<WidgetIdComponentName> widgetsIds){
        ComplicatedFiltersParams params = new ComplicatedFiltersParams();
        params.setInputFilterName(filterName);
        params.setInputFilterValue(filterValue);
        Plugin plugin = container.getPlugin();
        if (plugin != null && plugin.getConfig() != null) {
            FormPluginConfig formConfig = (FormPluginConfig) plugin.getConfig();
            params.setRootId(formConfig.getDomainObjectId());
        }
        params.setWidgetValuesMap(getOtherWidgetsValues(container, widgetsIds));
        return params;
    }
    public static ComplicatedFiltersParams createComplicatedFiltersParams(WidgetsContainer container,
                                                                          Collection<WidgetIdComponentName> widgetsIds){

        return createComplicatedFiltersParams(null, null, container, widgetsIds);
    }

    private static Map<WidgetIdComponentName, WidgetState> getOtherWidgetsValues(WidgetsContainer container,Collection<WidgetIdComponentName> widgetsIds){
        Map<WidgetIdComponentName, WidgetState> result = new HashMap<WidgetIdComponentName, WidgetState>();
        for (WidgetIdComponentName widgetIdComponentName : widgetsIds) {
            BaseWidget widget = container.getWidget(widgetIdComponentName.getWidgetId());
            WidgetState widgetState = widget.getCurrentState();

            result.put(widgetIdComponentName, widgetState);
        }
        return result;
    }


    public static Widget getCalendarItemPresentation(final CalendarItemData itemData, final Id rootObjectId) {
        final Widget result;
        String toolTipText;
        if (itemData.isLink()) {
            final Hyperlink hyperlink = new InlineHyperlink();
            hyperlink.setHTML(itemData.getPresentation());
            hyperlink.addHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    final FormPluginConfig formPluginConfig = new FormPluginConfig(rootObjectId);
                    final FormPluginState formPluginState = new FormPluginState();
                    formPluginState.setInCentralPanel(Application.getInstance().getCompactModeState().isExpanded());
                    formPluginState.setToggleEdit(true);
                    formPluginState.setInCentralPanel(true);
                    formPluginConfig.setPluginState(formPluginState);
                    final FormPlugin formPlugin = ComponentRegistry.instance.get("form.plugin");
                    formPlugin.setConfig(formPluginConfig);
                    formPlugin.setDisplayActionToolBar(true);
                    formPlugin.setLocalEventBus((EventBus) GWT.create(SimpleEventBus.class));
                    Application.getInstance().getEventBus().fireEvent(
                            new CentralPluginChildOpeningRequestedEvent(formPlugin));
                }
            }, ClickEvent.getType());
            hyperlink.addStyleName("calendarItemLink");
            toolTipText = hyperlink.getText();
            result = hyperlink;
        } else {
            InlineHTML inlineHtml = new InlineHTML(itemData.getPresentation());
            toolTipText = inlineHtml.getText();
            result = inlineHtml;

        }
        result.setTitle(toolTipText);
        return result;
    }

    public static<T> List<T> filter(List<T> collection, Predicate<T> predicate){
        List<T> result = new ArrayList<T>();
        for (T t : collection) {
            if(predicate.evaluate(t)){
                result.add(t);
            }
        }
        return result;

    }
}
