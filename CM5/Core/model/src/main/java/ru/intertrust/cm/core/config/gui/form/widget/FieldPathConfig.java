package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.convert.Convert;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.converter.FieldPathOnDeleteActionConverter;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 13/9/13
 *         Time: 12:05 PM
 */
@Root(name = "field-path")
@Convert(FieldPathOnDeleteActionConverter.class)
public class FieldPathConfig implements Dto {
    public static final String CASCADE_STRING = "cascade";
    public static final String UNLINK_STRING = "unlink";

    public static enum OnDeleteAction {
        CASCADE(CASCADE_STRING),
        UNLINK(UNLINK_STRING);

        private final String string;

        private OnDeleteAction() {
            string = null;
        }

        private OnDeleteAction(String str) {
            this.string = str;
        }

        public String getString() {
            return string;
        }

        public static OnDeleteAction getEnum(String name) {
            switch (name) {
                case CASCADE_STRING:
                    return CASCADE;
                case UNLINK_STRING:
                    return UNLINK;
            }
            return null;
        }
    }

    @Attribute(name = "value", required = false)
    private String value;

    @Attribute(name="on-root-delete", required = false)
    private OnDeleteAction onRootDelete;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public OnDeleteAction getOnRootDelete() {
        return onRootDelete;
    }

    public void setOnRootDelete(OnDeleteAction onRootDelete) {
        this.onRootDelete = onRootDelete;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        FieldPathConfig that = (FieldPathConfig) o;

        if (onRootDelete != that.onRootDelete) {
            return false;
        }
        if (value != null ? !value.equals(that.value) : that.value != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = value != null ? value.hashCode() : 0;
        result = 31 * result + (onRootDelete != null ? onRootDelete.hashCode() : 0);
        return result;
    }
}
