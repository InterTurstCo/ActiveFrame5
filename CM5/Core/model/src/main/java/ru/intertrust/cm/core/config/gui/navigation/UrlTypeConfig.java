package ru.intertrust.cm.core.config.gui.navigation;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * Created by IntelliJ IDEA.
 * Developer: Ravil Abdulkhairov
 * Date: 11.03.2016
 * Time: 11:23
 * To change this template use File | Settings | File and Code Templates.
 */
@Root(name = "url-configuration")
public class UrlTypeConfig implements Dto {

    @Attribute(name = "property-with-base-url", required = false)
    private String propertyWithBaseUrl;

    @Attribute(name = "is-absolute", required = true)
    private boolean absolute = true;

    public String getPropertyWithBaseUrl() {
        return propertyWithBaseUrl;
    }

    public void setPropertyWithBaseUrl(String propertiWithBaseUrl) {
        this.propertyWithBaseUrl = propertyWithBaseUrl;
    }

    public boolean isAbsolute() {
        return absolute;
    }

    public void setAbsolute(boolean absolute) {
        this.absolute = absolute;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        UrlTypeConfig that = (UrlTypeConfig) o;

        if (propertyWithBaseUrl != null ? !propertyWithBaseUrl.equals(that.getPropertyWithBaseUrl()) : that.getPropertyWithBaseUrl() != null) {
            return false;
        }

        if (absolute != that.isAbsolute()) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = propertyWithBaseUrl != null ? propertyWithBaseUrl.hashCode() : 0;
        return result * 23;
    }
}
