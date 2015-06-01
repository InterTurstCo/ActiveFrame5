package ru.intertrust.cm.core.util;

import com.esotericsoftware.kryo.Kryo;
import org.objenesis.strategy.StdInstantiatorStrategy;

/**
 * Вспомогательный класс для клонирования обхектов с использованием библиотеки Kryo
 */
public class ObjectCloner {

    private static final ThreadLocal<ObjectCloner> objectCloner = new ThreadLocal<ObjectCloner>() {
        protected ObjectCloner initialValue() {
            return new ObjectCloner();
        }
    };

    private Kryo kryo;

    private ObjectCloner() {
        kryo = new Kryo();
        ((Kryo.DefaultInstantiatorStrategy) kryo.getInstantiatorStrategy()).setFallbackInstantiatorStrategy(new StdInstantiatorStrategy());
    }

    public static ObjectCloner getInstance () {
        return objectCloner.get();
    }

    /**
     * Сериализует и десериализует объект, тем самым осуществляется полное клонирование
     * @param source источник
     * @return копию
     */
    public <T> T cloneObject(Object source, Class<T> tClass) {
        if (source == null) {
            return null;
        }
        return (T) kryo.copy(source);
    }

    public <T> T cloneObject(T source) {
        return (T) kryo.copy(source);
    }
}
