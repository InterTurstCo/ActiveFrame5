package ru.intertrust.cm.core.gui.model;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.LoginScreenConfig;
import ru.intertrust.cm.core.config.ProductTitle;
import ru.intertrust.cm.core.config.ProductVersion;

import java.util.List;
import java.util.Map;

/**
 * Created by tbilyi on 11.08.2014.
 */
public class LoginWindowInitialization implements Dto{
    private String version;
    private String productVersion;
    private LoginScreenConfig loginScreenConfig;
    private ProductTitle globalProductTitle;
    private ProductVersion globalProductVersion;
    private Map<String, String> localizedResources;
    private List<VersionInfo> productVersionList;

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

    public Map<String, String> getLocalizedResources() {
        return localizedResources;
    }

    public void setLocalizedResources(Map<String, String> localizedResources) {
        this.localizedResources = localizedResources;
    }

    public LoginWindowInitialization() {
    }

    public List<VersionInfo> getProductVersionList() {
        return productVersionList;
    }

    public void setProductVersionList(List<VersionInfo> productVersionList) {
        this.productVersionList = productVersionList;
    }

}
