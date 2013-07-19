package ru.intertrust.cm.core.gui.impl.client;

import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinService;
import com.vaadin.ui.*;
import ru.intertrust.cm.core.business.api.dto.UserUidWithPassword;
import ru.intertrust.cm.core.gui.api.server.GuiService;
import ru.intertrust.cm.core.gui.api.server.exception.AuthenticationException;
import ru.intertrust.cm.core.gui.impl.server.GuiServiceImpl;

/**
 * Страница, производящяя аутентификацию пользователей в диалоговом окне
 * @author Denis Mitavskiy
 *         Date: 10.07.13
 *         Time: 12:18
 */
public class LoginDialog extends UI implements Button.ClickListener {
    public static final String PATH = "/welcome";

    private Window window;
    private TextField loginField;
    private PasswordField passwordField;
    private Label message;

    @Override
    protected void init(VaadinRequest request) {
        message = new Label("");
        loginField = new TextField("Login");
        passwordField = new PasswordField("Password");

        Button loginButton = new Button("Login");
        loginButton.addClickListener(this);

        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.setMargin(true);
        dialogLayout.addComponent(message);
        dialogLayout.addComponent(loginField);
        dialogLayout.addComponent(passwordField);
        dialogLayout.addComponent(loginButton);

        window = new Window("Log in");
        window.setClosable(false);
        window.setModal(true);
        window.setResizable(false);
        window.setContent(dialogLayout);

        // Center it in the browser window
        window.center();

        setContent(new VerticalLayout()); // без этого контента окно диалога закроется по клику кнопки само
        addWindow(window);

        loginField.setValue("admin");
        passwordField.setValue("admin");

        buttonClick(null);
    }

    @Override
    /**
     * Обработчик события "Вход в систему"
     */
    public void buttonClick(Button.ClickEvent event) {
        String login = loginField.getValue();
        String password = passwordField.getValue();
        GuiService guiService = new GuiServiceImpl();
        try {
            guiService.login(new UserUidWithPassword(login, password));
            window.close();
            Page.getCurrent().setLocation(VaadinService.getCurrentRequest().getContextPath() + BusinessUniverse.PATH);
        } catch (AuthenticationException e) {
            message.setValue("No way!");
        }
    }
}
