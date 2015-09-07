package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.Element;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.gui.NotNullLogicalValidation;

/**
 * @author Lesia Puhova
 *         Date: 14.01.14
 *         Time: 18:29
 *
 *  Конфигурация виджета, позволяющего выбирать одно значение из нескольких возможных.
 *  Является супер-классом для конкретных конфигураций, таких как ComboBoxConfig и RadioButtonConfig.
 */
public abstract class SingleSelectionWidgetConfig extends WidgetConfig implements Dto {
    @NotNullLogicalValidation
    @Element(name = "pattern", required = false)
    protected PatternConfig patternConfig;

    public PatternConfig getPatternConfig() {
        return patternConfig;
    }

    public void setPatternConfig(PatternConfig patternConfig) {
        this.patternConfig = patternConfig;
    }

}
