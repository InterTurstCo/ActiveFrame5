package ru.intertrust.cm.core.gui.api.client;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.web.bindery.event.shared.EventBus;
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

    private Timer timer = new Timer() {
        @Override
        public void run() {
            RootPanel.get().getElement().getStyle().setCursor(Style.Cursor.WAIT);
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

    public void showLoadingIndicator() {
        showLoadingIndicator(true);
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

    public void showLoadingIndicator(boolean lockScreenImmediately) {
        if (lockScreenImmediately) {
            glassPopupPanel.show();
            glassPopupPanel.center();
        }
        timer.schedule(1000);
    }

    public void hideLoadingIndicator() {
        timer.cancel();
        RootPanel.get().getElement().getStyle().setCursor(Style.Cursor.DEFAULT);
        glassPopupPanel.hide();
    }

    private PopupPanel createGlassPopup() {
        final PopupPanel glassPopup = new PopupPanel();
        glassPopup.setModal(true);
        glassPopup.setGlassEnabled(true);
        glassPopup.setGlassStyleName("transparentGlass");
        glassPopup.setStyleName("PopupPanelPreloader");
        return glassPopup;
    }

    public boolean isInUploadProcess() {
        return inUploadProcess;
    }

    public void setInUploadProcess(boolean inUploadProcess) {
        this.inUploadProcess = inUploadProcess;
    }
}
