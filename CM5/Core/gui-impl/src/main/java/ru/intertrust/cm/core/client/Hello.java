package ru.intertrust.cm.core.client;

import com.vaadin.annotations.PreserveOnRefresh;
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
import java.util.concurrent.TimeUnit;

/**
 * @author Denis Mitavskiy
 *         Date: 5/1/13
 *         Time: 6:43 PM
 */
@Title("hello")
@PreserveOnRefresh
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
        try {
            currentRequest.logout();
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
                HttpServletRequest req = (HttpServletRequest) VaadinService.getCurrentRequest();
                try {
                    req.login(login, password);
                    message.setValue("Success!");
                } catch (ServletException e) {
                    e.printStackTrace();
                    message.setValue(e.getMessage());
                } finally {
                    try {
                        req.logout();
                    } catch (ServletException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
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
        /*setPollInterval((int) TimeUnit.SECONDS.toMillis(1));

        class Loader implements Runnable {

            @Override
            public void run() {
                while (true ) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }

                    // Wrap UI updates in access to properly deal with locking
                    System.out.println("Come on!");
                    access(new Runnable() {
                        @Override
                        public void run() {
                            Window subWindow = new Window("Hallo!");
                            subWindow.setClosable(true);
                            addWindow(subWindow);
                            setPollInterval((int) TimeUnit.SECONDS.toMillis(1));
                            //subWindow.close();
                        }
                    });
                }
            }
        }
        new Thread(new Loader()).start();*/
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
