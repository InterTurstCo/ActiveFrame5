package ru.intertrust.cm.core.dao.impl;

public interface SqlStatisticLogger {
  void log(String query, long executionTime, StackTraceElement[] stackTraceElements);
}
