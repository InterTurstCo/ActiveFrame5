package ru.intertrust.cm.core.gui.impl.client.form.widget.tablebrowser;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.config.gui.form.widget.*;
import ru.intertrust.cm.core.gui.impl.client.PluginPanel;
import ru.intertrust.cm.core.gui.impl.client.event.collection.CollectionChangeSelectionEvent;
import ru.intertrust.cm.core.gui.impl.client.form.widget.BaseWidget;
import ru.intertrust.cm.core.gui.impl.client.form.widget.buttons.ClearAllConfiguredButton;
import ru.intertrust.cm.core.gui.impl.client.form.widget.buttons.ConfiguredButton;
import ru.intertrust.cm.core.gui.impl.client.form.widget.buttons.OpenCollectionConfiguredButton;
import ru.intertrust.cm.core.gui.impl.client.form.widget.hyperlink.HyperlinkNoneEditablePanel;
import ru.intertrust.cm.core.gui.impl.client.util.GuiUtil;
import ru.intertrust.cm.core.gui.model.form.widget.TableBrowserState;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 19.11.2014
 *         Time: 6:12
 */
public class TableBrowserViewsBuilder {
    public static final int DEFAULT_DIALOG_WIDTH = 800;
    public static final int DEFAULT_DIALOG_HEIGHT = 300;
    public static final int DEFAULT_TABLE_HEIGHT = 300;
    public static final int DEFAULT_TABLE_WIDTH = 500;
    public static final int DEFAULT_HEIGHT_OFFSET = 100;
    public static final int MINIMAL_DIALOG_WIDTH= 300;
    public static final int MINIMAL_DIALOG_HEIGHT = 200;
    public static final int MINIMAL_TABLE_WIDTH = 300;
    public static final int MINIMAL_TABLE_HEIGHT = 200;
    private int dialogWidth;
    private int dialogHeight;
    private int tableHeight;
    private Integer externalHeight;
    private int tableWidth;
    private TableBrowserState state;
    private HasLinkedFormMappings hasLinkedFormMappings;
    private EventBus eventBus;
    private boolean editable;
    private ClickHandler openCollectionButtonHandler;
    private ConfiguredButton createLinkedItemButton;
    private WidgetDisplayConfig displayConfig;
    private BaseWidget parent;

    public TableBrowserViewsBuilder withState(TableBrowserState state) {
        this.state = state;
        return this;
    }

    public TableBrowserViewsBuilder withHasLinkedFormMappings(HasLinkedFormMappings hasLinkedFormMappings) {
        this.hasLinkedFormMappings = hasLinkedFormMappings;
        return this;
    }

