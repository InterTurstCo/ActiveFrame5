package ru.intertrust.cm.core.gui.model.action;

/**
 * @author Lesia Puhova
 *         Date: 18.03.14
 *         Time: 17:41
 */
public class GenerateReportActionData extends ActionData {

    private byte[] reportBytes;

    public byte[] getReportBytes() {
        return reportBytes;
    }

    public void setReportBytes(byte[] reportBytes) {
        this.reportBytes = reportBytes;
    }
}
