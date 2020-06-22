package ru.intertrust.cm.globalcacheclient.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.jms.BytesMessage;
import javax.jms.CompletionListener;
import javax.jms.ConnectionFactory;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSProducer;
import javax.jms.Message;
import javax.jms.Topic;
import javax.naming.InitialContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import ru.intertrust.cm.core.business.api.Stamp;
import ru.intertrust.cm.core.business.api.dto.CacheInvalidation;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.globalcache.PingData;
import ru.intertrust.cm.core.business.api.dto.globalcache.PingResponse;
import ru.intertrust.cm.core.business.api.util.ObjectCloner;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.Clock;
import ru.intertrust.cm.core.dao.api.ClusterManagerDao;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.api.ExtensionService;
import ru.intertrust.cm.core.dao.api.GlobalCacheClient;
import ru.intertrust.cm.core.dao.api.extension.diagnostic.OnReceiveDiagnosticMessage;
import ru.intertrust.cm.globalcache.api.util.Size;
import ru.intertrust.cm.globalcacheclient.ClusterTransactionStampService;
import ru.intertrust.cm.globalcacheclient.cluster.GlobalCacheJmsHelper;
import ru.intertrust.cm.globalcacheclient.ping.GlobalCachePingService;
import ru.intertrust.cm.globalcacheclient.ping.PingNodeInfo;

/**
 * Сервис предназанчен для организации работы с MOM для нужд глобального кэша
 * @author larin
 *
 */
public class GlobalCacheJmsHelperImpl implements GlobalCacheJmsHelper {
    final static Logger logger = LoggerFactory.getLogger(GlobalCacheJmsHelperImpl.class);

    public static final String CLUSTER_NOTIFICATION_CONNECTION_FACTORY = "LocalConnectionFactory";
    public static final String NOTIFICATION_TOPIC = "ClusterNotificationTopic";
    public static final String CLUSTER_ID_PROPERTY = "Af5ClusterId";

    @Autowired
    private ClusterManagerDao clusterManagerDao;

    @Autowired
    private Clock clock;

    @Autowired
    private AccessControlService accessControlService;

    @Autowired
    private GlobalCacheClient globalCacheClient;

    @Autowired
    private DomainObjectDao domainObjectDao;

    @Autowired
    private ClusterTransactionStampService clusterTransactionStampService;

    @Autowired
    private ExtensionService extensionService;

    /**
     * Максимальный размер очереди сообщений на отправку
     */
    @Value("${global.cache.message.sender.queue.size:0x7fffffff}")
    private int messageSenderQueueSize;

    /**
     * Имя фабрики подключений к MOM
     */
    @Value("${global.cache.message.sender.connection.factory:" + CLUSTER_NOTIFICATION_CONNECTION_FACTORY + "}")
    private String messageConnectionFactory;

    /**
     * Имя подписки на MOM
     */
    @Value("${global.cache.message.sender.topic:" + NOTIFICATION_TOPIC + "}")
    private String messageTopicName;

    /**
     * Размер пула потоков инвалидации глобального кэша
     */
    @Value("${global.cache.invalidation.pool.size:10}")
    private int invalidationPoolSize;

    /**
     * Максимальный размер очереди принятых сообщений на инвалидацию
     */
    @Value("${global.cache.message.processor.queue.size:0x7fffffff}")
    private int messageProcessorQueueSize;

    /**
     * Периодичность задачи проверки JMS подсистемы
     */
    @Value("${global.cache.message.check.interval:10000}")
    private long checkInterval;
            
    /**
     * Очередь на отправку
     */
    private LinkedBlockingQueue<CacheInvalidation> sendQueue;

    /**
     * Исполнитель (поток), отправляющий сообщения
     */
    private ExecutorService sendExecutor;

    /**
     * Исполнитель (поток), который следит за корректной работой подсистемы отправки и приема JMS сообщений
     */
    private ScheduledExecutorService checkExecutor;
    
    /**
     * Поток принимающий сообщения
     */
    private Thread receiveThread;

    /**
     * JMS Context для отправки. Инициализируется один раз из потока в котором
     * работают задачи sendExecutor
     */
    private JMSContext sendContext;

