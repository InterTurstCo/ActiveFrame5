package ru.intertrust.cm.core.gui.model.plugin;

import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * Модель данных панели статистики
 * @author Ravil Abdulkhairov
 * @version 1.0
 * @since 13.11.2015
 */
public class GlobalCacheStatPanel implements Dto {

    private Long size;
    private Float freeSpacePercentage;
    private Float hitCount;

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public Float getFreeSpacePercentage() {
        return freeSpacePercentage;
    }

    public void setFreeSpacePercentage(Float freeSpacePercentage) {
        this.freeSpacePercentage = freeSpacePercentage;
    }

    public Float getHitCount() {
        return hitCount;
    }

    public void setHitCount(Float hitCount) {
        this.hitCount = hitCount;
    }

    public GlobalCacheStatPanel(){}

}
