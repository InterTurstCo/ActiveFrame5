package ru.intertrust.cm.core.gui.model.plugin.calendar;

import java.util.ArrayList;
import java.util.List;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;

/**
 *
 * @author Sergey.Okolot
 *         Created on 13.10.2014 15:44.
 */
public class CalendarItemData implements Dto {

    private Id rootObjectId;
    private String imageWidth;
    private String imageHeight;
    private String image;
    private String monthItem;
    private List<String> dayItems;

    public CalendarItemData() {
    }

    public CalendarItemData(final Id rootObjectId)
    {
        this.rootObjectId = rootObjectId;
    }

    public String getImageWidth() {
        return imageWidth;
    }

    public CalendarItemData setImageWidth(String imageWidth) {
        this.imageWidth = imageWidth;
        return this;
    }

    public String getImageHeight() {
        return imageHeight;
    }

    public CalendarItemData setImageHeight(String imageHeight) {
        this.imageHeight = imageHeight;
        return this;
    }

    public String getImage() {
        return image;
    }

    public CalendarItemData setImage(String image) {
        this.image = image;
        return this;
    }

    public Id getRootObjectId() {
        return rootObjectId;
    }

    public String getMonthItem() {
        return monthItem;
    }

    public CalendarItemData setMonthItem(String monthItem) {
        this.monthItem = monthItem;
        return this;
    }

    public List<String> getDayItems() {
        return dayItems;
    }

    public CalendarItemData addDayItem(final String dayItem) {
        if (dayItems == null) {
            dayItems = new ArrayList<>();
        }
        dayItems.add(dayItem);
        return this;
    }

    @Override
    public String toString() {
        return new StringBuilder(CalendarItemData.class.getSimpleName())
                .append(": rootObjectId=").append(rootObjectId.toStringRepresentation())
                .append(", image=").append(image)
                .append(", monthItem=").append(monthItem)
                .append(", dayItems=").append(dayItems)
                .toString();
    }
}
