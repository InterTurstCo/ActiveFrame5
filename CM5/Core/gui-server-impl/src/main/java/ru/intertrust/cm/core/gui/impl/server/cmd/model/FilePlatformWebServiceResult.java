package ru.intertrust.cm.core.gui.impl.server.cmd.model;

/**
 * Created by Vitaliy.Orlov on 25.06.2018.
 */
public class FilePlatformWebServiceResult extends SuccessPlatformWebServiceResult {

    private byte[] result;

    public byte[] getResult() {
        return result;
    }

    public void setResult(byte[] result) {
        this.result = result;
    }
}
