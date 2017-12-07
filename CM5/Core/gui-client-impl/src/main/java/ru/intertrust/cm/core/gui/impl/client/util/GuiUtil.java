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
import ru.intertrust.cm.core.business.api.dto.Case;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.config.gui.form.title.AbstractTitleRepresentationConfig;
import ru.intertrust.cm.core.config.gui.form.title.TitleConfig;
import ru.intertrust.cm.core.config.gui.form.widget.DialogWindowConfig;
import ru.intertrust.cm.core.config.gui.form.widget.LinkedDomainObjectsTableConfig;
import ru.intertrust.cm.core.config.gui.form.widget.LinkedFormConfig;
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
import ru.intertrust.cm.core.gui.impl.client.form.FormPanel;
import ru.intertrust.cm.core.gui.impl.client.form.WidgetsContainer;
import ru.intertrust.cm.core.gui.impl.client.form.widget.BaseWidget;
import ru.intertrust.cm.core.gui.model.Browser;
import ru.intertrust.cm.core.gui.model.Client;
import ru.intertrust.cm.core.gui.model.action.SaveActionContext;
import ru.intertrust.cm.core.gui.model.filters.ComplexFiltersParams;
import ru.intertrust.cm.core.gui.model.filters.WidgetIdComponentName;
import ru.intertrust.cm.core.gui.model.form.FormDisplayData;
import ru.intertrust.cm.core.gui.model.form.FormState;
import ru.intertrust.cm.core.gui.model.form.widget.LabelState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginConfig;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginData;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginState;
import ru.intertrust.cm.core.gui.model.plugin.calendar.CalendarItemData;
import ru.intertrust.cm.core.gui.model.util.WidgetUtil;

import java.util.*;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 27.09.2014
 *         Time: 10:41
 */
public final class GuiUtil {

    private GuiUtil() {
    }

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
        return labelState == null ? BusinessUniverseConstants.EMPTY_VALUE : labelState.getLabel();

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

