package ru.intertrust.cm.core.config.gui.form.extension.markup;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.config.gui.form.CellConfig;
import ru.intertrust.cm.core.config.gui.form.extension.IdentifiedFormExtensionOperation;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 02.05.2015
 *         Time: 11:05
 */
@Root(name = "before-td")
public class BeforeCellConfig implements IdentifiedFormExtensionOperation<CellConfig> {
    @Attribute(name = "id")
    private String id;

    @ElementList(inline = true, name = "td")
    private List<CellConfig> source = new ArrayList<CellConfig>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<CellConfig> getSource() {
        return source;
    }

    public void setSource(List<CellConfig> source) {
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

        BeforeCellConfig that = (BeforeCellConfig) o;

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
        return id != null ? id.hashCode() : 0;
    }

}
