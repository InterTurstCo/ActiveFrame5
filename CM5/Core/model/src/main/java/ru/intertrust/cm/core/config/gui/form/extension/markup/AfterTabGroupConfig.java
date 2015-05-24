package ru.intertrust.cm.core.config.gui.form.extension.markup;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.config.gui.form.TabGroupConfig;
import ru.intertrust.cm.core.config.gui.form.extension.IdentifiedFormExtensionOperation;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 02.05.2015
 *         Time: 12:03
 */
@Root(name = "after-tab-group")
public class AfterTabGroupConfig implements IdentifiedFormExtensionOperation<TabGroupConfig> {
    @Attribute(name = "id")
    private String id;

    @ElementList(inline = true, name = "tab-group")
    private List<TabGroupConfig> source = new ArrayList<TabGroupConfig>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<TabGroupConfig> getSource() {
        return source;
    }

    public void setSource(List<TabGroupConfig> source) {
        this.source = source;
    }
    @Override
    public ExtensionPlace getExtensionPlace() {
        return ExtensionPlace.AFTER;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AfterTabGroupConfig that = (AfterTabGroupConfig) o;

        if (id != null ? !id.equals(that.id) : that.id != null) {
            return false;
        }
        if (source != null ? !source.equals(that.source) : that.source != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        return result;
    }
}

