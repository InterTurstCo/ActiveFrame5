package ru.intertrust.cm.core.config;

import java.util.List;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root(name="ImportData")
public class ImportDataConfig {
    
    @ElementList(inline=true)
    private List<ImportFileConfig> file;

    public List<ImportFileConfig> getFile() {
        return file;
    }

    public void setFile(List<ImportFileConfig> file) {
        this.file = file;
    }
}
