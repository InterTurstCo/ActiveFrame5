package ru.intertrust.cm.core.client;

import com.vaadin.server.*;

import javax.servlet.ServletException;

/**
 * @author Denis Mitavskiy
 *         Date: 05.07.13
 *         Time: 17:31
 */
public class DomainUniverseServlet extends VaadinServlet {
    @Override
    protected void servletInitialized() throws ServletException {
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
}
