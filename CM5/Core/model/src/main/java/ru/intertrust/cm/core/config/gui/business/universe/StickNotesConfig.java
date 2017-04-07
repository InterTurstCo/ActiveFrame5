package ru.intertrust.cm.core.config.gui.business.universe;

import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 11.04.2015
 *         Time: 19:23
 */
@Root(name = "stick-notes")
public class StickNotesConfig implements Dto {
    @Override
    public boolean equals(Object o) {
        return this == o ? true : o == null || getClass() != o.getClass() ? false : true;
    }

    @Override
    public int hashCode() {
        return 1;
    }
}
