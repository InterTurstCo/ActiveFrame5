package ru.intertrust.cm.core.dao.access;

public class PersonAltUid {
    private String alterUid; 
    private String alterUidType;
    
    public PersonAltUid() {
    }
    
    public PersonAltUid(String alterUid, String alterUidType) {
        super();
        this.alterUid = alterUid;
        this.alterUidType = alterUidType;
    }

    public String getAlterUidType() {
        return alterUidType;
    }
    
    public void setAlterUidType(String alterUidType) {
        this.alterUidType = alterUidType;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((alterUid == null) ? 0 : alterUid.hashCode());
        result = prime * result + ((alterUidType == null) ? 0 : alterUidType.hashCode());
        return result;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PersonAltUid other = (PersonAltUid) obj;
        if (alterUid == null) {
            if (other.alterUid != null)
                return false;
        } else if (!alterUid.equals(other.alterUid))
            return false;
        if (alterUidType == null) {
            if (other.alterUidType != null)
                return false;
        } else if (!alterUidType.equals(other.alterUidType))
            return false;
        return true;
    } 
}
