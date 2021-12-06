package ru.intertrust.cm.core.gui.model.action;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class UploadActionContext extends ActionContext {
    Map<String, File> uploadedFiles = new HashMap<>();

    public Map<String, File> getUploadedFiles() {
        return uploadedFiles;
    }

    public void setUploadedFiles(Map<String, File> uploadedFiles) {
        this.uploadedFiles = uploadedFiles;
    }
}
