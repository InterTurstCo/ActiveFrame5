package ru.intertrust.cm.core.client;

import com.vaadin.annotations.Title;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinService;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import ru.intertrust.cm.core.config.model.Configuration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.StringWriter;

/**
 * @author Denis Mitavskiy
 *         Date: 5/1/13
 *         Time: 6:43 PM
 */
@Title("hello")
public class Hello extends UI {
    protected boolean isLoggedIn(VaadinRequest request) {
        return request.getWrappedSession().getAttribute("USER_LOGIN") != null;
    }

    @Override
    protected void init(VaadinRequest request) {
        request.getWrappedSession();
        final HttpServletRequest currentRequest = (HttpServletRequest) VaadinService.getCurrentRequest();
        try {
            currentRequest.login("admin", "admin");
        } catch (ServletException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        HorizontalSplitPanel splitPanel = new HorizontalSplitPanel();
        setContent(splitPanel);

        VerticalLayout leftLayout = new VerticalLayout();
        leftLayout.addComponent(new Label("Welcome Home!", ContentMode.PREFORMATTED));
        splitPanel.addComponent(leftLayout);

        HorizontalLayout bottomLeftLayout = new HorizontalLayout();
        leftLayout.addComponent(bottomLeftLayout);


        /* Set the contents in the left of the split panel to use all the space */
        leftLayout.setSizeFull();

        bottomLeftLayout.setWidth("100%");

        Window subWindow = new Window("Log in");
        subWindow.setClosable(false);
        VerticalLayout subContent = new VerticalLayout();
        subContent.setMargin(true);
        subWindow.setContent(subContent);

        // Put some components in it
        //subContent.addComponent(new Label("Login"));
        final Label message = new Label("");
        final TextField loginField = new TextField("Login");
        final PasswordField passwordField = new PasswordField("Password");
        Button loginButton = new Button("Login");
        loginButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                String login = loginField.getValue();
                String password = passwordField.getValue();
                try {
                    currentRequest.login(login, password);
                    message.setValue("Success!");
                } catch (ServletException e) {
                    message.setValue(e.getMessage());
                }
            }
        });
        subContent.addComponent(message);
        subContent.addComponent(loginField);
        subContent.addComponent(passwordField);
        subContent.addComponent(loginButton);

        // Center it in the browser window
        subWindow.center();

        // Open it in the UI
        addWindow(subWindow);

        try {
            currentRequest.logout();
        } catch (ServletException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public static String getConfigXml() {
        Serializer serializer = new Persister();
        File source = new File("c:\\projects\\CM5_workspace\\CM5\\Core\\src\\main\\resources\\config\\business-objects.xml");

        try {
            Configuration example = serializer.read(Configuration.class, source);
            StringWriter stringWriter = new StringWriter();
            serializer.write(example, stringWriter);
            return stringWriter.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
