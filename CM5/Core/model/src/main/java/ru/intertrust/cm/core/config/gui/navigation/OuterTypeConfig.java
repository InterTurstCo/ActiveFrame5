package ru.intertrust.cm.core.config.gui.navigation;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * Created by IntelliJ IDEA.
 * Developer: Ravil Abdulkhairov
 * Date: 11.03.2016
 * Time: 11:38
 * To change this template use File | Settings | File and Code Templates.
 */
@Root(name = "outer")
public class OuterTypeConfig implements Dto {

    @Attribute(name = "url", required = true)
    private String url;

    @Attribute(name = "open-position", required = true)
    private String openPosition;

    @Element(name = "url-configuration", required = true)
    private UrlTypeConfig urlTypeConfig;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getOpenPosition() {
        return openPosition;
    }

    public void setOpenPosition(String openPosition) {
        this.openPosition = openPosition;
    }

    public UrlTypeConfig getUrlTypeConfig() {
        return urlTypeConfig;
    }

    public void setUrlTypeConfig(UrlTypeConfig urlTypeConfig) {
        this.urlTypeConfig = urlTypeConfig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OuterTypeConfig that = (OuterTypeConfig) o;

        if (url != null ? !url.equals(that.url) : that.url != null) return false;
        if (openPosition != null ? !openPosition.equals(that.openPosition) : that.openPosition != null) return false;
        if (urlTypeConfig != null ? !urlTypeConfig.equals(that.urlTypeConfig) : that.urlTypeConfig != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return url != null ? url.hashCode() : 0;
    }

    @Override
    public String toString() {
        return new StringBuilder(LinkConfig.class.getSimpleName())
                .append(": url=").append(url)
                .append(", open position=").append(openPosition)
                .toString();
    }
}
