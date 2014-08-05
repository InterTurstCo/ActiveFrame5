package ru.intertrust.cm.core.gui.impl.authentication;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.storage.client.Storage;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import ru.intertrust.cm.core.business.api.dto.UserUidWithPassword;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseAuthenticationServiceAsync;
import ru.intertrust.cm.core.model.AuthenticationException;
import java.util.Date;

/**
 * @author Denis Mitavskiy
 *         Date: 25.07.13
 *         Time: 14:47
 */
@ComponentName("login.window")
public class LoginWindow implements Component {
    private DialogBox loginDialog;
    protected TextBox loginField;
    protected PasswordTextBox passwordField;
    private Label message;
    private FocusPanel loginButton;
    private AbsolutePanel rootPanel;
    private AbsolutePanel enterPanel;
    private AbsolutePanel loginAndPasswordPanel;

    public DialogBox getLoginDialog() {
        return loginDialog;
    }

    public void setLoginDialog(DialogBox loginDialog) {
        this.loginDialog = loginDialog;
    }

    public void show() {
        loginDialog.show();
    }

    public void center() {
        loginDialog.center();
    }

    public LoginWindow() {


        String version = "";
        loginDialog = new DialogBox();
        loginDialog.getElement().addClassName("auth-DialogBox");
        loginDialog.setHTML("<span class = 'auth_small_logo'> </span>" + "<span class = 'auth_version'>" + version + "</span>");

        //loginDialog.setText(" Аутентификация");
        loginField = new TextBox();
        loginField.getElement().setId("id_login");
        passwordField = new PasswordTextBox();
        //error message
        message = new Label();
        message.getElement().addClassName("auth-error-label");
        Label loginName = new Label("Имя пользователя");
        loginName.getElement().removeClassName(".gwt-Label");
        loginName.getElement().addClassName("auth-Label");
        Label passwordLabel = new Label("Пароль");
        passwordLabel.getElement().removeClassName(".gwt-Label");
        passwordLabel.getElement().addClassName("auth-Label");
        loginField.setWidth("140px");
        passwordField.setWidth("140px");

        loginButton = new FocusPanel();

        Label titleLogin = new Label("Войти");
        titleLogin.getElement().addClassName("auth_button_title");
        loginButton.add(titleLogin);
        loginButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                login();
            }
        });

        loginDialog.setAnimationEnabled(true);
        loginDialog.setGlassEnabled(true);

        rootPanel = new AbsolutePanel();
        rootPanel.setStyleName("auth_LoginPage_wrapper");
        AbsolutePanel decoratedContentPanel = new AbsolutePanel();
        decoratedContentPanel.setStyleName("auth_LoginPage_block");
        loginAndPasswordPanel = new AbsolutePanel();
        loginAndPasswordPanel.setStyleName("auth_wrapper");
        AbsolutePanel settingsPanel = new AbsolutePanel();
        settingsPanel.setStyleName("auth_add_settings");
        AbsolutePanel labelLoginPanel = new AbsolutePanel();
        labelLoginPanel.setStyleName("auth_login");
        AbsolutePanel labelPasswordPanel = new AbsolutePanel();
        labelPasswordPanel.setStyleName("auth_pass");
        AbsolutePanel memoryPanel = new AbsolutePanel();
        memoryPanel.setStyleName("auth_remember");

        Label labelCheckBox = new Label("Запомнить меня");
        labelCheckBox.setStyleName("auth_checkbox_title");
