package ru.intertrust.cm.core.config.gui.form.template;

import org.simpleframework.xml.Attribute;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 23.08.2015
 *         Time: 21:29
 */
public abstract class TemplateBasedConfig implements Dto{
    @Attribute(name = "template")
    private String template;

    @Attribute(name = "ids-prefix", required = false)
    private String idsPrefix;

    public String getTemplate() {
        return template;
    }

    public String getIdsPrefix() {
        return idsPrefix;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TemplateBasedConfig that = (TemplateBasedConfig) o;

        if (idsPrefix != null ? !idsPrefix.equals(that.idsPrefix) : that.idsPrefix != null) {
            return false;
        }
        if (template != null ? !template.equals(that.template) : that.template != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = template != null ? template.hashCode() : 0;
        result = 31 * result + (idsPrefix != null ? idsPrefix.hashCode() : 0);
        return result;
    }
}
