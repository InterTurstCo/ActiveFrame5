package ru.intertrust.cm.core.config;

import org.simpleframework.xml.ElementList;

import java.util.ArrayList;
import java.util.List;

/**
 * @author vmatsukevich
 *         Date: 5/16/13
 *         Time: 10:52 AM
 */
public class UniqueKey {

    @ElementList(entry="field", inline=true)
    private List<Field> fields;

    public UniqueKey() {
    }

    public List<Field> getFields() {
        if(fields == null) {
            fields = new ArrayList<Field>();
        }
        return fields;
    }

    public void setFields(List<Field> fields) {
        this.fields = fields;
    }
}
