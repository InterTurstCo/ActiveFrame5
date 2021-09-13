package ru.intertrust.cm.core.gui.impl.client;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.StatusCodeException;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.gui.navigation.NavigationConfig;
import ru.intertrust.cm.core.config.gui.navigation.PluginConfig;
import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.api.client.BaseComponent;
import ru.intertrust.cm.core.gui.api.client.event.PluginCloseListener;
import ru.intertrust.cm.core.gui.api.server.plugin.PluginHandler;
import ru.intertrust.cm.core.gui.impl.client.event.PluginViewCreatedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.PluginViewCreatedEventListener;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.action.ToolbarContext;
import ru.intertrust.cm.core.gui.model.plugin.ActivePluginData;
import ru.intertrust.cm.core.gui.model.plugin.PluginData;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * <p>
 * Базовый класс плагинов системы. Плагин - это элемент управления, который может быть внедрён в соответствующую панель
 * - {@link PluginPanel}. Плагин может быть наделён логикой, выполняемой на сервере. За обработку сообщений и команд
 * плагинов на стороне сервера отвечают наследники класса {@link PluginHandler}.
 * </p>
 * <p>
 * Плагин является компонентом GUI и должен быть именован {@link ru.intertrust.cm.core.gui.model.ComponentName}.
 * </p>
 *
 * @author Denis Mitavskiy
 *         Date: 14.08.13
 *         Time: 13:01
 */
public abstract class Plugin extends BaseComponent {

    private PluginPanel owner;
    private PluginConfig config;
    private boolean displayActionToolBar;
    private boolean displayInfobar;

    private PluginData initialData;
    private boolean initialDataAssigned;
    private PluginView view;
    private List<HandlerRegistration> handlerRegistrations = new ArrayList<HandlerRegistration>();
    private List<PluginViewCreatedEventListener> viewCreatedEventListeners = new ArrayList<PluginViewCreatedEventListener>(1);
    private List<PluginCloseListener> pluginCloseListeners = new ArrayList<PluginCloseListener>(1);
    /**
     * Нужно ли показывать индикатор выполнения запроса через 1 секунду (игнорируется, если экран не блокировался)
     */
    protected boolean indicateLockScreen = true;
    static Logger logger = Logger.getLogger("plugin logger");
    private NavigationConfig navigationConfig;

    protected boolean showBreadcrumbs = true;

    /**
     * Создаёт представление данного плагина
     *
     * @return представление данного плагина
     */
    public abstract PluginView createView();

    public void addViewCreatedListener(PluginViewCreatedEventListener listener) {
        viewCreatedEventListeners.add(listener);
    }

    public void addPluginCloseListener(PluginCloseListener listener){
        pluginCloseListeners.add(listener);
    }

    public void onDataLoadFailure(Throwable cause) {
        Application.getInstance().unlockScreen();
        String message;
        if (cause instanceof StatusCodeException) {
            message = "Невозможно подключиться к серверу.";
        } else {
            message = cause.getMessage();
        }
        ApplicationWindow.errorAlert("Ошибка инициализации плагина " + this.getName() + ": " + message);
    }

    /**
     * Возвращает текущее состояние приложения
     *
     * @return текущее состояние приложения
     */
    public Dto getCurrentState() {
        return null;
    }

    public EventBus getLocalEventBus() {
        throw new UnsupportedOperationException(getName() + " does not support event bus");
    }

    /**
     * Производит первичную инициализацию плагина, загружая необходимые для него данные. Серверная инициализация
     * производится соответствующим обработчиком плагина в методе {@link PluginHandler#initialize(Dto)}
     */
    protected void setUp() {

        registerEventsHandling(getEventTypesToHandle());
        if (!shouldBeInitialized()) {
            postSetUp();
            return;
        }

        AsyncCallback<Dto> callback = new AsyncCallback<Dto>() {
            @Override
            public void onSuccess(Dto result) {
                Plugin.this.setInitialData((PluginData) result);
                postSetUp();
            }

            @Override
            public void onFailure(Throwable caught) {
                Plugin.this.onDataLoadFailure(caught);
            }
        };
        fillHistoryData();
        final Command command = new Command("initialize", this.getName(), getConfig());
        BusinessUniverseServiceAsync.Impl.executeCommand(command, callback, true, indicateLockScreen);
    }

    protected GwtEvent.Type[] getEventTypesToHandle() {
        return null;
    }

    void setIndicateLockScreen(boolean indicateLockScreen) {
        this.indicateLockScreen = indicateLockScreen;
    }

    public boolean isIndicateLockScreen() {
        return indicateLockScreen;
    }

