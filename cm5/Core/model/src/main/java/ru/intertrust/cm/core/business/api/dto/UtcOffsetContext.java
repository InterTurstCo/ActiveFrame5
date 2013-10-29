package ru.intertrust.cm.core.business.api.dto;

/**
* @author vmatsukevich
*         Date: 10/29/13
*         Time: 12:38 PM
*/
public class UtcOffsetContext extends DateContext {
    private long offset;

    public UtcOffsetContext(long offset) {
        this.offset = offset;
    }

    public long getOffset() {
        return offset;
    }
}
