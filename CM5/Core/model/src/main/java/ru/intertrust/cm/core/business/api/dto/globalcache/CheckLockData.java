package ru.intertrust.cm.core.business.api.dto.globalcache;

import ru.intertrust.cm.core.business.api.dto.Id;

public class CheckLockData extends DiagnosticData{
    private Id lockResourceId;

    public Id getLockResourceId() {
        return lockResourceId;
    }

    public void setLockResourceId(Id lockResourceId) {
        this.lockResourceId = lockResourceId;
    }
}
