package ru.intertrust.cm.core.dao.impl;

import java.util.UUID;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.intertrust.cm.core.dao.api.ClusterManagerDao;

public class ClusterManagerDaoImpl implements ClusterManagerDao{
    final private static Logger logger = LoggerFactory.getLogger(ClusterManagerDaoImpl.class);
    private String nodeId;
    
    @org.springframework.beans.factory.annotation.Value("${server.name:#{null}}")
    private String nodeName;
    
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

}
