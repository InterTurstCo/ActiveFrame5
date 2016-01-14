package ru.intertrust.performance.gwtrpcproxy;

import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root(name = "journal")
public class GwtRpcJournal {
    
    @ElementList(inline=true, entry="group")
    private List<GwtInteractionGroup> groupList;
    
    @Attribute
    private String xGwtPermutation;
    
    @ElementList(name="user-params", entry="user-param")
    private List<UserParam> params;

    public List<GwtInteractionGroup> getGroupList() {
        return groupList;
    }

    public void setGroupList(List<GwtInteractionGroup> groupList) {
        this.groupList = groupList;
    }

    public String getxGwtPermutation() {
        return xGwtPermutation;
    }

    public void setxGwtPermutation(String xGwtPermutation) {
        this.xGwtPermutation = xGwtPermutation;
    }

    public List<UserParam> getParams() {
        return params;
    }

    public void setParams(List<UserParam> params) {
        this.params = params;
    }
}
