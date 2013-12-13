package ru.intertrust.cm.core.business.api.dto;

public final class AttachmentUploadPercentage implements Dto {

    private Integer percentage = 0;

    public AttachmentUploadPercentage() {
    }

    public Integer getPercentage() {
        return percentage;
    }

    public void setPercentage(Integer percentage) {
        this.percentage = percentage;
    }

    @Override
    public String toString() {
        return "uploaded " + percentage;
    }
}