    /**
     * JMS Публикация для отправки. Инициализируется один раз из потока в
     * котором работают задачи sendExecutor
     */
    private Topic sendTopic;

    /**
     * Отправитель сообщения
     */
    private JMSProducer producer;

    /**
     * Очереди на инвалидацию кэша. Для каждого отправителя своя очередь
     */
    private Map<String, LinkedBlockingQueue<InvalidationProcessorInfo>> invalidateCacheQueue =
            new ConcurrentHashMap<String, LinkedBlockingQueue<InvalidationProcessorInfo>>();

    @PostConstruct
    public void init() {
        sendQueue = new LinkedBlockingQueue<CacheInvalidation>(messageSenderQueueSize);
        sendExecutor = Executors.newSingleThreadExecutor();

        // Контекст для отправки создаем один раз при старте, и делаем это в том потоке, где будем отправлять сообщения
        logger.info("Init global cache message sender queue. Size: {}", messageSenderQueueSize);
        sendExecutor.execute(new InitSendMessageSubsystem());

        // Поток для получения сообщений
        receiveThread = new Thread(new ReceiveMessageThread());
        receiveThread.start();
        
        // Запускаем поток, который должен следить за работоспособностью подсистемы отправки и получения уведомлений
        checkExecutor = Executors.newSingleThreadScheduledExecutor();
        checkExecutor.scheduleAtFixedRate(()->{
            if (producer == null) {
                sendExecutor.execute(new InitSendMessageSubsystem());
            }
            
            if (!receiveThread.isAlive()) {
                receiveThread = new Thread(new ReceiveMessageThread());
                receiveThread.start();
            }
        }, checkInterval, checkInterval, TimeUnit.MILLISECONDS);
    }

    @PreDestroy
    public void deinit() {
        logger.info("Pre Destroy GlobalCacheJmsHelperImpl");

        // Отключаемся от MOM в том потоке где подключены для потока отправки
        sendExecutor.execute(() -> {
            sendContext.close();
            logger.info("Send JMS Message context is closed");
        });

        // Гасим поток отправки сообщений
        sendExecutor.shutdown();

        // Отправяем сигнал потоку приемнику о необходимости остановить работу 
        receiveThread.interrupt();
        
        // Останавливаем следящий поток
        checkExecutor.shutdown();
    }

    /**
     * Постановка сообщения в очередь на отправку. Метод работает в потоке
     * транзакции Осуществляется добавление в очередь ограниченную размером,
     * Далее добавляется в очередь на исполненние в однопотоковом исполнителе.
     */
    @Override
    public void sendClusterNotification(CacheInvalidation message) {
        try {

            if (producer == null) {
                logger.error("JMS subsystem not initialized");
                return;
            }

            synchronized (GlobalCacheJmsHelperImpl.class) {
                // Получение метки времени 
                Stamp<?> stamp = clock.nextStamp();

                // Запись метки в сообщение и в сервис меток времени транзакций
                message.setStamp(stamp);
                clusterTransactionStampService.setLocalInvalidationCacheInfo(stamp);

                // Очередь ограниченна по размеру, если размер превышен поток ждет пока очередь не освободится
                sendQueue.add(message);
            }

            // Добавление задачи на отправку сообщения
            sendExecutor.execute(() -> {
                // Берем из очереди, но пока не удаляем, чтоб в очередь никто не попал лишний
                CacheInvalidation messageFromQueue = sendQueue.peek();
                // Отправляем сообщение
                send(messageFromQueue, true);
                // Сообщение отправлено, можно освободить очередь
                sendQueue.poll();
            });

        } catch (Throwable t) {
            logger.error("Exception while sending cluster notification: " + message, t);
            throw t;
        }
    }

