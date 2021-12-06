package ru.intertrust.cm.core.config.gui.action;

import java.util.Objects;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root(name = "simple-upload-action")
public class SimpleUploadActionConfig extends ActionConfig {

    public SimpleUploadActionConfig() {
        super();
    }

    public SimpleUploadActionConfig(String actionHandler) {
        super();
        this.actionHandler = actionHandler;
    }

    @Attribute(name = "action-handler")
    private String actionHandler;

    @Attribute(name = "multiple-file", required = false)
    private Boolean multipleFile;

    @Attribute(name = "file-extensions", required = false)
    private String fileExtensions;

    @Override
    public String getComponentName() {
        return "simple.upload.action";
    }

    public String getActionHandler() {
        return actionHandler;
    }

    public void setActionHandler(String actionHandler) {
        this.actionHandler = actionHandler;
    }

    public Boolean getMultipleFile() {
        return multipleFile;
    }

    public void setMultipleFile(Boolean multipleFile) {
        this.multipleFile = multipleFile;
    }

    public String getFileExtensions() {
        return fileExtensions;
    }

    public void setFileExtensions(String fileExtensions) {
        this.fileExtensions = fileExtensions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        SimpleUploadActionConfig that = (SimpleUploadActionConfig) o;
        return Objects.equals(actionHandler, that.actionHandler) &&
                Objects.equals(multipleFile, that.multipleFile) &&
                Objects.equals(fileExtensions, that.fileExtensions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), actionHandler, multipleFile, fileExtensions);
    }
}
