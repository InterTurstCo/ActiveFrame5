package ru.intertrust.performance.gwtrpcproxy;

import java.util.List;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root(name = "journal")
public class GwtRpcJournal {
    
    @ElementList(inline=true, entry="group")
    private List<GwtInteractionGroup> groupList;

    public List<GwtInteractionGroup> getGroupList() {
        return groupList;
    }

    public void setGroupList(List<GwtInteractionGroup> groupList) {
        this.groupList = groupList;
    }
}
