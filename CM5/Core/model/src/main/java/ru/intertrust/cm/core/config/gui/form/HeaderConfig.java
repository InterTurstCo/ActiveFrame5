package ru.intertrust.cm.core.config.gui.form;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.gui.form.template.TemplateBasedTableConfig;

/**
 * @author Denis Mitavskiy
 *         Date: 09.09.13
 *         Time: 18:00
 */
@Root(name = "header")
public class HeaderConfig implements Dto {
    @Element(name = "table", required=false)
    private TableLayoutConfig tableLayout;

    @Element(name = TemplateBasedTableConfig.CONFIG_TAG_NAME, required=false)
    private TemplateBasedTableConfig templateBasedTableConfig;

    public TableLayoutConfig getTableLayout() {
        return tableLayout;
    }

    public void setTableLayout(TableLayoutConfig tableLayout) {
        this.tableLayout = tableLayout;
    }

    public TemplateBasedTableConfig getTemplateBasedTableConfig() {
        return templateBasedTableConfig;
    }

    public void setTemplateBasedTableConfig(TemplateBasedTableConfig templateBasedTableConfig) {
        this.templateBasedTableConfig = templateBasedTableConfig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        HeaderConfig that = (HeaderConfig) o;

        if (tableLayout != null ? !tableLayout.equals(that.tableLayout) : that.tableLayout != null) {
            return false;
        }
        if (templateBasedTableConfig != null ? !templateBasedTableConfig.equals(that.templateBasedTableConfig)
                : that.templateBasedTableConfig != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return tableLayout != null ? tableLayout.hashCode() : 0;
    }
}
