package ru.intertrust.cm.core.business.api.dto.notification;

import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * Класс данных содержащее сообщение
 * @author larin
 *
 */
public class NotificationText implements Dto{
    private String partName;
    private String text;
    public String getPartName() {
        return partName;
    }
    public void setPartName(String partName) {
        this.partName = partName;
    }
    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }

}
