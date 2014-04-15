package ru.intertrust.cm.core.gui.api.client;

import com.google.gwt.user.client.Window;

/**
 * @author Sergey.Okolot
 *         Created on 07.04.2014 17:50.
 */
public class UrlManager {
    private static final UrlManager INSTANCE = new UrlManager();

    private UrlManager() {
    }

    public static UrlManager get() {
        return INSTANCE;
    }

    /**
     * Update url without reload page.
     * @param newUrlSettings - new values of url.
     */
    public void updateUrl(final String newUrlSettings) {
        final String newUrl = Window.Location.createUrlBuilder().setHash(newUrlSettings).buildString();
        Window.Location.replace(newUrl);
    }
}