    public TableBrowserViewsBuilder withEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
        return this;

    }

    public TableBrowserViewsBuilder withHeight(Integer height) {
        this.externalHeight =height;
        return this;

    }


    public TableBrowserViewsBuilder withEditable(boolean editable) {
        this.editable = editable;
        return this;

    }

    public TableBrowserViewsBuilder withOpenCollectionButtonHandler(ClickHandler clickHandler) {
        this.openCollectionButtonHandler = clickHandler;
        return this;

    }

    public TableBrowserViewsBuilder withCreateLinkedItemButton(ConfiguredButton button) {
        this.createLinkedItemButton = button;
        return this;

    }

    public TableBrowserViewsBuilder withWidgetDisplayConfig(WidgetDisplayConfig displayConfig) {
        this.displayConfig = displayConfig;
        return this;

    }

    public TableBrowserViewsBuilder withParentWidget(BaseWidget parent) {
        this.parent = parent;
        return this;
    }

    public ViewHolder buildViewHolder() {
        if (editable) {
            return new TableBrowserEditableViewBuilder().buildViewHolder();
        } else {
            return new TableBrowserNoneEditableViewBuilder().buildViewHolder();
        }
    }
    public CollectionDialogBox buildCollectionDialogBox(){
        PluginPanel pluginPanel = createDialogCollectionPluginPanel();
        CollectionDialogBox dialogBox = new CollectionDialogBox()
                .withDialogWidth(dialogWidth)
                .setDialogHeight(dialogHeight)
                .setPluginPanel(pluginPanel)
                .setResizable(GuiUtil.isDialogWindowResizable(state.getTableBrowserConfig().getDialogWindowConfig()));
        dialogBox.init();
        return dialogBox;

    }

    private void initDialogWindowSize() {
        DialogWindowConfig dialogWindowConfig = state.getTableBrowserConfig().getDialogWindowConfig();
        String widthString = dialogWindowConfig != null ? dialogWindowConfig.getWidth() : null;
        String heightString = dialogWindowConfig != null ? dialogWindowConfig.getHeight() : null;
        dialogWidth = widthString == null ? DEFAULT_DIALOG_WIDTH : Integer.parseInt(widthString.replaceAll("\\D+", ""));
        dialogHeight = heightString == null ? DEFAULT_DIALOG_HEIGHT : Integer.parseInt(heightString.replaceAll("\\D+", ""));
    }

    private void initWidgetSize() {
        String heightString = displayConfig!= null ? displayConfig.getHeight() : null;
        tableHeight = heightString == null ? ((externalHeight==null)?DEFAULT_TABLE_HEIGHT:externalHeight) : Integer.parseInt(heightString.replaceAll("\\D+", ""));
        String widthString = displayConfig!= null ? displayConfig.getWidth() : null;
        tableWidth = widthString == null ? DEFAULT_TABLE_WIDTH : Integer.parseInt(widthString.replaceAll("\\D+", ""));
    }

    public PluginPanel createDialogCollectionPluginPanel() {
        initDialogWindowSize();
        PluginPanel pluginPanel = new PluginPanel();
        pluginPanel.setVisibleWidth(dialogWidth);
        pluginPanel.setVisibleHeight(dialogHeight - DEFAULT_HEIGHT_OFFSET);//it's height of table body only. TODO: eliminate hardcoded value
        return pluginPanel;

    }
    public PluginPanel createWidgetCollectionPluginPanel() {
        initWidgetSize();
        PluginPanel pluginPanel = new PluginPanel();
        pluginPanel.setVisibleWidth(tableWidth);
        pluginPanel.setVisibleHeight(tableHeight - DEFAULT_HEIGHT_OFFSET);
        return pluginPanel;

    }

    class TableBrowserNoneEditableViewBuilder {
        private ViewHolder buildViewHolder() {

            if (state.isTableView()) {
                TableBrowserCollection collection = createTableBrowserCollection(true, false,
                        state.getTableBrowserConfig().isResizable());
                return new TableBrowserCollectionViewHolder(collection);
            } else {
                SelectionStyleConfig styleConfig = state.getTableBrowserConfig().getSelectionStyleConfig();
                DisplayValuesAsLinksConfig displayValuesAsLinksConfig = state.getTableBrowserConfig().getDisplayValuesAsLinksConfig();
                boolean modalWindow = displayValuesAsLinksConfig == null || displayValuesAsLinksConfig.isModalWindow();
                HyperlinkNoneEditablePanel widget = new HyperlinkNoneEditablePanel(styleConfig, eventBus, false,
                        state.getTypeTitleMap(), hasLinkedFormMappings).withHyperlinkModalWindow(modalWindow);
                return new HyperlinkNoneEditablePanelViewHolder(widget);
            }
        }
    }

    class TableBrowserEditableViewBuilder {
        private ViewHolder buildViewHolder() {
            SelectionStyleConfig styleConfig = state.getTableBrowserConfig().getSelectionStyleConfig();
            DisplayValuesAsLinksConfig displayValuesAsLinksConfig = state.getTableBrowserConfig().getDisplayValuesAsLinksConfig();
            boolean modalWindow = displayValuesAsLinksConfig == null || displayValuesAsLinksConfig.isModalWindow();
            TableBrowserItemsView itemsWidget = new TableBrowserItemsView(styleConfig, eventBus, state.getTypeTitleMap(),
                    hasLinkedFormMappings, parent).withModalWindow(modalWindow);
            if (state.isTableView()) {
                TableBrowserCollection collection = createTableBrowserCollection(false, true,
                        state.getTableBrowserConfig().isResizable());
                TableBrowserCollectionViewHolder itemsWidgetChildViewHolder = new TableBrowserCollectionViewHolder(collection);
                TableBrowserItemsViewHolder editableWidgetChildViewHolder = new TableBrowserItemsViewHolder(itemsWidget);
                editableWidgetChildViewHolder.setChildViewHolder(itemsWidgetChildViewHolder);
                TableBrowserEditableView editableView = createEditableTableWidgetView(itemsWidget, collection);
                TableBrowserEditableViewHolder tableBrowserEditableViewHolder = new TableBrowserEditableViewHolder(editableView);
                tableBrowserEditableViewHolder.setChildViewHolder(editableWidgetChildViewHolder);
                return tableBrowserEditableViewHolder;
            } else {
                TableBrowserItemsViewHolder childViewHolder = new TableBrowserItemsViewHolder(itemsWidget);
                TableBrowserEditableView editableView = createEditableWidgetView(itemsWidget);
                TableBrowserEditableViewHolder tableBrowserEditableViewHolder = new TableBrowserEditableViewHolder(editableView);
                tableBrowserEditableViewHolder.setChildViewHolder(childViewHolder);
                return tableBrowserEditableViewHolder;
            }
        }
    }


    private TableBrowserEditableView createEditableWidgetView(final TableBrowserItemsView itemsWidget) {
        final TableBrowserEditableView root = new TableBrowserEditableView();
        ConfiguredButton openDialogButton = createOpenButton();
        openDialogButton.addClickHandler(openCollectionButtonHandler);
        root.addStyleName("tableBrowserRoot");
        root.addHeaderWidget(itemsWidget);
        root.addHeaderWidget(openDialogButton);
        if (createLinkedItemButton != null) {
            root.addHeaderWidget(createLinkedItemButton);
        }
        ConfiguredButton clearButton = createClearButton();
        if (clearButton != null) {
            clearButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    state.clearState();
                    itemsWidget.clearContent();

                }
            });
            root.addHeaderWidget(clearButton);
        }

        return root;
    }
    private TableBrowserEditableView createEditableTableWidgetView(final TableBrowserItemsView itemsView,
                                                                   TableBrowserCollection collection) {
        final TableBrowserEditableView root = new TableBrowserEditableView();
        root.addStyleName("tableBrowserRoot");
        root.addHeaderWidget(itemsView);
        root.addBodyWidget(collection);
        if (createLinkedItemButton != null) {
            root.addHeaderWidget(createLinkedItemButton);
        }
        ConfiguredButton clearButton = createClearButton();
        if (clearButton != null) {
            clearButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    eventBus.fireEvent(new CollectionChangeSelectionEvent(state.getIds(), false));
                    state.clearState();
                    itemsView.clearContent();
                }
            });
            root.addHeaderWidget(clearButton);
        }

        return root;
    }

    private TableBrowserCollection createTableBrowserCollection(Boolean displayOnlySelectedIds,
                                                                Boolean displayCheckBoxes, Boolean resizable) {
        TableBrowserCollection result = new TableBrowserCollection()
                .withEventBus(eventBus)
                .withPluginPanel(createWidgetCollectionPluginPanel())
                .withHeight(tableHeight)
                .withDisplayOnlyChosenIds(displayOnlySelectedIds)
                .withDisplayCheckBoxes(displayCheckBoxes)
                .withResizable(resizable)
                .createView();

        return result;
    }

    private ConfiguredButton createOpenButton() {
       TableBrowserConfig tableBrowserConfig = state.getTableBrowserConfig();
       return  new OpenCollectionConfiguredButton(tableBrowserConfig.getAddButtonConfig());
    }

    private ConfiguredButton createClearButton() {
        TableBrowserConfig tableBrowserConfig = state.getTableBrowserConfig();
        ClearAllButtonConfig config = tableBrowserConfig.getClearAllButtonConfig();
        if (config != null) {
            return new ClearAllConfiguredButton(config);
        }
        return null;

    }

}
