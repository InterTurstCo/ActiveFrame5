package ru.intertrust.cm.core.gui.impl.client;

/**
 * Created by tbilyi on 12.08.2014.
 */
public class CurrentVersionInfo {
    private String coreVersion = null;
    private String productVersion = null;

    public String getCoreVersion() {
        return coreVersion;
    }

    public void setCoreVersion(String coreVersion) {
            this.coreVersion = coreVersion;
    }

    public String getProductVersion() {
        return productVersion;
    }

    public void setProductVersion(String productVersion) {
            this.productVersion = productVersion;
    }

    public CurrentVersionInfo(String coreVersion, String productVersion) {
        this.coreVersion = coreVersion;
        this.productVersion = productVersion;
    }

}
