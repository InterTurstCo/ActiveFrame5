package ru.intertrust.cm.core.business.api.schedule;

public enum ScheduleResult {
    /**
     * Успешное завершение задачи
     */
    Complete(1),
    /**
     * Ошибка выполнения задачи
     */
    Error(2),
    /**
     * Задача была остановлена по таймауту
     */
    Timeout(3),
    /**
     * Выключение сервера в момент когда задача еще запущена
     */
    Emergency(4);

    long value;

    private ScheduleResult(long value) {
        this.value = value;
    }

    public long toLong() {
        return this.value;
    }

    public ScheduleResult valueOf(long value) {
        if (value == 1) {
            return ScheduleResult.Complete;
        } else if (value == 2) {
            return ScheduleResult.Error;
        } else if (value == 3) {
            return ScheduleResult.Timeout;
        } else if (value == 4) {
            return ScheduleResult.Emergency;
        } else {
            throw new RuntimeException(value + " not valid argument");
        }
    }
}
