package ru.intertrust.cm.core.gui.model;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.*;

/**
 * Created by tbilyi on 11.08.2014.
 */
public class LoginWindowInitialization implements Dto{
    private String version;
    private String productVersion;
    private LoginScreenConfig loginScreenConfig;
    private ProductTitle globalProductTitle;
    private ProductVersion globalProductVersion;

    public ProductTitle getGlobalProductTitle() {
        return globalProductTitle;
    }

    public void setGlobalProductTitle(ProductTitle globalProductTitle) {
        this.globalProductTitle = globalProductTitle;
    }

    public ProductVersion getGlobalProductVersion() {
        return globalProductVersion;
    }

    public void setGlobalProductVersion(ProductVersion globalProductVersion) {
        this.globalProductVersion = globalProductVersion;
    }

    public String getVersion() {
        return version;
    }

    public String getProductVersion() {
        return productVersion;
    }

    public void setProductVersion(String productVersion) {
        if(productVersion == null){
            this.productVersion = "";
        }
        else{
            this.productVersion = productVersion;
        }
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