    private void send(CacheInvalidation message, boolean appendNodeId) {
        try {
            
            // make sure this node id is set in the message
            message.setSenderId();
            // Установка идентификатора ноды
            message.setSenderNodeId(clusterManagerDao.getNodeId());

            // Создаем сообщение
            BytesMessage bm = sendContext.createBytesMessage();
            if (appendNodeId) {
                bm.writeLong(CacheInvalidation.NODE_ID);
            }
            final byte[] messageBytes = ObjectCloner.getInstance().toBytesWithClassInfo(message);
            bm.writeInt(messageBytes.length);
            bm.writeBytes(messageBytes);

            // Добавляем информацию о ноде
            bm.setStringProperty(CLUSTER_ID_PROPERTY, clusterManagerDao.getClusterId());

            // отправляем сообщение
            producer.send(sendTopic, bm);

            if (logger.isTraceEnabled()) {
                logger.trace("Node " + CacheInvalidation.NODE_ID + " (\"this\") sent message to cluster: " + message);
            }

        } catch (Exception ex) {
            logger.error("Error send global cache message", ex);
        }
    }

    private class InitSendMessageSubsystem implements Runnable {

        @Override
        public void run() {
            InitialContext initialContext;
            try {
                initialContext = new InitialContext();
                ConnectionFactory tcf = (ConnectionFactory) initialContext.lookup(messageConnectionFactory);

                sendContext = tcf.createContext();
                sendTopic = sendContext.createTopic(messageTopicName);
                producer = sendContext.createProducer();
                producer.setAsync(new CompletionListener() {

                    @Override
                    public void onCompletion(Message message) {
                        logger.trace("Message sended {}", message);
                    }

                    @Override
                    public void onException(Message message, Exception exception) {
                        logger.error("Error send message {}", message);
                    }

                });
                producer.setDisableMessageTimestamp(true);

                logger.info("Send JMS subsystem is initialised");

            } catch (Exception ex) {
                logger.error("Send JMS subsystem is not correct initialised", ex);
            }

        }

    }

    /**
     * 
     * Поток обслуживающий прием JMS сообщения
     */
    private class ReceiveMessageThread implements Runnable {
        /**
         * JMS Context для получения. Инициализируется один раз из потока в
         * котором работают задачи sendExecutor
         */
        private JMSContext receiveContext;

        /**
         * JMS Публикация для получения. Инициализируется один раз из потока в
         * котором работают задачи sendExecutor
         */
        private Topic receiveTopic;

        /**
         * Получатель сообщения
         */
        private JMSConsumer consumer;

        private ExecutorService invalidateExecutor = Executors.newFixedThreadPool(invalidationPoolSize);

        @Override
        public void run() {
            InitialContext initialContext;
            try {
                initialContext = new InitialContext();
                ConnectionFactory tcf = (ConnectionFactory) initialContext.lookup(messageConnectionFactory);

                receiveContext = tcf.createContext();
                receiveTopic = receiveContext.createTopic(messageTopicName);
                consumer = receiveContext.createConsumer(receiveTopic);

                // Сбрасываем глобальный кэш, вдруг что то уже прочиталось в процессе старта и осело там
                globalCacheClient.clearCurrentNode();

                // Слушаем сообщения
                while (!Thread.currentThread().isInterrupted()) {
                    onMessage(consumer.receive());
                }

                receiveContext.close();
            } catch (Exception ex) {
                if (Thread.currentThread().isInterrupted()) {
                    receiveContext.close();
                    logger.info("Receive JMS is shutdown");
                } else {
                    logger.error("Error in receive message thread", ex);
                }
            }
        }

