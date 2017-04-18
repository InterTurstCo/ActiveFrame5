package ru.intertrust.cm.core.config;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Text;
import ru.intertrust.cm.core.config.base.TopLevelConfig;

@Root(name = "url-config")
public class UrlConfig implements TopLevelConfig {

    @Attribute(required = true)
    private String name;

    @Attribute(name = "replace", required = false)
    private String replacementPolicy;

    @Text(data = true)
    private String urlTemplate;

    public String getName() {
        return name;
    }

    @Override
    public ExtensionPolicy getReplacementPolicy() {
        return ExtensionPolicy.fromString(replacementPolicy);
    }

    @Override
    public ExtensionPolicy getCreationPolicy() {
        return ExtensionPolicy.Runtime;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrlTemplate() {
        return urlTemplate;
    }

    public void setUrlTemplate(String urlTemplate) {
        this.urlTemplate = urlTemplate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UrlConfig urlConfig = (UrlConfig) o;

        if (!name.equals(urlConfig.name)) return false;
        if (replacementPolicy != null ? !replacementPolicy.equals(urlConfig.replacementPolicy) : urlConfig.replacementPolicy != null) return false;
        if (urlTemplate != null ? !urlTemplate.equals(urlConfig.urlTemplate) : urlConfig.urlTemplate != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + (urlTemplate != null ? urlTemplate.hashCode() : 0);
        return result;
    }
}