//        enterPanel = new AbsolutePanel();
//        enterPanel.setStyleName("dark-button");
        AbsolutePanel languagePanel = new AbsolutePanel();
        languagePanel.setStyleName("auth_language");

        rootPanel.add(decoratedContentPanel);
        decoratedContentPanel.add(message);

        decoratedContentPanel.add(loginAndPasswordPanel);
        decoratedContentPanel.add(settingsPanel);

        loginAndPasswordPanel.add(labelLoginPanel);
        loginAndPasswordPanel.add(labelPasswordPanel);
        loginAndPasswordPanel.add(memoryPanel);
        //loginAndPasswordPanel.add(enterPanel);
        loginAndPasswordPanel.add(languagePanel);

        labelLoginPanel.add(loginName);
        labelLoginPanel.add(loginField);
        loginField.getElement().setId("focus_field");
        labelPasswordPanel.add(passwordLabel);
        labelPasswordPanel.add(passwordField);
        CheckBox memoryCheckbox = new CheckBox();
        memoryCheckbox.getElement().addClassName("auth-CheckBox");
        memoryPanel.add(memoryCheckbox);

        memoryPanel.add(labelCheckBox);



        loginDialog.add(rootPanel);

        loginDialog.addDomHandler(new KeyDownHandler() {
            @Override
            public void onKeyDown(KeyDownEvent event) {
                checkEnterKey(event);
            }
        }, KeyDownEvent.getType());


        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                loginField.getElement().focus();
            }
        });
        final Timer timer = new Timer() {
            @Override
            public void run() {
                try {
                    final String activeElement = getActiveElement();
                    if (activeElement.equals("focus_field")) {
                        this.cancel();
                    } else {
                        loginField.getElement().focus();
                    }
                } catch (Throwable throwable) {
                    this.cancel();
                }
            }
        };
        timer.scheduleRepeating(100);
    }

    public static native String getActiveElement() /*-{
        return $doc.activeElement.id;
    }-*/;

    protected void checkEnterKey(KeyDownEvent event) {
        if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
            login();
        }
    }

    private String getQueryStringWithLocalization() {
        StringBuilder queryParam = new StringBuilder(Window.Location.getQueryString());
        if (queryParam.length() == 0) {
            queryParam.append("?locale=ru");
        } else if (!queryParam.toString().contains("locale")) {
            queryParam.append("&locale=ru");
        }
        return queryParam.toString();
    }

    protected void login() {

        AsyncCallback<Void> callback = new AsyncCallback<Void>() {


            @Override
            public void onSuccess(Void result) {
                //todo: переадресация должна уметь задаваться на стадии открытия LoginPage. пока хардкод

                String path = GWT.getHostPageBaseURL();
                Window.Location.replace(path + "BusinessUniverse.html" + getQueryStringWithLocalization());

            }

            @Override
            public void onFailure(Throwable caught) {
                if (caught instanceof AuthenticationException) {
                    message.setText("Ошибка авторизации. Проверте правильность введенных данных.");
                } else {
                    message.setText("No way. " + caught.getMessage());
                }
            }
        };
        final Date date = new Date();
        final String timezone = DateTimeFormat.getFormat("vvvv").format(date);
        final UserUidWithPassword credentials =
                new UserUidWithPassword(loginField.getText(), passwordField.getText(), timezone);
        BusinessUniverseAuthenticationServiceAsync.Impl.getInstance().login(credentials, callback);
    }

    public void addClearUserSettingsButton() {
        FocusPanel clearUserSettingsButton = new FocusPanel();
        clearUserSettingsButton.setStyleName("light-button");
        clearUserSettingsButton.addStyleName("clearUserSettings");
        Label titleClearUserSettings = new Label("Очистить настройки");
        titleClearUserSettings.getElement().addClassName("auth_button_title");
        clearUserSettingsButton.add(titleClearUserSettings);

        clearUserSettingsButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                Storage storage = Storage.getLocalStorageIfSupported();
                if (storage == null) {
                    return;
                }
                storage.clear();

            }
        });
        //rootPanel.add(clearUserSettingsButton);


        loginAndPasswordPanel.add(clearUserSettingsButton);
        enterPanel = new AbsolutePanel();
        enterPanel.setStyleName("dark-button");

        loginAndPasswordPanel.add(enterPanel);
        enterPanel.add(loginButton);
    }

    @Override
    public String getName() {
        return "login.window";
    }

    @Override
    public Component createNew() {
        return new LoginWindow();
    }

    public TextBox getLoginField() {
        return loginField;
    }

    public void setLoginField(TextBox loginField) {
        this.loginField = loginField;
    }

    public PasswordTextBox getPasswordField() {
        return passwordField;
    }

    public void setPasswordField(PasswordTextBox passwordField) {
        this.passwordField = passwordField;
    }
}
