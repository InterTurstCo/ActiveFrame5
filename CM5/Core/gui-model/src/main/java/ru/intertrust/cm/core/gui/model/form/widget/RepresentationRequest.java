package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;

import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 03.01.14
 *         Time: 13:15
 */
public class RepresentationRequest implements Dto {
    private List<Id> ids;
    private String pattern;
    private boolean useSplit;
    public RepresentationRequest() {
    }

    public RepresentationRequest(List<Id> ids, String pattern, boolean useSplit) {
        this.ids = ids;
        this.pattern = pattern;
        this.useSplit = useSplit;
    }

    public List<Id> getIds() {
        return ids;
    }

    public void setIds(List<Id> ids) {
        this.ids = ids;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public boolean isUseSplit() {
        return useSplit;
    }

    public void setUseSplit(boolean useSplit) {
        this.useSplit = useSplit;
    }
}
