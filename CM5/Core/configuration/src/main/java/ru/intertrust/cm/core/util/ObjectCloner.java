package ru.intertrust.cm.core.util;

import java.io.ByteArrayOutputStream;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.shaded.org.objenesis.strategy.StdInstantiatorStrategy;

/**
 * Вспомогательный класс для клонирования обхектов с использованием библиотеки Kryo
 */
public class ObjectCloner {


    public ObjectCloner() {
    }

    /**
     * Сериализует и десериализует объект, тем самым осуществляется полное клонирование
     * @param source источник
     * @return копию
     */
    public <T> T cloneObject(Object source, Class<T> tClass) {

        if (source == null) return null;

        Kryo kryo = new Kryo();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Output output = new Output(stream);
        kryo.register(tClass);
        kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());

        kryo.writeObject(output, source);
        output.close();
        byte[] bytes = stream.toByteArray();
        int buffSize = bytes.length;
        Input input = new Input(bytes);
        T newBean = kryo.readObject(input, tClass);
        input.close();

        return newBean;
    }

}
