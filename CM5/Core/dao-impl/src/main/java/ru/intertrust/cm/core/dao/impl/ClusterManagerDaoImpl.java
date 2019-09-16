package ru.intertrust.cm.core.dao.impl;

import java.util.Collections;
import java.util.UUID;

import javax.annotation.PostConstruct;


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

public class ClusterManagerDaoImpl implements ClusterManagerDao{
    final private static Logger logger = LoggerFactory.getLogger(ClusterManagerDaoImpl.class);
    private String nodeId;
    
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

}
