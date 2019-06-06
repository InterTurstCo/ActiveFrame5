package ru.intertrust.cm.globalcacheclient.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.Stamp;
import ru.intertrust.cm.core.dao.api.Clock;
import ru.intertrust.cm.core.dao.api.ClusterManagerDao;
import ru.intertrust.cm.globalcacheclient.ClusterTransactionStampService;

public class ClusterTransactionStampServiceImpl implements ClusterTransactionStampService {
    //Вектор V из технического решения https://conf.inttrust.ru:8443/pages/viewpage.action?pageId=24275514
    public final Map<String, Stamp> nodesStamp = new ConcurrentHashMap<String, Stamp>();

    @Autowired
    private Clock clock;

    @Autowired
    private ClusterManagerDao clusterManagerDao;

    @Override
    public Map<String, Stamp> getInvalidationCacheInfo() {
        return nodesStamp;
    }

    @Override
    public void setInvalidationCacheInfo(String serverName, Stamp serverStamp) {
        nodesStamp.merge(serverName, serverStamp, (prevStamp, newStamp) -> newStamp.compareTo(prevStamp) > 0 ? newStamp : prevStamp);

    }

    @Override
    public void setLocalInvalidationCacheInfo() {
        nodesStamp.merge(clusterManagerDao.getNodeName(), clock.nextStamp(), (prevStamp, newStamp) -> newStamp.compareTo(prevStamp) > 0 ? newStamp : prevStamp);
    }

}