        /**
         * Обработка одного сообщения
         */
        public void onMessage(Message message) {
            try {
               final BytesMessage bytesMessage = (BytesMessage) message;

                // Проверка ид ноды
                String senderClusterId = message.getStringProperty(CLUSTER_ID_PROPERTY);
                if (senderClusterId == null){
                    logger.debug("Message not has cluster id info in header. Ignoring it.");
                    return;
                }

                // Проверяем что сообщения от нашего кластера
                if (!clusterManagerDao.getClusterId().equals(senderClusterId)){
                    logger.debug("Message cluster id " + senderClusterId +" not equals current cluster id " + clusterManagerDao.getClusterId() + ". Ignoring it.");
                    return;
                }

                // Определяем собственное это сообщение или нет
                final long nodeId = bytesMessage.readLong();
                boolean ownMessage = false;
                if (nodeId == CacheInvalidation.NODE_ID) {
                    if (logger.isTraceEnabled()) {
                        logger.trace("Node " + CacheInvalidation.NODE_ID + " (\"this\") received its own message. Ignoring it. ");
                    }
                    ownMessage = true;
                }

                // Десериализуем пришедшее сообщение
                final int messageLength = bytesMessage.readInt();
                if (logger.isTraceEnabled()) {
                    logger.trace("Node " + CacheInvalidation.NODE_ID + " (\"this\") received message from cluster node: "
                            + nodeId + ". Message length: " + messageLength + " bytes.");
                }
                if (messageLength <= 0) {
                    if (logger.isWarnEnabled()) {
                        logger.warn("Erroneous message received from cluster of size " + messageLength + ". Ignored.");
                    }
                    return;
                }
                if (messageLength > 256 * Size.BYTES_IN_MEGABYTE) {
                    logger.warn("Huge message received from cluster of length: " + messageLength / 1024 / 1024 + " GB. Other messages will wait in the queue");
                }
                final byte[] bytes = new byte[messageLength];
                bytesMessage.readBytes(bytes);
                final CacheInvalidation invalidation = ObjectCloner.getInstance().fromBytes(bytes);
                if (logger.isDebugEnabled()) {
                    logger.debug("Node " + CacheInvalidation.NODE_ID + " (\"this\") parsed message from cluster: " + invalidation);
                }

                //Проверка что это диагностическое сообщение сообщение
                if (invalidation.getDiagnosticData() != null) {
                    if (invalidation.getDiagnosticData() instanceof PingData) {
                        PingData pingData = (PingData) invalidation.getDiagnosticData();
                        if (pingData.getResponse() != null) {
                            // Это ping ответ
                            logger.info("Reseive ping response message from " + pingData.getResponse().getNodeName());

                            // Формируем результат
                            PingNodeInfo nodeInfo = new PingNodeInfo();
                            nodeInfo.setNodeName(pingData.getResponse().getNodeName());
                            nodeInfo.setTime(pingData.getResponse().getResponseTime() - pingData.getRequest().getSendTime());

                            // Сохраняем результат
                            GlobalCachePingService.setPingResult(pingData.getRequest().getRequestId(), nodeInfo);
                        } else {
                            // Это ping запрос
                            logger.info("Reseive ping request message from " + pingData.getRequest().getNodeName());
                            //Формируем ответ
                            pingData.setResponse(new PingResponse());
                            pingData.getResponse().setResponseTime(System.currentTimeMillis());
                            String nodeName = clusterManagerDao.getNodeName();
                            pingData.getResponse().setNodeName(nodeName == null ? "not_configured" : nodeName);

                            //Отправляем ответ
                            GlobalCacheJmsHelperImpl.this.sendClusterNotification(invalidation);
                            logger.info("Send ping response from " + clusterManagerDao.getNodeName());
                        }
                    } else {
                        logger.info("Receive diagnostic message");
                        // Обработка тестовых сообщений в слое server-impl, так как тут нет необходимых зависимостей
                        // запускаем в отдельном потоке чтоб освободить очередь для сообщений об инвалидации
                        ExecutorService executor = Executors.newSingleThreadExecutor();
                        executor.execute(() -> {
                            OnReceiveDiagnosticMessage extensionPoint = extensionService.getExtentionPoint(OnReceiveDiagnosticMessage.class, null);
                            extensionPoint.onMessage(invalidation.getDiagnosticData());
                        });
                        executor.shutdown();
                        logger.info("Diagnostic message {} send to processor", invalidation.getDiagnosticData());
                    }

                    // Прекращение обработки диагностических сообщений
                    return;
                }

                // Сообщения от самого себя интересны только в случае диагностических сообщений, остальные игнорируем
                if (ownMessage) {
                    return;
                }

                // Сообщение о сбросе кэша
                if (invalidation.isClearCache()) {
                    globalCacheClient.clearCurrentNode();
                    return;
                }

                invalidation.setReceiveTime(System.currentTimeMillis());

                addToInvalidationQueue(invalidation);

            } catch (Throwable throwable) {
                logger.error("Exception caught when processing message from cluster in ClusterNotificationReceiver.", throwable);
            }
        }

