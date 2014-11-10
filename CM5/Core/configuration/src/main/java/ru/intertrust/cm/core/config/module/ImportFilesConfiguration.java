package ru.intertrust.cm.core.config.module;

import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;

public class ImportFilesConfiguration {
    @Attribute(name="csv-encoding", required=false)
    private String csvEncoding;
    
    @Attribute(name="on-clean-base-by-type", required=false)
    private String onCleanBaseByType;    

    @Attribute(name="rewrite", required=false)
    private Boolean rewrite;
    
    @ElementList(inline=true, required=false, entry="import-file")
    private List<ImportFileConfiguration> importFiles;
    
    public String getCsvEncoding() {
        return csvEncoding;
    }

    public void setCsvEncoding(String csvEncoding) {
        this.csvEncoding = csvEncoding;
    }

    public List<ImportFileConfiguration> getImportFiles() {
        return importFiles;
    }

    public void setImportFiles(List<ImportFileConfiguration> importFiles) {
        this.importFiles = importFiles;
    }
    
    public Boolean getRewrite() {
        return rewrite;
    }

    public void setRewrite(Boolean rewrite) {
        this.rewrite = rewrite;
    }

    public String getOnCleanBaseByType() {
        return onCleanBaseByType;
    }

    public void setOnCleanBaseByType(String onCleanBaseByType) {
        this.onCleanBaseByType = onCleanBaseByType;
    }

}
