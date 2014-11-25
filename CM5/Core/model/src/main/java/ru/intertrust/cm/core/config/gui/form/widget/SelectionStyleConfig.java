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

    @Attribute(name = "resizable", required = false)
    private boolean resizable;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isResizable() {
        return resizable;
    }

    public void setResizable(boolean resizable) {
        this.resizable = resizable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SelectionStyleConfig that = (SelectionStyleConfig) o;

        if (resizable != that.resizable) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (resizable ? 1 : 0);
        return result;
    }
    public static enum Type{
        TABLE("table"),
        INLINE("inline");
        private String name;

        Type(String name) {
            this.name = name;
        }

        public static Type forName(String name){
            for (Type type : Type.values()) {
                if(type.name.equalsIgnoreCase(name)){
                    return type;
                }
            }
            return null;
        }
    }
}
