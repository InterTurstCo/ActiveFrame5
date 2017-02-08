package ru.intertrust.cm.core.business.api.util;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.DefaultArraySerializers;
import org.objenesis.strategy.StdInstantiatorStrategy;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;

/**
 * Вспомогательный класс для клонирования обхектов с использованием библиотеки Kryo. Класс потоконебезопасный, один экземпляр нельзя
 * использовать при совместном доступе нескольких потоков (например, объявлять ObjectCloner полем класса, который реализует
 * шаблон "Одиночка" ("Singleton").
 */
public class ObjectCloner {
    private static final Class<Value>[] IMMUTABLE_VALUE_CLASSES = new Class[] {
            BooleanValue.class, DateTimeValue.class, DateTimeWithTimeZoneValue.class, DecimalValue.class, ImagePathValue.class,
            LongValue.class, ReferenceValue.class, StringValue.class, TimelessDateValue.class
    };

    private static final ThreadLocal<ObjectCloner> objectCloner = new ThreadLocal<ObjectCloner>() {
        protected ObjectCloner initialValue() {
            return new ObjectCloner();
        }
    };

    private Kryo kryo;

    private ObjectCloner() {
        kryo = new Kryo();
        ((Kryo.DefaultInstantiatorStrategy) kryo.getInstantiatorStrategy()).setFallbackInstantiatorStrategy(new StdInstantiatorStrategy());
        kryo.register(Arrays.asList("").getClass(), new ArraysAsListSerializer(kryo));
        kryo.register(RdbmsId.class, new RdbmsIdSerializer());
        ImmutableValueSerializer immutableValueSerializer = new ImmutableValueSerializer();
        for (Class<Value> valueClass : IMMUTABLE_VALUE_CLASSES) {
            kryo.register(valueClass, immutableValueSerializer);
        }
    }

    /**
     * Возвращает экземпляр данного класса. Экземпляр нельзя использовать при совместном доступе нескольких потоков
     * (например, объявлять ObjectCloner полем класса, который реализует шаблон "Одиночка" ("Singleton"), так класс {@link ObjectCloner} потоконебезопасный.
     * @return
     */
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
        kryo.writeObject(output, source);
        output.close();
        return stream.toByteArray();
    }

    public byte[] toBytesWithClassInfo(Object source) {
        if (source == null) return null;

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Output output = new Output(stream);
        kryo.writeClassAndObject(output, source);
        output.close();
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

    private static class ArraysAsListSerializer extends Serializer<List<?>> {
        private final DefaultArraySerializers.ObjectArraySerializer arraySerializer;

        public ArraysAsListSerializer(Kryo kryo) {
            arraySerializer = new DefaultArraySerializers.ObjectArraySerializer(kryo, Object[].class);
        }

        @Override
        public void write(Kryo kryo, Output output, List<?> object) {
            arraySerializer.write(kryo, output, object.toArray());
        }

        @Override
        public List<?> read(Kryo kryo, Input input, Class<List<?>> type) {
            final Object[] array = arraySerializer.read(kryo, input, Object[].class);
            return Arrays.asList(array);
        }

        @Override
        public List<?> copy(Kryo kryo, List<?> original) {
            return Arrays.asList(arraySerializer.copy(kryo, original.toArray()));
        }
    }

    private static class ImmutableValueSerializer extends Serializer<Value<?>> {
        public ImmutableValueSerializer() {
            super(true, true);
        }

        @Override
        public void write(Kryo kryo, Output output, Value<?> object) {
            kryo.getDefaultSerializer(object.getClass()).write(kryo, output, object);
        }

        @Override
        public Value<?> read(Kryo kryo, Input input, Class<Value<?>> type) {
            return (Value<?>) kryo.getDefaultSerializer(type).read(kryo, input, type);
        }
    }

    private static class RdbmsIdSerializer extends Serializer<RdbmsId> {
        public RdbmsIdSerializer() {
            super(true, true);
        }

        @Override
        public void write(Kryo kryo, Output output, RdbmsId object) {
            kryo.getDefaultSerializer(object.getClass()).write(kryo, output, object);
        }

        @Override
        public RdbmsId read(Kryo kryo, Input input, Class<RdbmsId> type) {
            return (RdbmsId) kryo.getDefaultSerializer(type).read(kryo, input, type);
        }
    }
}
