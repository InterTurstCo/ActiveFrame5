package ru.intertrust.cm.core.gui.model;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.LoginScreenConfig;

/**
 * Created by tbilyi on 11.08.2014.
 */
public class LoginWindowInitialization implements Dto{
    private String version;
    private LoginScreenConfig loginScreenConfig;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        if(version == null){
            this.version = "";
        }
        else{
            this.version = version;
        }

    }

    public LoginScreenConfig getLoginScreenConfig() {
        return loginScreenConfig;
    }

    public void setLoginScreenConfig(LoginScreenConfig loginScreenConfig) {
        this.loginScreenConfig = loginScreenConfig;
    }

    public LoginWindowInitialization() {
    }

}
