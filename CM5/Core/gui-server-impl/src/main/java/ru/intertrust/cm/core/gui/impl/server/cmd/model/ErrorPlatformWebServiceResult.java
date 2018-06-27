package ru.intertrust.cm.core.gui.impl.server.cmd.model;

/**
 * Created by Vitaliy.Orlov on 25.06.2018.
 */
public class ErrorPlatformWebServiceResult extends PlatformWebServiceResult {
    private String error;

    public ErrorPlatformWebServiceResult(String error) {
        this.error = error;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    @Override
    public int getHttpStatusCode() {
        return 500;
    }
}