    private void registerEventsHandling(GwtEvent.Type[] events) {
        if (events == null) {
            return;
        }
        for (GwtEvent.Type eventType : events) {
            HandlerRegistration handlerRegistration = Application.getInstance().getEventBus().addHandler(eventType, this);
            handlerRegistrations.add(handlerRegistration);
        }
    }

    private void reinit(PluginData initData) {
        getOwner().closeCurrentPlugin();
        Plugin.this.setInitialData(initData);
        postSetUp();
    }

    public void clearHandlers() {
        for (HandlerRegistration registration : handlerRegistrations) {
            registration.removeHandler();
        }
        handlerRegistrations.clear();
    }

    /**
     * Возвращает представление плагина.
     *
     * @return представление плагина
     */
    public PluginView getView() {
        return view;
    }

    /**
     * Устанавливает "владельца" (панель плагинов) данного плагина.
     *
     * @param owner панель плагинов, в которой данный плагин отображается
     */
    void setOwner(PluginPanel owner) {
        this.owner = owner;
    }

    /**
     * Возвращает конфигурацию плагина
     *
     * @return конфигурацию плагина
     */
    public PluginConfig getConfig() {
        return config;
    }

    /**
     * Устанавливает конфигурацию плагина
     *
     * @param config конфигурация плагина
     */
    public void setConfig(PluginConfig config) {
        this.config = config;
    }

    /**
     * Возвращает первичные данные плагина.
     *
     * @return первичные данные плагина
     */
    public <T extends PluginData> T getInitialData() {
        return (T) initialData;
    }

    /**
     * Устанавливает первичные данные плагина. Эти данные используются при инициализация плагина.
     *
     * @param initialData первичные данные данного плагина
     */
    public void setInitialData(PluginData initialData) {
        PluginData original = this.initialData;
        boolean initialDataChanged = original != null && initialData != original;
        this.initialData = initialData;
        this.initialDataAssigned = true;
        if (initialDataChanged) {
            getView().updateActionToolBar();
            afterInitialDataChange(original, initialData);
        }
    }

    public void setToolbarContext(ToolbarContext context) {
        ((ActivePluginData) this.initialData).setToolbarContext(context);
        getView().updateActionToolBar();
    }

    protected void afterInitialDataChange(PluginData oldData, PluginData newData) {
    }

    /**
     * @return TRUE - Обработка события восстановления данных истории завершена и дальше по цепочке обработка не
     * требуется, FALSE - событие восстановления данных истории должно быть передано дальше по цепочкею
     */
    public boolean restoreHistory() {
        return false;
    }

    public void fillHistoryData() {
    }

    /**
     * Определяет, требует ли плагин инициализации. Если нет, то при открытии плагина не будет совершаться вызов
     * серверных методов, ускоряя его открытие и снижая нагрузку на сервер.
     *
     * @return true, если плагин требует инициализации и false - в противном случае
     */
    protected boolean shouldBeInitialized() {
        return !initialDataAssigned;
    }

    private void postSetUp() {
        view = createView();
        if (viewCreatedEventListeners != null) {
            PluginViewCreatedEvent event = new PluginViewCreatedEvent(this);
            for (PluginViewCreatedEventListener listener : viewCreatedEventListeners) {
                listener.onViewCreation(event);
            }
            // just in case, make sure to produce NPE if view is "created" again. and free memory of course
            viewCreatedEventListeners = null;
        }

        // owner is not implemented as PluginViewCreatedListener as we want it to be notified after all
        owner.onPluginViewCreated(this);

    }

    public PluginPanel getOwner() {
        return owner;
    }

    public boolean displayActionToolBar() {
        return displayActionToolBar;
    }

    public void setDisplayActionToolBar(boolean displayActionToolBar) {
        this.displayActionToolBar = displayActionToolBar;
    }

    public boolean isDisplayInfobar() {
        return displayInfobar;
    }

    public void setDisplayInfobar(boolean displayInfobar) {
        this.displayInfobar = displayInfobar;
    }

    public void setNavigationConfig(NavigationConfig navigationConfig) {
        this.navigationConfig = navigationConfig;
    }

    public NavigationConfig getNavigationConfig() {
        return navigationConfig;
    }

    public boolean isShowBreadcrumbs() {
        return showBreadcrumbs;
    }

    public void setShowBreadcrumbs(boolean showBreadcrumbs) {
        this.showBreadcrumbs = showBreadcrumbs;
    }

    public boolean isDirty() {
        return false;
    }

    /**
     * Вызывается из действия Refresh. Должен быть переопределен для плагинов,
     * поддерживающих это действие.
     */
    public void refresh() {
    }

    public void notifyCloseListeners(){
        for (PluginCloseListener pluginCloseListener : pluginCloseListeners) {
            pluginCloseListener.onPluginClose();
        }
    }
}
