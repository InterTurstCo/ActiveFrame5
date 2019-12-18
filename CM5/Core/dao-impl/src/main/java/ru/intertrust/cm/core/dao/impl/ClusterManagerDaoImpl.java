package ru.intertrust.cm.core.dao.impl;

import java.util.Collections;
import java.util.UUID;

import javax.annotation.PostConstruct;


import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.business.api.dto.LongValue;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.ClusterManagerDao;
import ru.intertrust.cm.core.dao.api.CollectionsDao;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.api.extension.ExtensionPoint;
import ru.intertrust.cm.core.dao.api.extension.OnLoadConfigurationExtensionHandler;

@ExtensionPoint
public class ClusterManagerDaoImpl implements ClusterManagerDao, OnLoadConfigurationExtensionHandler {
    final private static Logger logger = LoggerFactory.getLogger(ClusterManagerDaoImpl.class);
    final private static long SINGLETON_KEY_VALUE = 1;
    final private static String CLUSTER_INFO = "cluster_info";
    final private static String SINGLETON_KEY_FIELD = "singleton_key";
    final private static String CLUSTER_ID_FIELD = "cluster_id";

    private String nodeId;
    private String clusterId;
    
    @org.springframework.beans.factory.annotation.Value("${server.name:#{null}}")
    private String nodeName;

    @Autowired
    private DomainObjectDao domainObjectDao;

    @Autowired
    private AccessControlService accessControlService;

    @PostConstruct
    public void init() {
        nodeId = UUID.randomUUID().toString();
        logger.info("Generate cluster id: " + nodeId);
    }

    @Override
    public String getNodeId() {
        return nodeId;
    }

    @Override
    public String getClusterId() {
        return clusterId;
    }

    @Override
    public String getNodeName() {
        return nodeName;
    }

    @Override
    public boolean hasNode(String nodeId) {
        AccessToken token = accessControlService.createSystemAccessToken(ClusterManagerDaoImpl.class.getName());
        DomainObject nodeInfo =  domainObjectDao.findByUniqueKey(
                "cluster_node", Collections.singletonMap("node_id", new StringValue(nodeId)), token);
        return nodeInfo != null;
    }


    @Override
    public void onLoad() {
        // Инициализация идентификатора кластера
        AccessToken token = accessControlService.createSystemAccessToken(ClusterManagerDaoImpl.class.getName());
        DomainObject clusterInfo =  domainObjectDao.findByUniqueKey(
                CLUSTER_INFO, Collections.singletonMap("singleton_key", new LongValue(SINGLETON_KEY_VALUE)), token);
        if (clusterInfo == null){
            clusterId = UUID.randomUUID().toString();
            clusterInfo = new GenericDomainObject(CLUSTER_INFO);
            clusterInfo.setString(CLUSTER_ID_FIELD, clusterId);
            clusterInfo.setLong(SINGLETON_KEY_FIELD, SINGLETON_KEY_VALUE);
            domainObjectDao.save(clusterInfo, token);
        }else{
            clusterId = clusterInfo.getString(CLUSTER_ID_FIELD);
        }
    }
}
