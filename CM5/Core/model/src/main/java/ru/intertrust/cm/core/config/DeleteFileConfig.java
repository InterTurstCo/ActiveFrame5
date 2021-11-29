package ru.intertrust.cm.core.config;

import java.io.Serializable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import org.simpleframework.xml.Attribute;

public class DeleteFileConfig implements Serializable {

    private static final String DELAY_PROP = "delay";

    public enum Mode {
        IMMED(Collections.emptyList()),
        NEVER(Collections.emptyList()),
        DELAYED(Collections.singletonList(new DelayedModeConfig(DELAY_PROP, 1)));

        private final List<ModeConfig> properties;

        Mode(List<ModeConfig> properties) {
            this.properties = Collections.unmodifiableList(properties);
        }

        public List<ModeConfig> getProperties() {
            return properties;
        }
    }

    public interface ModeConfig {
        String getName();

        @Nullable
        Object getDefault();

        @Nullable
        Object valueFromString(String value);
    }

    public static class DelayedModeConfig implements ModeConfig{

        private final String name;
        private final Object defaultValue;

        public DelayedModeConfig(String name, Object value) {
            this.name = name;
            this.defaultValue = value;
        }

        public String getName() {
            return name;
        }

        public Object getDefault() {
            return defaultValue;
        }

        @Override
        public Object valueFromString(String value) {
            if (value == null) {
                return null;
            }

            return Integer.parseInt(value);
        }
    }


    @Attribute(name = "mode", required = false)
    private Mode mode;

    @Attribute(name = "delay", required = false)
    private Integer delay;

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public Integer getDelay() {
        return delay;
    }

    public void setDelay(Integer delay) {
        this.delay = delay;
    }

    public void setProperty(String name, Object value) {
        // Расширение на будущее. В случае если будет множество реализаций (уже сейчас напрашивается 2 реализации)
        if (DELAY_PROP.equalsIgnoreCase(name)) {
            delay = (Integer) value;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != getClass()) {
            return true;
        }
        DeleteFileConfig that = (DeleteFileConfig) obj;
        return this.mode == that.mode
                && (Objects.equals(this.delay, that.delay));
    }

    @Override
    public int hashCode() {
        int hash = 0;
        if (mode != null) {
            hash += mode.ordinal();
        }
        hash *= 31;
        if (delay != null) {
            hash += delay.hashCode();
        }
        return hash;
    }

    @Override
    public String toString() {
        return "DeleteFileConfig{" +
                "mode=" + mode +
                ", delay=" + delay +
                '}';
    }
}
