package ru.intertrust.cm.core.gui.api.client;

/**
 * @author Ravil Abdulkhairov
 * @version 1.0
 * @since 24.09.2015
 */
public class History extends com.google.gwt.user.client.History {
    private static String application;

    public static String getApplication() {
        return application;
    }

    public static void setApplication(String application) {
        History.application = application;
    }
}
