package ru.intertrust.cm.core.gui.api.client;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import ru.intertrust.cm.core.gui.api.client.event.OpenHyperlinkInSurferEvent;
import ru.intertrust.cm.core.gui.api.client.event.OpenHyperlinkInSurferEventHandler;
import ru.intertrust.cm.core.gui.api.client.history.HistoryManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Утилитный класс, который обеспечивает доступ к "глобальным" объектам приложения
 * User: IPetrov
 * Date: 25.11.13
 * Time: 16:54
 */
public class Application {

    private static final int MILLIS_IN_SEC = 1000;

    private static Application ourInstance = null;

    private PopupPanel glassPopupPanel;

    private Timer indicateLockScreenTimer = new Timer() {
        @Override
        public void run() {
            showLockScreenIndicator();
        }
    };

    /*
     * Глобальная шина событий приложения
     */
    private final EventBus eventBus;
    private final CompactModeState compactModeState;
    private final HistoryManager historyManager;
    private ActionManager actionManager;
    private String pageNamePrefix;
    private List<String> timeZoneIds;
    private int collectionCountersUpdatePeriod = -1;
    private int headerNotificationPeriod = -1;
    private String version;
    private String currentLocale;
    private Map<String, String> localizedResources = new HashMap<>();
    private boolean inUploadProcess;
    private HandlerRegistration openDoInPluginHandlerRegistration;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    private Application() {
        // создаем шину сообщений
        eventBus = GWT.create(SimpleEventBus.class);
        compactModeState = new CompactModeState();
        historyManager = GWT.create(HistoryManager.class);
        glassPopupPanel = createGlassPopup();
    }

    /*
     * Метод получения экземпляра класса                                        ;
     */
    public static Application getInstance() {
        if (ourInstance == null) {
            ourInstance = new Application();
        }
        return ourInstance;
    }

    /*
     * Метод получения "глобальной" шины событий
     */
    public EventBus getEventBus() {
        return eventBus;
    }

    public CompactModeState getCompactModeState() {
        return compactModeState;
    }

    public HistoryManager getHistoryManager() {
        return historyManager;
    }

    public ActionManager getActionManager() {
        return actionManager;
    }

    public void setActionManager(ActionManager actionManager) {
        this.actionManager = actionManager;
    }

    public String getPageName(final String name) {
        return pageNamePrefix == null ? name : pageNamePrefix + name;
    }

    public void setPageNamePrefix(String pageNamePrefix) {
        this.pageNamePrefix = pageNamePrefix;
    }

    public List<String> getTimeZoneIds() {
        return timeZoneIds;
    }

    public void setTimeZoneIds(List<String> timeZoneIds) {
        this.timeZoneIds = timeZoneIds;
    }

    public int getCollectionCountersUpdatePeriod() {
        return collectionCountersUpdatePeriod;
    }

    public void setCollectionCountersUpdatePeriod(final Integer collectionCountersUpdatePeriod) {
        if (collectionCountersUpdatePeriod != null && collectionCountersUpdatePeriod > 0) {
            this.collectionCountersUpdatePeriod = collectionCountersUpdatePeriod * MILLIS_IN_SEC;
        }
    }

    public int getHeaderNotificationPeriod() {
        return headerNotificationPeriod;
    }

    public void setHeaderNotificationPeriod(final Integer headerNotificationPeriod) {
        if (headerNotificationPeriod != null && headerNotificationPeriod > 0) {
            this.headerNotificationPeriod = headerNotificationPeriod * MILLIS_IN_SEC;
        }
    }

    public String getCurrentLocale() {
        return currentLocale;
    }

    public void setCurrentLocale(String currentLocale) {
        this.currentLocale = currentLocale;
    }

    public Map<String, String> getLocalizedResources() {
        return localizedResources;
    }

    public void setLocalizedResources(Map<String, String> localizedResources) {
        this.localizedResources.clear();
        if (localizedResources != null) {
            this.localizedResources.putAll(localizedResources);
        }
    }

