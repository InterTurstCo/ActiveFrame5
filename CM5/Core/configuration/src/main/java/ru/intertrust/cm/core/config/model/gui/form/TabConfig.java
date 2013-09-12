package ru.intertrust.cm.core.config.model.gui.form;

import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * @author Denis Mitavskiy
 *         Date: 09.09.13
 *         Time: 18:02
 */
public class TabConfig implements Dto {
    private TabGroupListConfig groupList;

    public TabGroupListConfig getGroupList() {
        return groupList;
    }

    public void setGroupList(TabGroupListConfig groupList) {
        this.groupList = groupList;
    }
}
