package ru.intertrust.cm.core.config;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import java.io.Serializable;

/**
 * @author vmatsukevich
 *         Date: 7/9/13
 *         Time: 5:45 PM
 */
@Root
public class ModuleConfig implements Serializable  {

    @Element(name = "path", required = true)
    private String path;

    @Element(name="schema-path", required = true)
    private String schemaPath;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getSchemaPath() {
        return schemaPath;
    }

    public void setSchemaPath(String schemaPath) {
        this.schemaPath = schemaPath;
    }
}
