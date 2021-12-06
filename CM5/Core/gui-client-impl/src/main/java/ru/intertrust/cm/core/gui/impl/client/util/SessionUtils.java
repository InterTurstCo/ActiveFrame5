package ru.intertrust.cm.core.gui.impl.client.util;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.gui.impl.client.form.widget.datebox.TimeUtil;
import ru.intertrust.cm.core.gui.impl.client.model.util.session.MakeServerSessionDirtyRequestDto;
import ru.intertrust.cm.core.gui.impl.client.model.util.session.SessionTimeoutRequestDto;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.session.SessionTimeoutResponseDto;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

import java.util.Date;

import static ru.intertrust.cm.core.gui.impl.client.plugins.navigation.NavigationTreePluginView.getLastActivity;

/**
 * Набор утилит для работы с сессией.<br>
 * <br>
 * Created by Myskin Sergey on 24.07.2020.
 */
public class SessionUtils {

    private SessionUtils() {
    }

    private static Timer checkSessionTimer = null;

    /**
     * Запускает все таймеры.
     */
    public static void startSessionSchedulers() {
        startSessionTimeoutScheduler();
        initAndStartDirtySessionTimer();
    }

    /**
     * Запускает клиентский таймер для проверки времени простоя.<br>
     * Время простоя до разлогина берется один раз перед созданием и стартом таймера из параметра 'session.client.timeout' в server.properties<br>
     * Если таймер до того существовал - он будет сначала остановлен, новый создается в любом случае.
     */
    private static void startSessionTimeoutScheduler() {
        Command command = new Command();

        command.setComponentName("session.utils.component");
        command.setName("getSessionTimeout");
        command.setParameter(new SessionTimeoutRequestDto());

        BusinessUniverseServiceAsync.Impl.executeCommand(command, new AsyncCallback<Dto>() {
            @Override
            public void onSuccess(Dto result) {
                SessionTimeoutResponseDto resultDto = (SessionTimeoutResponseDto) result;
                final Integer sessionTimeout = resultDto.getSessionTimeout();
                startSessionTimer(sessionTimeout);
            }

            @Override
            public void onFailure(Throwable caught) {
                GWT.log("something was going wrong while obtaining session data");
                caught.printStackTrace();
            }
        });
    }

    /**
     * Запускает клиентский таймер для проверки времени простоя.<br>
     * Если таймер до того существовал - он будет сначала остановлен, новый создается в любом случае.
     * Запуск таймера производится только, если время простоя, переданное в параметре не null и больше нуля.
     *
     * @param sessionTimeout время простоя до разлогина в минутах
     */
    private static void startSessionTimer(final Integer sessionTimeout) {
        stopSessionTimer();
        if ((sessionTimeout != null) && (sessionTimeout > 0)) {
            initSessionTimer(sessionTimeout);
            checkSessionTimer.scheduleRepeating(TimeUtil.SECONDS_IN_MINUTE * TimeUtil.MILLIS_IN_SEC);
        }
    }

    /**
     * Останавливает таймер проверки сессии, если он существует (!= null)
     */
    private static void stopSessionTimer() {
        if (checkSessionTimer != null) {
            checkSessionTimer.cancel();
        }
    }

    /**
     * Инициализирует (создает) таймер сессии с заданным в аргументе.<br>
     * По истечению этого таймера производится принудительный разлогин.
     *
     * @param sessionTimeout таймаут клиентской сессии в минутах
     */
    private static void initSessionTimer(final Integer sessionTimeout) {
        checkSessionTimer = new Timer() {
            @Override
            public void run() {
                Date now = new Date();
                final double lastActivity = getLastActivity();

                if (lastActivity != 0) {
                    if (((now.getTime() - lastActivity) / TimeUtil.MILLIS_IN_SEC) > (sessionTimeout * TimeUtil.SECONDS_IN_MINUTE)) {
                        AuthenticationUtils.logout();
                    }
                }
            }
        };
    }

    /**
     * Инициализирует (создает) и запускает таймер, который раз в минуту будет делать серверный запрос.<br>
     * Нужно это для того, чтобы таймаут сессии на сервере никогда не наступал,
     * а управление временем простоя до разлогина осуществлялось на клиенте.
     */
    private static void initAndStartDirtySessionTimer() {
        Timer dirtyServerSessionTimer = new Timer() {
            @Override
            public void run() {
                Command command = new Command();

                command.setComponentName("session.utils.component");
                command.setName("makeServerSessionDirty");
                command.setParameter(new MakeServerSessionDirtyRequestDto());

                BusinessUniverseServiceAsync.Impl.executeCommand(command, new AsyncCallback<Dto>() {

                    @Override
                    public void onSuccess(Dto result) {
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                        GWT.log("something was going wrong while making dirty server session");
                        caught.printStackTrace();
                    }
                });
            }
        };
        dirtyServerSessionTimer.scheduleRepeating(TimeUtil.SECONDS_IN_MINUTE * TimeUtil.MILLIS_IN_SEC);
    }

}
