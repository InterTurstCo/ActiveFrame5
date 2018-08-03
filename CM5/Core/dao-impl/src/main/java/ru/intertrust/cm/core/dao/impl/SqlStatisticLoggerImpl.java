package ru.intertrust.cm.core.dao.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.GlobalServerSettingsService;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTask;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTaskHandle;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTaskParameters;

import javax.ejb.EJBContext;
import javax.ejb.SessionContext;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;


@ScheduleTask(
    name = "SQLStatisticLogger",
    minute = "*/1",
    active = true,
    taskTransactionalManagement = true
)
public class SqlStatisticLoggerImpl implements ScheduleTaskHandle, SqlStatisticLogger {


  private static final int STORAGE_SIZE = 5000;
  private static final Logger logger = LoggerFactory.getLogger(SqlStatisticLoggerImpl.class);
  private final static String STAT_LOGGER_ENABLED = "stat_logger_enabled";
  private static Boolean enabled = false;
  private static Map<String, ConcurrentHashMap<String, StackStatisticStorage>> statistics;
  private static AtomicLong allCount = new AtomicLong(0l);
  private static AtomicLong allTime = new AtomicLong(0l);
  private static final String DELIMITER = "\t";
  private static final String NEW_ROW = "\n";

  @Autowired
  GlobalServerSettingsService globalServerSettingsService;

  public SqlStatisticLoggerImpl() {
    statistics = new ConcurrentHashMap<>(STORAGE_SIZE);
  }

  @Override
  public synchronized void log(String query, long executionTime, StackTraceElement[] stackTraceElements) {
    if (enabled || !enabled) {
      allCount.incrementAndGet();
      allTime.set(allTime.get()+executionTime);
      if (!statistics.containsKey(query)) {
        StackStatisticStorage stackStatisticStorage = new StackStatisticStorage(1l,executionTime);
        String stackString = getStackString(stackTraceElements);
        ConcurrentHashMap<String, StackStatisticStorage> stackMap = new ConcurrentHashMap<String, StackStatisticStorage>();
        stackMap.put(stackString,stackStatisticStorage);
        statistics.put(query, stackMap);
      } else {
        ConcurrentHashMap<String, StackStatisticStorage> stackMap = statistics.get(query);
        String stackString = getStackString(stackTraceElements);
        if(!stackMap.containsKey(stackString)){
          StackStatisticStorage stackStatisticStorage = new StackStatisticStorage(1l,executionTime);
          stackMap.put(stackString,stackStatisticStorage);
        } else {
          StackStatisticStorage statisticStorage = stackMap.get(stackString);
          statisticStorage.incCount();
          statisticStorage.incTime(executionTime);
        }
      }
    }
  }


  @Override
  public String execute(EJBContext ejbContext, SessionContext sessionContext, ScheduleTaskParameters parameters) throws InterruptedException {
    readSettings();

    if (enabled || !enabled) {
      writeLog();
      statistics.clear();
      allTime.set(0l);
      allCount.set(0l);
    }
    return "SQLStatisticLogger completed.";
  }

  private void writeLog() {
    StringBuilder sBuilder = new StringBuilder();
    sBuilder.append(String.format("All count: %s all time: %s ms",allCount,allTime)).append(NEW_ROW);
    for(String key : statistics.keySet()){
      sBuilder.append(buildLogRecord(key,statistics.get(key)));
    }
    logger.debug(sBuilder.toString());
  }

  private String buildLogRecord(String query, ConcurrentHashMap<String, StackStatisticStorage> stackStatistic){
    long cnt = 0;
    long time = 0;
    StringBuilder sBuilder = new StringBuilder();
    sBuilder.append("Count: %s Time: %s ms").append(DELIMITER).append(query).append(DELIMITER);
    for(String key : stackStatistic.keySet()){
      if(cnt>0){
        sBuilder.append(",");
      }
      cnt = cnt + stackStatistic.get(key).getCount();
      time = time + stackStatistic.get(key).getTime();

      sBuilder.append(key).append(DELIMITER).append(stackStatistic.get(key).toString()).append(DELIMITER);
    }
    sBuilder.append(NEW_ROW);
    return String.format(sBuilder.toString(),cnt,time);
  }

  /**
   * Вычитка параметра разрешения логирования из настроек сервера
   */
  private void readSettings() {
    try {
      if (globalServerSettingsService.getBoolean(STAT_LOGGER_ENABLED)) {
        enabled = true;
      } else {
        enabled = false;
      }
    } catch (NullPointerException npe) {
      logger.warn(String.format("Can not read boolean value for global server settings parameter:  %s. Possible not configured yet.", STAT_LOGGER_ENABLED));
    }
  }



  /**
   * Выбираем из стэка классы относящиеся к  ru.intertrust
   * @param stackTraceElements
   * @return
   */
  private String getStackString(StackTraceElement[] stackTraceElements){
    StringBuilder elementsBuilder = new StringBuilder();
    if(stackTraceElements!=null && stackTraceElements.length>0){
      for(int i = stackTraceElements.length-1;i>=0;i--){
        if(stackTraceElements[i].getClassName().contains("ru.intertrust")){
          if(!elementsBuilder.toString().equals("")){
            elementsBuilder.append("->");
          }
          elementsBuilder.append(stackTraceElements[i].getClassName()+"."+stackTraceElements[i].getMethodName());
        }
      }
    }
    return elementsBuilder.toString();
  }

  /**
   * Класс описывает один экземпляр хранения стека.
   */
  class StackStatisticStorage {
    private AtomicLong count;
    private AtomicLong time;

    public StackStatisticStorage(Long count, Long time){
      this.count = new AtomicLong(count);
      this.time = new AtomicLong(time);
    }

    public Long getCount() {
      return count.get();
    }

    public void setCount(AtomicLong count) {
      this.count = count;
    }

    public Long getTime() {
      return time.get();
    }

    public void setTime(AtomicLong time) {
      this.time = time;
    }

    public void incCount(){
      count.incrementAndGet();
    }

    public void incTime(Long queryTime){
      time.set(time.get()+queryTime);
    }

    @Override
    public String toString(){
      return String.format("Count: %s Time: %s ms",getCount(),getTime());
    }
  }
}
