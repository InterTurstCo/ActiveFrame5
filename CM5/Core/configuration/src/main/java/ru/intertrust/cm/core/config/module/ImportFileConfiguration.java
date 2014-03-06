package ru.intertrust.cm.core.config.module;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Text;

public class ImportFileConfiguration {
    
    @Attribute(name="rewrite", required=false)
    private Boolean rewrite;
   
    @Text
    private String fileName;
    
    public Boolean getRewrite() {
        return rewrite;
    }

    public void setRewrite(Boolean rewrite) {
        this.rewrite = rewrite;
    }
    
    public String getFileName() {
        return fileName;
    }

    public void setSetFileName(String fileName) {
        this.fileName = fileName;
    }
}
