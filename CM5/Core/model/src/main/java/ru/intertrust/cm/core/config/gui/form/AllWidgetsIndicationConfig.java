package ru.intertrust.cm.core.config.gui.form;

import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 22.02.2015
 *         Time: 10:39
 */
@Root(name = "all")
public class AllWidgetsIndicationConfig implements Dto {
    @Override
    public boolean equals(Object o) {
        return this == o ? true : o == null || getClass() != o.getClass() ? false : true;
    }

    @Override
    public int hashCode() {
        return 0;
    }
}
