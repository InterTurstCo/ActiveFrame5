package ru.intertrust.cm.core.gui.model;

/**
 * @author Denis Mitavskiy
 *         Date: 26.11.2014
 *         Time: 15:11
 */
public abstract class BaseClient implements Client {
    protected String descriptor;
    protected String timeZoneId;

    public BaseClient() {
    }

    protected BaseClient(String descriptor, String timeZoneId) {
        this.descriptor = descriptor;
        this.timeZoneId = timeZoneId;
    }

    @Override
    public String getDescriptor() {
        return descriptor;
    }

    public void setDescriptor(String descriptor) {
        this.descriptor = descriptor;
    }

    public String getTimeZoneId() {
        return timeZoneId;
    }

    public void setTimeZoneId(String timeZoneId) {
        this.timeZoneId = timeZoneId;
    }
}
