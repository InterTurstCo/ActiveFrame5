package ru.intertrust.cm.core.config.gui.form;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

import java.util.Arrays;
import java.util.Collection;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 22.02.2015
 *         Time: 10:43
 */
@Root(name = "widgets")
public class WidgetsIndicationConfig implements Dto {
    private static final String ID_SEPARATOR = ",";
    @Attribute(name = "ids")
    private String ids;

    public Collection<String> getIds() {
        return separateIds();
    }

    public void setIds(String ids) {
        this.ids = ids;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        WidgetsIndicationConfig that = (WidgetsIndicationConfig) o;

        if (ids != null ? !ids.equals(that.ids) : that.ids != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return ids != null ? ids.hashCode() : 0;
    }

    private Collection<String> separateIds(){
        String[] separatedIds = ids.split(ID_SEPARATOR);
        return Arrays.asList(separatedIds);
    }
}
