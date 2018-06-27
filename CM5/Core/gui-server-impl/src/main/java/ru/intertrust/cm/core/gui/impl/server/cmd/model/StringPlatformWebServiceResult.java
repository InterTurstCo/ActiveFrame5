package ru.intertrust.cm.core.gui.impl.server.cmd.model;

/**
 * Created by Vitaliy.Orlov on 25.06.2018.
 */
public class StringPlatformWebServiceResult extends SuccessPlatformWebServiceResult {
    private String result;

    public StringPlatformWebServiceResult(String result) {
        this.result = result;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
