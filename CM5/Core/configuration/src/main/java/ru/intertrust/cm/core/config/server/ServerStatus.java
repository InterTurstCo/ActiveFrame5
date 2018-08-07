package ru.intertrust.cm.core.config.server;

/**
 * Класс, определяющий доступность сервисов AF5
 * @author larin
 *
 */
public class ServerStatus {
    public static final String DISABLE_AF5_PARAM = "disable.af5";

    /**
     * Метод возвращает true если af5 активно, иначе false
     * @return
     */
    public static boolean isEnable() {
        String disableAf5 = System.getProperty(DISABLE_AF5_PARAM);
        return disableAf5 == null || !Boolean.parseBoolean(disableAf5);        
    }
}
