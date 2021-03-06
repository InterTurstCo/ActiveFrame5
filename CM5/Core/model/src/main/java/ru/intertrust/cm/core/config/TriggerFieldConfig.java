package ru.intertrust.cm.core.config;


/**
 * 
 * @author atsvetkov
 *
 */
public class TriggerFieldConfig extends DataConfig {

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        TriggerFieldConfig other = (TriggerFieldConfig) obj;
        if (data == null) {
            if (other.data != null) {
                return false;
            }
        } else if (!data.equals(other.data)) {
            return false;
        }
        return true;
    }
    
}
