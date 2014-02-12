package ru.intertrust.cm.core.config.module;

import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;

public class ImportFilesConfiguration {
    @Attribute(name="csv-encoding", required=false)
    private String csvEncoding;

    @ElementList(inline=true, required=false, entry="import-file")
    private List<String> importFiles;
    
    public String getCsvEncoding() {
        return csvEncoding;
    }

    public void setCsvEncoding(String csvEncoding) {
        this.csvEncoding = csvEncoding;
    }

    public List<String> getImportFiles() {
        return importFiles;
    }

    public void setImportFiles(List<String> importFiles) {
        this.importFiles = importFiles;
    }


}
