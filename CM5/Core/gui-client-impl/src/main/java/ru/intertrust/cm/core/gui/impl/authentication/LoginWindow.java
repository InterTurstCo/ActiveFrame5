package ru.intertrust.cm.core.gui.impl.authentication;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import ru.intertrust.cm.core.business.api.dto.UserUidWithPassword;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseAuthenticationServiceAsync;

/**
 * @author Denis Mitavskiy
 *         Date: 25.07.13
 *         Time: 14:47
 */
@ComponentName("login.window")
public class LoginWindow extends DialogBox {
    private TextBox loginField;
    private PasswordTextBox passwordField;
    private Label message;
    private Button loginButton;

    public LoginWindow() {
        loginField = new TextBox();
        passwordField = new PasswordTextBox();
        message = new Label();
        loginButton = new Button("Войти");
        loginButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                login();
            }
        });

        setText("Аутентификация");
        setAnimationEnabled(true);
        setGlassEnabled(true);

        VerticalPanel dialogPanel = new VerticalPanel();
        dialogPanel.add(message);
        dialogPanel.add(new Label("Имя пользователя"));
        dialogPanel.add(loginField);
        dialogPanel.add(new Label("Пароль"));
        dialogPanel.add(passwordField);
        dialogPanel.add(loginButton);


        setWidget(dialogPanel);
    }

    private void login() {
        AsyncCallback<Void> callback = new AsyncCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                //todo: переадресация должна уметь задаваться на стадии открытия LoginPage. пока хардкод
                Window.Location.assign("/cm-sochi/BusinessUniverse.html" + Window.Location.getQueryString());
            }

            @Override
            public void onFailure(Throwable caught) {
                message.setText("No way. " + caught.getMessage() );
            }
        };
        UserUidWithPassword credentials = new UserUidWithPassword(loginField.getText(), passwordField.getText());
        BusinessUniverseAuthenticationServiceAsync.Impl.getInstance().login(credentials, callback);
    }
}
