package ru.intertrust.cm.core.gui.impl.client.temp.vaadin;

import com.vaadin.server.*;

import javax.servlet.ServletException;

/**
 * Сервлет, обрабатывающий запросы приложения "Бизнес-вселенная"
 * @author Denis Mitavskiy
 *         Date: 05.07.13
 *         Time: 17:31
 */
public class BusinessUniverseServlet extends VaadinServlet {
    @Override
    protected void servletInitialized() throws ServletException {
        super.servletInitialized();
        getService().setSystemMessagesProvider(
                new SystemMessagesProvider() {
                    @Override
                    public SystemMessages getSystemMessages(SystemMessagesInfo systemMessagesInfo) {
                        CustomizedSystemMessages messages = new CustomizedSystemMessages();
                        messages.setSessionExpiredCaption("Bye-bye, session is expired");
                        messages.setSessionExpiredMessage("Session Expired. <u>Нажми сюда</u>");
                        messages.setSessionExpiredNotificationEnabled(true);
                        messages.setSessionExpiredURL("http://google.com");

                        messages.setCommunicationErrorCaption("Comm Err");
                        messages.setCommunicationErrorMessage("This is bad.");
                        messages.setCommunicationErrorNotificationEnabled(true);
                        messages.setCommunicationErrorURL("http://vaadin.com/");
                        return messages;
                    }
                }
        );
    }

    @Override
    protected VaadinServletService createServletService(DeploymentConfiguration deploymentConfiguration)
            throws ServiceException {
        VaadinServletService service = new BusinessUniverseVaadinServletService(this, deploymentConfiguration);
        service.init();
        return service;
    }


}
