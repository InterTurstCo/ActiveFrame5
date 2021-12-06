package ru.intertrust.cm.core.gui.model.action;

public class ShowFileActionData extends SimpleActionData{
    private String fileName;
    private String fileUnid;
    private boolean inline;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileUnid() {
        return fileUnid;
    }

    public void setFileUnid(String fileUnid) {
        this.fileUnid = fileUnid;
    }

    public boolean isInline() {
        return inline;
    }

    public void setInline(boolean inline) {
        this.inline = inline;
    }
}