    /**
     * Блокирует экран для выполнения действий.<br/>
     * Если блокировка длится больше 1 секунды - она визуализируется (экран чуть затеняется, показывается индикатор загрузки)
     */
    public void lockScreen() {
        lockScreen(true, false);
    }

    /**
     * Блокирует экран для выполнения действий.<br/>
     *
     * @param indicate флаг индикации блокировки.<br/>
     *                 <li/>true - блокировка визуализируется через 1 секунду (экран чуть затеняется, показывается индикатор загрузки)<br/>
     *                 <li/>false - блокировка остается незаметной (прозрачный элемент поверх остальных)
     */
    public void lockScreen(final boolean indicate) {
        lockScreen(indicate, false);
    }

    /**
     * Блокирует экран для выполнения действий.<br/>
     *
     * @param indicate                 флаг индикации блокировки.<br/>
     *                                 <li/>true - блокировка визуализируется<br/>
     *                                 <li/>false - блокировка остается незаметной (прозрачный элемент поверх остальных)
     * @param showIndicatorImmediately нужно ли немедленно показывать индикатор блокировки,
     *                                 либо через секунду, для того, чтобы сделать ее незаметной для пользователя в случае, когда она будет быстро снята (т.е. в течении секунды)
     *                                 (игнорируется, если индикации блокировки нет (indicate == false) )
     */
    public void lockScreen(final boolean indicate, final boolean showIndicatorImmediately) {
        setGlassPopupTransparentState();

        if (indicate) {
            if (showIndicatorImmediately) {
                showLockScreenIndicator();
            } else {
                indicateLockScreenTimer.schedule(1 * MILLIS_IN_SEC);
            }
        }

        glassPopupPanel.show();
        glassPopupPanel.center();
    }

    public void unlockScreen() {
        indicateLockScreenTimer.cancel();
        RootPanel.get().getElement().getStyle().setCursor(Style.Cursor.DEFAULT);
        glassPopupPanel.hide();
    }

    private PopupPanel createGlassPopup() {
        final PopupPanel glassPopup = new PopupPanel();

        glassPopup.setModal(true);
        glassPopup.setGlassEnabled(true);

        return glassPopup;
    }

    /**
     * Установить для блокирующей действия пользователя панели прозрачный стиль:<br/>
     * внешний вид экрана никак не меняется, но доступ к контролам интерфейса отсутствует.
     */
    private void setGlassPopupTransparentState() {
        glassPopupPanel.setGlassStyleName("transparent-glass-no-wheel");
        glassPopupPanel.setStyleName(null);
    }

    /**
     * Установить для блокирующей действия пользователя панели стиль загрузки:<br/>
     * экран чуть затеняется, а по центру показывается круговой индикатор выполнения, доступ к контролам интерфейса отсутствует.
     */
    private void setGlassPopupWheelState() {
        glassPopupPanel.setGlassStyleName("transparentGlass");
        glassPopupPanel.setStyleName("PopupPanelPreloader");
    }

    /**
     * Устанавливает для блокирующей действия пользователя панели стиль загрузки,<br/>
     * меняет стиль курсора мыши на ожидающий выполнения.
     */
    private void showLockScreenIndicator() {
        RootPanel.get().getElement().getStyle().setCursor(Style.Cursor.WAIT);
        setGlassPopupWheelState();
    }

    public boolean isInUploadProcess() {
        return inUploadProcess;
    }

    public void setInUploadProcess(boolean inUploadProcess) {
        this.inUploadProcess = inUploadProcess;
    }

    public void addOpenDoInPluginHandlerRegistration(OpenHyperlinkInSurferEventHandler handler) {
        if (openDoInPluginHandlerRegistration != null) {
            openDoInPluginHandlerRegistration.removeHandler();
        }
        openDoInPluginHandlerRegistration = eventBus.addHandler(OpenHyperlinkInSurferEvent.TYPE, handler);
    }

}
