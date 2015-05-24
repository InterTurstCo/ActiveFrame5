package ru.intertrust.cm.core.config.gui.form;

import ru.intertrust.cm.core.business.api.dto.Dto;

import java.util.List;

/**
 * @author Denis Mitavskiy
 *         Date: 09.09.13
 *         Time: 18:05
 */
public abstract class TabGroupListConfig implements Dto {
    public abstract List<TabGroupConfig> getTabGroupConfigs();

}
