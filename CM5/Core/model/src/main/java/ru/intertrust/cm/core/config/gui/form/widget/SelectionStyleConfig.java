package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;



/**
 * Created with IntelliJ IDEA.
 * User: lvov
 * Date: 02.12.13
 * Time: 11:33
 * To change this template use File | Settings | File Templates.
 */

@Root(name = "selection-style")
public class SelectionStyleConfig implements Dto {

    @Attribute(name = "name")
    String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SelectionStyleConfig)) return false;

        SelectionStyleConfig that = (SelectionStyleConfig) o;

        if (!name.equals(that.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
