package ru.intertrust.cm.core.dao.impl;

import ru.intertrust.cm.core.dao.api.Stamp;

/**
 * Имплементация метки времени. Метка хранится в двух long числах. 
 * @author larin
 *
 */
public class StampImpl implements Stamp<StampImpl> {
    private Long hiDigit;
    private Long lowDigit;

    public StampImpl(long hiDigit, long lowDigit) {
        this.hiDigit = hiDigit;
        this.lowDigit = lowDigit;
    }

    public Long getHiDigit() {
        return hiDigit;
    }

    public Long getLowDigit() {
        return lowDigit;
    }

    @Override
    public int compareTo(StampImpl o) {
        int result = 0;
        if (o == null) {
            result = Integer.MAX_VALUE;
        } else {

            result = hiDigit.compareTo(o.hiDigit);

            if (result == 0) {
                result = lowDigit.compareTo(o.lowDigit);
            }
        }

        return result;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((hiDigit == null) ? 0 : hiDigit.hashCode());
        result = prime * result + ((lowDigit == null) ? 0 : lowDigit.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        StampImpl other = (StampImpl) obj;
        if (hiDigit == null) {
            if (other.hiDigit != null)
                return false;
        } else if (!hiDigit.equals(other.hiDigit))
            return false;
        if (lowDigit == null) {
            if (other.lowDigit != null)
                return false;
        } else if (!lowDigit.equals(other.lowDigit))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "StampImpl [hiDigit=" + hiDigit + ", lowDigit=" + lowDigit + "]";
    }
}
