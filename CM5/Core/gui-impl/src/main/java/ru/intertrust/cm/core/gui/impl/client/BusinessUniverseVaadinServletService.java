package ru.intertrust.cm.core.gui.impl.client;

import com.vaadin.server.*;
import com.vaadin.shared.JsonConstants;

import java.io.IOException;

/**
 * Сервис, добавляющий дополнительную функциональность стандартному сервису Vaadin.
 * Переопределяет поведение в случае экспирации сессии, переадресуя пользователя на страницу аутентификации без
 * предупредительного сообщения.
 * @author Denis Mitavskiy
 *         Date: 09.07.13
 *         Time: 17:26
 */
public class BusinessUniverseVaadinServletService extends VaadinServletService {
    public BusinessUniverseVaadinServletService(VaadinServlet servlet, DeploymentConfiguration deploymentConfiguration)
            throws ServiceException {
        super(servlet, deploymentConfiguration);
    }

    @Override
    protected void handleSessionExpired(VaadinRequest request, VaadinResponse response) throws ServiceException {
        String redirectUrl = BusinessUniverse.PATH;
        try {
            writeStringResponse(response, JsonConstants.JSON_CONTENT_TYPE,
                    "for(;;);[{\"redirect\":{\"url\":\"" + redirectUrl + "\"}}]");
        } catch (IOException e) {
            throw new ServiceException("Handling of session expired failed", e);
        }
    }


}
