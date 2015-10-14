package ru.intertrust.cm.core.gui.api.server.extension;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ru.intertrust.cm.core.business.api.dto.UserCredentials;
import ru.intertrust.cm.core.dao.api.extension.ExtensionPointHandler;

public interface AuthenticationExtentionHandler extends ExtensionPointHandler {

    void onBeforeAuthentication(HttpServletRequest request, HttpServletResponse response);

    void onAfterAuthentication(HttpServletRequest request, HttpServletResponse response, UserCredentials authInfo);

    void onAfterLogout(HttpServletRequest request, HttpServletResponse response);
}
