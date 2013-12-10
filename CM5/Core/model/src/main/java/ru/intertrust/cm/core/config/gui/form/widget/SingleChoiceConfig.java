package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 06.11.13
 *         Time: 11:15
 */
@Root(name = "single-choice")
public class SingleChoiceConfig implements Dto {
    @Attribute(name = "value")
    private boolean singleChoice;

    public boolean isSingleChoice() {
        return singleChoice;
    }

    public void setSingleChoice(boolean singleChoice) {
        this.singleChoice = singleChoice;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SingleChoiceConfig that = (SingleChoiceConfig) o;

        if (singleChoice != that.singleChoice) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return (singleChoice ? 1 : 0);
    }
}

