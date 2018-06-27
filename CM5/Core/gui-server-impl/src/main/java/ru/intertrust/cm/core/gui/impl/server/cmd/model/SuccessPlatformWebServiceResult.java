package ru.intertrust.cm.core.gui.impl.server.cmd.model;

/**
 * Created by Vitaliy.Orlov on 25.06.2018.
 */
public class SuccessPlatformWebServiceResult extends PlatformWebServiceResult {
    @Override
    public int getHttpStatusCode() {
        return 200;
    }
}
