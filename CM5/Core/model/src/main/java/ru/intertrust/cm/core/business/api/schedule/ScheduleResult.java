package ru.intertrust.cm.core.business.api.schedule;

public enum ScheduleResult {
    NotRun(0),
    Complete(1),
    Error(2),
    Timeout(3);

    long value;

    private ScheduleResult(long value) {
        this.value = value;
    }

    public long toLong() {
        return this.value;
    }

    public ScheduleResult valueOf(long value) {
        if (value == 0) {
            return ScheduleResult.NotRun;
        } else if (value == 1) {
            return ScheduleResult.Complete;
        } else if (value == 2) {
            return ScheduleResult.Complete;
        } else if (value == 3) {
            return ScheduleResult.Complete;
        } else {
            throw new RuntimeException(value + " not valid argument");
        }
    }
}
