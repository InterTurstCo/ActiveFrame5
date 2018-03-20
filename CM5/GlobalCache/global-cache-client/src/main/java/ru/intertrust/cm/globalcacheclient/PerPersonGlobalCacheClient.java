package ru.intertrust.cm.globalcacheclient;

import javax.ejb.EJBContext;
import javax.ejb.SessionContext;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.dto.DomainObjectsModification;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.globalcache.api.GroupAccessChanges;
import ru.intertrust.cm.globalcache.api.PersonAccessChanges;

/**
 * @author Denis Mitavskiy
 *         Date: 22.07.2015
 *         Time: 14:17
 */
public class PerPersonGlobalCacheClient extends PerGroupGlobalCacheClient {
    @Autowired
    private PersonAccessHelper personAccessHelper;

    @Autowired
    private GlobalCacheSettings settings;

    @Override
    public void notifyCommit(DomainObjectsModification modification) {
        String transactionId = modification.getTransactionId();
        GroupAccessChanges groupAccessChanges = createAccessChangesIfAbsent(transactionId);
        clearTransactionChanges(transactionId);

        if (modification.isEmpty() && !groupAccessChanges.accessChangesExist()) {
            return;
        }

        PersonAccessChanges personAccessChanges = null;
        if (Boolean.TRUE.equals(getEjbContext().getContextData().get(CurrentUserAccessor.INITIAL_DATA_LOADING))) {
            personAccessChanges = new PersonAccessChanges();
        }else{
            personAccessChanges = getPersonAccessChanges(groupAccessChanges);
        }
        
        globalCache.notifyCommit(modification, personAccessChanges);
        if (settings.isInCluster()) {
            clusterSynchronizer.notifyCommit(modification, personAccessChanges);
        }
    }

    private EJBContext getEjbContext() {
        try {
            InitialContext ic = new InitialContext();
            return (SessionContext) ic.lookup("java:comp/EJBContext");
        } catch (NamingException ex) {
            throw new IllegalStateException(ex);
        }
    }
    
    @Override
    public void notifyPersonGroupChanged(Id person) {
        getAccessChanges().personGroupChanged(person);
    }

    @Override
    public void notifyGroupBranchChanged(Id groupId) {
        super.notifyGroupBranchChanged(groupId);
    }

    private PersonAccessChanges getPersonAccessChanges(GroupAccessChanges groupAccessChanges) {
        return personAccessHelper.getPersonAccessChanges(groupAccessChanges);
    }
}
