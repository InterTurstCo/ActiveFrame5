package ru.intertrust.cm.core.config.doel;

public class ValidationReport {

    private StringBuilder report;

    public void addRecord(String record) {
        report.append(record).append('\n');
    }

    public boolean hasRecords() {
        return report.length() > 0;
    }

    public String get() {
        return report.toString();
    }

    @Override
    public String toString() {
        return get();
    }
}
