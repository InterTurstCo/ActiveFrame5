package ru.intertrust.cm.globalcacheclient.impl;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import ru.intertrust.cm.core.business.api.Stamp;
import ru.intertrust.cm.core.business.api.util.ObjectCloner;

/**
 * Информация о временных метках коммитов транзакций на всех нодах кластера
 * @author larin
 *
 */
public class ClusterCommitStampsInfo {
    private Map<String, Stamp> nodesStamp;

    public ClusterCommitStampsInfo(Map<String, Stamp> nodesStamp) {
        this.nodesStamp = nodesStamp;
    }

    public ClusterCommitStampsInfo() {
        this.nodesStamp = new HashMap<String, Stamp>();
    }

    public static ClusterCommitStampsInfo decode(String encodeNodesStamp) {
        if (encodeNodesStamp == null) {
            return new ClusterCommitStampsInfo();
        } else {
            // Данный способ десириализации позволяет выполнять одну операцию за 85 микросекунд 
            byte[] infoAsByteArray = Base64.getDecoder().decode(encodeNodesStamp);
            return ObjectCloner.getInstance().fromBytes(infoAsByteArray);
        }
    }

    public String encode() {
        // Данный способ сериализации позволяет выполнять одну операцию за 85 микросекунд 
        byte[] infoAsByteArray = ObjectCloner.getInstance().toBytesWithClassInfo(this);
        return Base64.getEncoder().encodeToString(infoAsByteArray);
    }

    /**
     * Проверка на то что текущие метки равны или больше переданных меток
     * В случае если в текущих метках отсутствует метка какого либо из узлов, 
     * данная проверка считается пройденной, это означае6т что после старта еще не получено ни одной временной метке от данного сервера   
     * @param another
     * @return
     */
    public boolean equalsOrGreater(ClusterCommitStampsInfo another) {
        boolean result = true;
        for (String nodeId : another.nodesStamp.keySet()) {
            Stamp stamp = nodesStamp.get(nodeId);
            if (stamp != null && stamp.compareTo(another.nodesStamp.get(nodeId)) < 0) {
                result = false;
                break;
            }
        }

        return result;
    }

    public Map<String, Stamp> getNodesStamps() {
        return nodesStamp;
    }

}
