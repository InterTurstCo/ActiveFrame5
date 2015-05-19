package ru.intertrust.cm.core.util;

import java.io.ByteArrayOutputStream;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.UnsafeInput;
import com.esotericsoftware.kryo.io.UnsafeOutput;
import org.objenesis.strategy.StdInstantiatorStrategy;

/**
 * Вспомогательный класс для клонирования обхектов с использованием библиотеки Kryo
 */
public class ObjectCloner {

    private static ThreadLocal<ObjectCloner> objectCloner = new ThreadLocal<ObjectCloner>() {
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

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        UnsafeOutput output = new UnsafeOutput(stream);

        kryo.register(tClass);
        kryo.writeObject(output, source);
        output.close();

        byte[] bytes = stream.toByteArray();
        UnsafeInput input = new UnsafeInput(bytes);
        T newBean = kryo.readObject(input, tClass);
        input.close();

        return newBean;
    }

}
