package ru.intertrust.cm.core.gui.api.server;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by Ravil on 16.10.2017.
 */
public interface HttpRequestFilterUser {
    interface Remote extends HttpRequestFilterUser {
    }
    String getUserName(HttpServletRequest request);
}
