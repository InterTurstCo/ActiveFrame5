package ru.intertrust.cm.core.config;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * Created by tbilyi on 31.07.2014.
 */
public class LoginScreenConfig implements Dto{

    @Attribute(name = "display-product-version", required = false)
    private boolean DisplayProductVersion;

    @Attribute(name = "display-core-version", required = false)
    private boolean DisplaycoreVersion;

    @Attribute(name = "display-version-list", required = false)
    private boolean displayVersionList;
    
    @Element(name = "product-title", required = true)
    private ProductTitleConfig productTitleConfig;

    public boolean isDisplayProductVersion() {
        return DisplayProductVersion;
    }

    public void setDisplayProductVersion(boolean displayProductVersion) {
        DisplayProductVersion = displayProductVersion;
    }

    public boolean isDisplaycoreVersion() {
        return DisplaycoreVersion;
    }

    public void setDisplaycoreVersion(boolean displaycoreVersion) {
        DisplaycoreVersion = displaycoreVersion;
    }

    public ProductTitleConfig getProductTitleConfig() {
        return productTitleConfig;
    }

    public void setProductTitleConfig(ProductTitleConfig productTitleConfig) {
        this.productTitleConfig = productTitleConfig;
    }
    
    public boolean isDisplayVersionList() {
        return displayVersionList;
    }

    public void setDisplayVersionList(boolean displayVersionList) {
        this.displayVersionList = displayVersionList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LoginScreenConfig that = (LoginScreenConfig) o;

        if (DisplayProductVersion != that.DisplayProductVersion) return false;
        if (DisplaycoreVersion != that.DisplaycoreVersion) return false;
        if (!productTitleConfig.equals(that.productTitleConfig)) return false;
        if (displayVersionList != that.displayVersionList) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (DisplayProductVersion ? 1 : 0);
        result = 31 * result + (DisplaycoreVersion ? 1 : 0);
        result = 31 * result + (displayVersionList ? 1 : 0);
        result = 31 * result + productTitleConfig.hashCode();
        return result;
    }
}
