package ru.intertrust.cm.core.gui.impl.authentication;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.storage.client.Storage;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.StatusCodeException;
import com.google.gwt.user.client.ui.*;
import ru.intertrust.cm.core.business.api.dto.UserUidWithPassword;
import ru.intertrust.cm.core.config.LoginScreenConfig;
import ru.intertrust.cm.core.config.ProductTitleConfig;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.api.client.History;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.LoginWindowInitialization;
import ru.intertrust.cm.core.gui.model.VersionInfo;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseAuthenticationServiceAsync;
import ru.intertrust.cm.core.model.AuthenticationException;

import java.util.List;
import java.util.Map;

import static ru.intertrust.cm.core.config.localization.LocalizationKeys.*;

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
    private AbsolutePanel versionPanel;
    private FormPanel formPanel;
    private String coreVersion = "";
    private String productVersion = "";
    private String textApplicationLogo = "";
    private String authSmallLogo = "auth_small_logo";
    private String coreVersionPrefix = "";
    private String productVersionPrefix = "";
    private String initialToken;
    private Map<String, String> localizedResources;
    private Label loginName;
    private Label passwordLabel;
    private Label titleLogin;
    private Label labelCheckBox;
    private Label titleClearUserSettings;
    private List<VersionInfo> productVersionList;

    public String getVersion() {
        return coreVersion;
    }

    public void setVersion(String coreVersion) {
        this.coreVersion = coreVersion;
    }

    public void show() {
        loginDialog.show();
    }

    public void center() {
        loginDialog.center();
    }

    public LoginWindow() {
        formPanel = new FormPanel();

        initialToken = History.getToken();
        loginDialog = new DialogBox();
        loginDialog.getElement().addClassName("auth-DialogBox");
        loginField = new TextBox();
        loginField.getElement().setId("id_login");
        passwordField = new PasswordTextBox();
        //error message
        message = new Label();
        message.getElement().addClassName("auth-error-label");
        loginName = new Label();
        loginName.getElement().removeClassName(".gwt-Label");
        loginName.getElement().addClassName("auth-Label");
        passwordLabel = new Label();
        passwordLabel.getElement().removeClassName(".gwt-Label");
        passwordLabel.getElement().addClassName("auth-Label");
        loginField.setWidth("140px");
        passwordField.setWidth("140px");
        passwordField.setName("username");
        loginButton = new FocusPanel();

        titleLogin = new Label();
        titleLogin.getElement().addClassName("auth_button_title");
        loginButton.add(titleLogin);

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

        labelCheckBox = new Label();
        labelCheckBox.setStyleName("auth_checkbox_title");
        AbsolutePanel languagePanel = new AbsolutePanel();
        languagePanel.setStyleName("auth_language");
        rootPanel.add(decoratedContentPanel);
        decoratedContentPanel.add(message);

        decoratedContentPanel.add(loginAndPasswordPanel);
        decoratedContentPanel.add(settingsPanel);

        loginAndPasswordPanel.add(labelLoginPanel);
        loginAndPasswordPanel.add(labelPasswordPanel);
        loginAndPasswordPanel.add(memoryPanel);
        loginAndPasswordPanel.add(languagePanel);
        //loginAndPasswordPanel.add(new SubmitButton("GO"));

        labelLoginPanel.add(loginName);
        labelLoginPanel.add(loginField);
        loginField.getElement().setId("focus_field");
        labelPasswordPanel.add(passwordLabel);
        labelPasswordPanel.add(passwordField);
        CheckBox memoryCheckbox = new CheckBox();
        memoryCheckbox.getElement().addClassName("auth-CheckBox");
        memoryPanel.add(memoryCheckbox);
        memoryPanel.add(labelCheckBox);

        formPanel.setWidget(rootPanel);
        formPanel.addSubmitHandler(new FormPanel.SubmitHandler() {
            @Override
            public void onSubmit(FormPanel.SubmitEvent submitEvent) {
                login();
            }
        });

        loginDialog.add(formPanel);

        versionPanel = new AbsolutePanel();
        versionPanel.setStyleName("versionPanel");


        loginDialog.addDomHandler(new KeyDownHandler() {
            @Override
            public void onKeyDown(KeyDownEvent event) {
                checkEnterKey(event);
            }
        }, KeyDownEvent.getType());
        enterPanel = new AbsolutePanel();
        enterPanel.setStyleName("darkButton");

        loginAndPasswordPanel.add(enterPanel);
        enterPanel.add(loginButton);
        enterPanel.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                login();
            }
        }, ClickEvent.getType());

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

    public void getGlobalAndLoginWindowConfiguration() {

        AsyncCallback<LoginWindowInitialization> callback = new AsyncCallback<LoginWindowInitialization>() {

            @Override
            public void onSuccess(LoginWindowInitialization loginWindowInitialization) {
                localizedResources = loginWindowInitialization.getLocalizedResources();
                if (loginWindowInitialization.getLoginScreenConfig() != null) {
                    LoginScreenConfig logoConfig = loginWindowInitialization.getLoginScreenConfig();
                    ProductTitleConfig productTitleConfig = logoConfig.getProductTitleConfig();

                    if (logoConfig.isDisplaycoreVersion()) {
                        coreVersion = loginWindowInitialization.getVersion();
                        coreVersionPrefix = get(CORE_VERSION_KEY, "Версия платформы: ");
                    }

                    if (logoConfig.isDisplayProductVersion() && loginWindowInitialization.getProductVersion() != null) {
                        productVersion = loginWindowInitialization.getProductVersion();
                        productVersionPrefix = get(VERSION_KEY, "Версия: ");
                    }

                    if (logoConfig.isDisplayVersionList()) {
                        productVersionList = loginWindowInitialization.getProductVersionList();
                    }                    
                    
                    if (productTitleConfig.getStyle().equals("text")) {
                        if (loginWindowInitialization.getGlobalProductTitle() != null) {
                            textApplicationLogo = loginWindowInitialization.getGlobalProductTitle().getTitle();
                            authSmallLogo = "";
                        }

                    }

                    if (productTitleConfig.getStyle().equals("image")) {
                        if (loginWindowInitialization.getGlobalProductTitle() != null) {
                            authSmallLogo = "<img src=" + productTitleConfig.getImage() + " />";
                            textApplicationLogo = "";
                        }
                    }

                }
                loginDialog.setHTML("<div class='loginLogo'>" + authSmallLogo + textApplicationLogo + " </div>");


                if (productVersion != null) {
                    Element divPlatformVersion = DOM.createDiv();
                    divPlatformVersion.setClassName("versionPlatform");
                    divPlatformVersion.setInnerHTML(productVersionPrefix + "<span>" + productVersion + "</span>");
                    loginDialog.getElement().getChild(0).getLastChild().getLastChild().getLastChild().getChild(1).getLastChild().insertFirst(divPlatformVersion);
                }

                if (coreVersion != null){
                    Element divCoreVersion = DOM.createDiv();
                    divCoreVersion.setClassName("versionCore");
                    divCoreVersion.setInnerHTML(coreVersionPrefix + "<span>" + coreVersion + "</span>");
                    loginDialog.getElement().getChild(0).getLastChild().getLastChild().getLastChild().getChild(1).getLastChild().insertFirst(divCoreVersion);
                }

                if (productVersionList != null){
                    for (VersionInfo productVersion : productVersionList) {
                        Element divVersion = DOM.createDiv();
                        divVersion.setClassName("versionCore");
                        divVersion.setInnerHTML(productVersion.getComponent() + ": <span>" + productVersion.getVersion() + "</span>");
                        loginDialog.getElement().getChild(0).getLastChild().getLastChild().getLastChild().getChild(1).getLastChild().insertFirst(divVersion);
                    }
                }

                loginName.setText(get(USER_NAME_KEY, "Имя пользователя"));
                passwordLabel.setText(get(PASSWORD_KEY, "Пароль"));
                titleLogin.setText(get(SIGN_ON_KEY, "Войти"));
                labelCheckBox.setText(get(REMEMBER_ME_KEY, "Запомнить меня"));
                if (titleClearUserSettings != null) {
                    titleClearUserSettings.setText(get(RESET_SETTINGS_KEY, "Очистить настройки"));
                }
            }

            @Override
            public void onFailure(Throwable throwable) {
                throwable.printStackTrace();
            }
        };
        BusinessUniverseAuthenticationServiceAsync.Impl.getInstance().getLoginWindowInitialization(callback);
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
        StringBuilder queryParam = new StringBuilder();
        Map<String, List<String>> parameterMap = Window.Location.getParameterMap();

        /*if (!parameterMap.containsKey("locale")) {
            queryParam.append("locale=ru");
        }*/
        for (String paramName : parameterMap.keySet()) {
            if (!"targetPage".equals(paramName)) {
                List<String> paramValues = parameterMap.get(paramName);
                for (String value : paramValues) {
                    if (queryParam.length() > 0) {
                        queryParam.append("&");
                    }
                    queryParam.append(paramName).append("=").append(value);
                }
            }
        }
        if (queryParam.length() > 0) {
            queryParam.insert(0, "?");
        }
        return queryParam.toString();
    }

    protected void login() {

        AsyncCallback<Void> callback = new AsyncCallback<Void>() {


            @Override
            public void onSuccess(Void result) {
                String targetPage = Window.Location.getParameter("targetPage");
                if (targetPage == null || targetPage.isEmpty()) {
                    targetPage = "BusinessUniverse.html";
                }
                final StringBuilder pathBuilder = new StringBuilder(GWT.getHostPageBaseURL())
                        .append(targetPage).append(getQueryStringWithLocalization());
                if (initialToken != null && !initialToken.isEmpty()) {
                    pathBuilder.append('#').append(initialToken);
                }
                Window.Location.replace(pathBuilder.toString());
            }

            @Override
            public void onFailure(Throwable caught) {
                if (caught instanceof AuthenticationException) {
                    message.setText(get(AUTHORIZATION_WRONG_PSW_ERROR_KEY, "Ошибка авторизации. Проверьте правильность введенных данных."));
                } else if (caught instanceof StatusCodeException) {
                    message.setText(get(AUTHORIZATION_CONNECTION_ERROR_KEY, "Ошибка авторизации. Невозможно подключиться к серверу."));
                } else {
                    message.setText(get(AUTHORIZATION_ERROR_KEY, "Ошибка авторизации. ") + caught.getMessage());
                }
            }
        };
        final UserUidWithPassword credentials = new UserUidWithPassword(loginField.getText(), passwordField.getText());
        BusinessUniverseAuthenticationServiceAsync.Impl.getInstance().login(credentials, callback);
    }

    public void addClearUserSettingsButton() {
        FocusPanel clearUserSettingsButton = new FocusPanel();
        clearUserSettingsButton.setStyleName("lightButton");
        clearUserSettingsButton.addStyleName("clearUserSettings");
        titleClearUserSettings = new Label("Очистить настройки");
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


        loginAndPasswordPanel.add(clearUserSettingsButton);

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

    public String get(String key, String defaultValue) {
        if (localizedResources == null) {
            return defaultValue;
        }
        String value = localizedResources.get(key);
        return value != null ? value : defaultValue;
    }

}
