package ru.intertrust.cm.core.gui.model.plugin;

import java.math.BigDecimal;

/**
 * @author Denis Mitavskiy
 *         Date: 23.08.13
 *         Time: 13:43
 */
public class SomeActivePluginData extends ActivePluginData {
    private BigDecimal number;

    public SomeActivePluginData() {
    }

    public SomeActivePluginData(BigDecimal number) {
        this.number = number;
    }

    public BigDecimal getText() {
        return number;
    }

    public void setText(BigDecimal text) {
        this.number = number;
    }

    @Override
    public String toString() {
        return "SomeActivePluginData {" +
                "number=" + number +
                "actions=" + getActionConfigs() +
                '}';
    }
}
