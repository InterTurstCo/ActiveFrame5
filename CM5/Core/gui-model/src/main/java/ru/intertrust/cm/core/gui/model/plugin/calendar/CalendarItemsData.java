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
public class CalendarItemsData implements Dto {

    private Id rootObjectId;
    private String imageWidth;
    private String imageHeight;
    private String image;
    private CalendarItemData monthItem;
    private List<CalendarItemData> dayItems;

    public CalendarItemsData() {
    }

    public CalendarItemsData(final Id rootObjectId)
    {
        this.rootObjectId = rootObjectId;
    }

    public String getImageWidth() {
        return imageWidth;
    }

    public CalendarItemsData setImageWidth(String imageWidth) {
        this.imageWidth = imageWidth;
        return this;
    }

    public String getImageHeight() {
        return imageHeight;
    }

    public CalendarItemsData setImageHeight(String imageHeight) {
        this.imageHeight = imageHeight;
        return this;
    }

    public String getImage() {
        return image;
    }

    public CalendarItemsData setImage(String image) {
        this.image = image;
        return this;
    }

    public Id getRootObjectId() {
        return rootObjectId;
    }

    public CalendarItemData getMonthItem() {
        return monthItem;
    }

    public CalendarItemsData setMonthItem(CalendarItemData monthItem) {
        this.monthItem = monthItem;
        return this;
    }

    public List<CalendarItemData> getDayItems() {
        return dayItems;
    }

    public CalendarItemsData addDayItem(final CalendarItemData dayItem) {
        if (dayItems == null) {
            dayItems = new ArrayList<>();
        }
        dayItems.add(dayItem);
        return this;
    }

    @Override
    public String toString() {
        return new StringBuilder(CalendarItemsData.class.getSimpleName())
                .append(": rootObjectId=").append(rootObjectId.toStringRepresentation())
                .append(", image=").append(image)
                .append(", monthItem=").append(monthItem)
                .append(", dayItems=").append(dayItems)
                .toString();
    }
}
