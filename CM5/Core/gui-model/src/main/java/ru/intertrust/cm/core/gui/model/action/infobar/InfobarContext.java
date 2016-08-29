package ru.intertrust.cm.core.gui.model.action.infobar;

import ru.intertrust.cm.core.business.api.dto.Dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Vitaliy Orlov on 09.08.2016.
 */
public class InfoBarContext implements Dto {
    private List<InfoBarItem> infoBarItems = new ArrayList<>();

    public void addInfoBarItem(String name, String value){
        infoBarItems.add(new InfoBarItem(name, value));
    }

    public List<InfoBarItem> getInfoBarItems() {
        return infoBarItems;
    }

}