    public static FormPluginConfig createNewFormPluginConfig(String domainObjectType, LinkedFormMappingConfig mappingConfig,
                                                             WidgetsContainer container,
                                                             Map<String, Collection<String>> parentWidgetIdsForNewFormMap) {
        FormPluginConfig config = GuiUtil.createFormPluginConfig(mappingConfig, domainObjectType);
        Collection<String> widgetIds = parentWidgetIdsForNewFormMap.get(Case.toLower(domainObjectType));
        config.setParentFormState(GuiUtil.createParentFormStatesHierarchy(container, widgetIds));
        config.setParentId(GuiUtil.getParentId(container));
        return config;
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

    public static ComplexFiltersParams createComplexFiltersParams(String filterValue, String filterName, WidgetsContainer container,
                                                                  Collection<WidgetIdComponentName> widgetsIds) {
        ComplexFiltersParams params = createComplexFiltersParams(container);
        params.setInputFilterName(filterName);
        params.setInputFilterValue(filterValue);
        params.setWidgetValuesMap(getOtherWidgetsValues(container, widgetsIds));
        return params;
    }

    public static ComplexFiltersParams createComplexFiltersParams(WidgetsContainer container,
                                                                  Collection<WidgetIdComponentName> widgetsIds) {
        return createComplexFiltersParams(null, null, container, widgetsIds);
    }

    public static ComplexFiltersParams createComplexFiltersParams(WidgetsContainer container) {
        ComplexFiltersParams params = new ComplexFiltersParams();
        params.setRootId(getParentId(container));
        return params;
    }

    public static Id getParentId(WidgetsContainer container) {
        Id id = null;
        Plugin plugin = container == null ? null : container.getPlugin();
        if (plugin != null) {
            FormPluginData pluginData = plugin.getInitialData();
            id = pluginData.getFormDisplayData().getFormState().getObjects().getRootNode().getDomainObject().getId();
        }
        return id;
    }

    private static Map<WidgetIdComponentName, WidgetState> getOtherWidgetsValues(WidgetsContainer container,
                                                                                 Collection<WidgetIdComponentName> widgetsIds) {
        if (WidgetUtil.isEmpty(widgetsIds)) {
            return null;
        }
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

    public static <T> List<T> filter(List<T> collection, Predicate<T> predicate) {
        List<T> result = new ArrayList<T>();
        for (T t : collection) {
            if (predicate.evaluate(t)) {
                result.add(t);
            }
        }
        return result;

    }

    public static <T> T find(List<T> collection, Predicate<T> predicate) {
        for (T t : collection) {
            if (predicate.evaluate(t)) {
                return t;
            }
        }
        return null;
    }

    public static Client getClient() {
        return new Browser(getUserAgent(), GuiDateUtil.getClientTimeZoneId());
    }

    /**
     * Gets the name of the used browser.
     */
    public static native String getUserAgent() /*-{
        return navigator.userAgent;
    }-*/;

    public static String getModalHeight(String domainObjectType, LinkedFormMappingConfig linkedFormMappingConfig,
                                        LinkedFormConfig lowPriorityLinkedFormConfig) {
        LinkedFormConfig linkedFormConfig = null;
        if (domainObjectType != null) {
            linkedFormConfig = getLinkedFormConfig(domainObjectType, linkedFormMappingConfig);
            if (linkedFormConfig != null && linkedFormConfig.getModalHeight() != null) {
                return linkedFormConfig.getModalHeight();
            }
        }
        linkedFormConfig = lowPriorityLinkedFormConfig;
        if (linkedFormConfig != null && linkedFormConfig.getModalHeight() != null) {
            return linkedFormConfig.getModalHeight();
        }

        return null;
    }

    public static String getModalWidth(String domainObjectType, LinkedFormMappingConfig linkedFormMappingConfig,
                                       LinkedFormConfig lowPriorityLinkedFormConfig) {
        LinkedFormConfig linkedFormConfig = null;
        if (domainObjectType != null) {
            linkedFormConfig = getLinkedFormConfig(domainObjectType, linkedFormMappingConfig);
            if (linkedFormConfig != null && linkedFormConfig.getModalWidth() != null) {
                return linkedFormConfig.getModalWidth();
            }
        }
        linkedFormConfig = lowPriorityLinkedFormConfig;
        if (linkedFormConfig != null && linkedFormConfig.getModalWidth() != null) {
            return linkedFormConfig.getModalWidth();
        }

        return null;
    }

    public static String getModalHeight(String domainObjectType, LinkedDomainObjectsTableConfig config) {
        LinkedFormMappingConfig linkedFormMappingConfig = config.getLinkedFormMappingConfig();
        LinkedFormConfig linkedFormConfig = config.getLinkedFormConfig();
        String modalHeight = GuiUtil.getModalHeight(domainObjectType, linkedFormMappingConfig, linkedFormConfig);

        return modalHeight == null ? config.getModalHeight() : modalHeight;
    }

    public static String getModalWidth(String domainObjectType, LinkedDomainObjectsTableConfig config) {
        LinkedFormMappingConfig linkedFormMappingConfig = config.getLinkedFormMappingConfig();
        LinkedFormConfig linkedFormConfig = config.getLinkedFormConfig();
        String modalWidth = GuiUtil.getModalWidth(domainObjectType, linkedFormMappingConfig, linkedFormConfig);

        return modalWidth == null ? config.getModalWidth() : modalWidth;
    }

    private static LinkedFormConfig getLinkedFormConfig(String domainObjectType, LinkedFormMappingConfig mappingConfig) {
        if (mappingConfig != null && domainObjectType != null) {
            for (LinkedFormConfig linkedFormConfig : mappingConfig.getLinkedFormConfigs()) {
                if (domainObjectType.equalsIgnoreCase(linkedFormConfig.getDomainObjectType())) {
                    return linkedFormConfig;
                }
            }
        }
        return null;
    }

    public static boolean isFormResizable(String domainObjectType, LinkedFormMappingConfig linkedFormMappingConfig,
                                          LinkedFormConfig lowPriorityLinkedFormConfig) {
        boolean result = false;
        LinkedFormConfig linkedFormConfig = getLinkedFormConfig(domainObjectType, linkedFormMappingConfig);
        if (linkedFormConfig == null) {
            result = lowPriorityLinkedFormConfig != null && lowPriorityLinkedFormConfig.isResizable();
        } else {
            result = linkedFormConfig.isResizable();
        }
        return result;
    }

    public static FormState createParentFormStatesHierarchy(WidgetsContainer container, Collection<String> widgetsIds) {
        if (WidgetUtil.isEmpty(widgetsIds)) {
            return null;
        }
        FormState formState = createParentFormState(container, widgetsIds);
        fillFormStateWithParentsFormStates(formState, container);
        if(container instanceof FormPanel && formState.getName()==null){
            formState.setName(((FormPanel)container).getFormIdentifier());
        }
        return formState;
    }

    private static void fillFormStateWithParentsFormStates(FormState formState, WidgetsContainer container) {
        FormState nestedFormState = formState;
        WidgetsContainer parentWidgetContainer = container;
        do {
            parentWidgetContainer = parentWidgetContainer.getParentWidgetsContainer();
            FormState parentFormState = createParentFormState(parentWidgetContainer);
            Id parentId = getParentId(parentWidgetContainer);
            if (parentFormState != null) {
                parentFormState.setParentId(parentId);
                nestedFormState.setParentState(parentFormState);
                nestedFormState = parentFormState;
            }
        }
        while (parentWidgetContainer != null);
    }

    private static FormState createParentFormState(WidgetsContainer container, Collection<String> widgetsIds) {
        Map<String, WidgetState> widgetStateMap = new HashMap<String, WidgetState>();
        for (String widgetId : widgetsIds) {
            BaseWidget widget = container.getWidget(widgetId);
            if (widget != null) {
                WidgetState widgetState = widget.getCurrentState();
                widgetStateMap.put(widgetId, widgetState);
            }
        }
        FormState formState = new FormState();
        formState.setWidgetStateMap(widgetStateMap);
        return formState;
    }

    private static FormState createParentFormState(WidgetsContainer container) {
        if (container == null) {
            return null;
        }
        Map<String, WidgetState> widgetStateMap = new HashMap<String, WidgetState>();
        for (BaseWidget widget : container.getWidgets()) {
            if (widget.isEditable()) {
                WidgetState widgetState = widget.getCurrentState();
                widgetStateMap.put(widget.getDisplayConfig().getId(), widgetState);
            }
        }
        FormState formState = new FormState();
        formState.setWidgetStateMap(widgetStateMap);
        return formState;
    }

    public static boolean isDialogWindowResizable(DialogWindowConfig dialogWindowConfig) {
        return dialogWindowConfig != null && dialogWindowConfig.isResizable();
    }

    public static List<LinkedFormConfig> getLinkedFormConfigs(LinkedFormConfig linkedFormConfig,
                                                              LinkedFormMappingConfig linkedFormMappingConfig) {
        List<LinkedFormConfig> result = null;
        if (linkedFormMappingConfig != null) {
            result = linkedFormMappingConfig.getLinkedFormConfigs();
        } else if (linkedFormConfig != null) {
            result = Arrays.asList(linkedFormConfig);
        }
        return result;
    }
}