        /**
         * Метод добавляет сообщение о инвалидации в очередь на обработку. Для
         * каждого отправителя существует своя очередь. Далее очередь
         * обрабатывается асинхронно пулом потоков. Сообщение считается
         * принятым. Управление отдается для обработки следующего сообщения. Так
         * как прием ведется в одном потоке дополнительная синхронизация не
         * нужна.
         * @param invalidation
         */
        private void addToInvalidationQueue(final CacheInvalidation invalidation) {
            LinkedBlockingQueue<InvalidationProcessorInfo> oneNodeQueue = invalidateCacheQueue.get(invalidation.getSenderNodeId());
            if (oneNodeQueue == null) {
                oneNodeQueue = new LinkedBlockingQueue<InvalidationProcessorInfo>(messageProcessorQueueSize);
                invalidateCacheQueue.put(invalidation.getSenderNodeId(), oneNodeQueue);
            }

            InvalidationProcessorInfo invalidationInfo = new InvalidationProcessorInfo(invalidation);
            oneNodeQueue.add(invalidationInfo);

            invalidateExecutor.execute(new InvalidateCacheProcessor(invalidationInfo));
        }

    }

    /**
     * Класс ваыполняет операцию инвалидации глобального кэша. Инвалидация
     * ведется в другом потоке, отличном от потока приема сообщения.
     * @author larin
     *
     */
    public class InvalidateCacheProcessor implements Runnable {
        private final InvalidationProcessorInfo invalidationInfo;

        public InvalidateCacheProcessor(final InvalidationProcessorInfo invalidationInfo) {
            this.invalidationInfo = invalidationInfo;
        }

        @Override
        public void run() {
            try {
                // Непосредственно инвалидация кэша
                AccessToken accessToken = accessControlService.createSystemAccessToken(this.getClass().getName());
                // TODO зачем чтоб инвалидировать кэш лезть в базу?
                final List<DomainObject> domainObjects = domainObjectDao.find(
                        new ArrayList<>(invalidationInfo.getInvalidation().getCreatedIdsToInvalidate()), accessToken);
                invalidationInfo.getInvalidation().setCreatedDomainObjectsToInvalidate(domainObjects);
                globalCacheClient.invalidateCurrentNode(invalidationInfo.getInvalidation());

                invalidationInfo.setComplete(true);

                setStampData();
            } catch (Exception ex) {
                logger.error("Error invalidate global cache", ex);
            }
        }

        /**
         * Забираем из очереди все завершенные задачи по инвалидации кэша
         */
        private void setStampData() {
            LinkedBlockingQueue<InvalidationProcessorInfo> oneNodeQueue =
                    invalidateCacheQueue.get(invalidationInfo.getInvalidation().getSenderNodeId());

            // Из очереди забрать все первые завершенные "пары" (peek, проверка флага и poll в критической секции).
            InvalidationProcessorInfo lastInvalidationInfo = null;
            synchronized (oneNodeQueue) {
                // Поиск всех первых завершенных инвалидаций
                while (oneNodeQueue.peek() != null && oneNodeQueue.peek().isComplete()) {
                    lastInvalidationInfo = oneNodeQueue.poll();
                }

                // Метку времени из последней полученной "пары" и следует прописать в вектор V
                if (lastInvalidationInfo != null) {
                    clusterTransactionStampService.setInvalidationCacheInfo(
                            lastInvalidationInfo.getInvalidation().getSenderNodeId(),
                            lastInvalidationInfo.getInvalidation().getStamp());
                }
            }
        }
    }

    /**
     * Элемент очереди инвалидации для одного сервера
     * @author larin
     *
     */
    public class InvalidationProcessorInfo {
        private final CacheInvalidation invalidation;
        private boolean complete = false;

        public InvalidationProcessorInfo(final CacheInvalidation invalidation) {
            this.invalidation = invalidation;
        }

        public CacheInvalidation getInvalidation() {
            return invalidation;
        }

        public boolean isComplete() {
            return complete;
        }

        public void setComplete(boolean complete) {
            this.complete = complete;
        }
    }
}
