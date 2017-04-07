package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * Created by IntelliJ IDEA.
 * Developer: Ravil Abdulkhairov
 * Date: 31.03.2016
 * Time: 14:14
 * To change this template use File | Settings | File and Code Templates.
 */
@Root(name = "user-settings")
public class UserSettingsConfig implements Dto {
    @Attribute(name = "save", required = true)
    private boolean save = true;

    public boolean isSave() {
        return save;
    }

    public void setSave(boolean save) {
        this.save = save;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserSettingsConfig that = (UserSettingsConfig) o;

        if (save != that.save) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return (save ? 1 : 0);
    }
}
