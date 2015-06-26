package ru.intertrust.performance.gwtrpcproxy;

import org.simpleframework.xml.Attribute;

public class FileInfo {
    
    @Attribute(name="file-name")
    private String fileName;
    @Attribute(name="param-name")
    private String paramName;
    @Attribute(name="file-content-type")
    private String fileContentType;
    public String getFileName() {
        return fileName;
    }
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    public String getParamName() {
        return paramName;
    }
    public void setParamName(String paramName) {
        this.paramName = paramName;
    }
    public String getFileContentType() {
        return fileContentType;
    }
    public void setFileContentType(String fileContentType) {
        this.fileContentType = fileContentType;
    }
    
    
}
