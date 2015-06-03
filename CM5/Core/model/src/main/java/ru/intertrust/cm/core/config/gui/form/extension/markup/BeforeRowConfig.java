package ru.intertrust.cm.core.config.gui.form.extension.markup;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.config.gui.form.RowConfig;
import ru.intertrust.cm.core.config.gui.form.extension.IdentifiedFormExtensionOperation;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 02.05.2015
 *         Time: 11:42
 */
@Root(name = "before-tr")
public class BeforeRowConfig implements IdentifiedFormExtensionOperation<RowConfig> {
    @Attribute(name = "id")
    private String id;

    @ElementList(inline = true, name = "tr")
    private List<RowConfig> source = new ArrayList<RowConfig>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<RowConfig> getSource() {
        return source;
    }

    public void setSource(List<RowConfig> source) {
        this.source = source;
    }
    @Override
    public ExtensionPlace getExtensionPlace() {
        return ExtensionPlace.BEFORE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BeforeRowConfig that = (BeforeRowConfig) o;

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
