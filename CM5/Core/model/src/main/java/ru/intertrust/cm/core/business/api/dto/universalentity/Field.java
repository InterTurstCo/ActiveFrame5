package ru.intertrust.cm.core.business.api.dto.universalentity;

import java.io.Serializable;

public interface Field<T> extends Serializable {

    T get ();

    void set (T value);

}