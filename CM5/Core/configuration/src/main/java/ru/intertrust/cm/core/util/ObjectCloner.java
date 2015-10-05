package ru.intertrust.cm.core.util;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.objenesis.strategy.StdInstantiatorStrategy;

import java.io.ByteArrayOutputStream;

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
     * Осуществляет глубокое клонирование объекта. Неизменяемые (immutable) объекты не клонируются
     * @param source источник
     * @return копию
     */
    public <T> T cloneObject(T source) {
        return (T) kryo.copy(source);
    }

    /**
     * Осуществляет глубокое клонирование объекта. Неизменяемые (immutable) объекты не клонируются
     * @param source источник
     * @return копию
     */
    @Deprecated
    public <T> T cloneObject(Object source, Class<T> tClass) {
        if (source == null) {
            return null;
        }
        return (T) kryo.copy(source);
    }

    public byte[] toBytes(Object source) {
        if (source == null) return null;

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Output output = new Output(stream);
        output.close();
        kryo.writeObject(output, source);
        return stream.toByteArray();
    }

    public byte[] toBytesWithClassInfo(Object source) {
        if (source == null) return null;

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Output output = new Output(stream);
        output.close();
        kryo.writeClassAndObject(output, source);
        return stream.toByteArray();
    }

    public <T> T fromBytes(byte[] bytes, Class<T> clazz) {
        return kryo.readObject(new Input(bytes), clazz); // input may not be closed, as no InputStream participates
    }

    public <T> T fromBytes(byte[] bytes) {
        return (T) kryo.readClassAndObject(new Input(bytes)); // input may not be closed, as no InputStream participates
    }

    /**
     * Сериализует и десериализует объект, тем самым осуществляется полное клонирование, в том числе и неизменяемых
     * (immutable) полей классов.
     * @param source источник
     * @return копию
     */
    public <T> T serializeObject(Object source) {
        if (source == null) return null;

        Kryo kryo = new Kryo();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Output output = new Output(stream);

        kryo.writeObject(output, source);
        output.close();

        return (T) kryo.readObject(new Input(stream.toByteArray()), source.getClass());
    }
}
