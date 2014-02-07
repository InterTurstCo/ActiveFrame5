package ru.intertrust.cm.core.config;

import java.io.Serializable;
import java.util.List;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

@Root(name="ImportData")
public class ImportDataConfig implements Dto {
    
    @ElementList(inline=true)
    private List<ImportFileConfig> file;

    public List<ImportFileConfig> getFile() {
        return file;
    }

    public void setFile(List<ImportFileConfig> file) {
        this.file = file;
    }
}
