package ru.intertrust.cm.core.util;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.*;
import java.util.ArrayList;

/**
 * Вспомогательный класс для клонирования обхектов с использованием библиотеки Kryo
 */
public class KryoCloner {

    private Kryo kryo;

    public KryoCloner() {
        this.kryo = new Kryo();
        kryo.register(ArrayList.class);
        kryo.setRegistrationRequired(false);
    }

    /**
     * Сериализует и десериализует объект, тем самым осуществляется полное клонирование
     * @param source источник
     * @return копию
     */
    public <T> T cloneObject(Object source, Class<T> tClass) {

        if (source == null) return null;

        long timeMillis = System.currentTimeMillis();

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Output output = new Output(stream);
        kryo.register(tClass);
        kryo.writeObject(output, source);
        output.close();
        byte[] bytes = stream.toByteArray();
        int buffSize = bytes.length;
        Input input = new Input(bytes);
        T newBean = kryo.readObject(input, tClass);
        input.close();

        long delay = System.currentTimeMillis() - timeMillis;
        if (delay > 15) {
            System.out.println("Kryo Clone of " + tClass.getSimpleName() + " ["
                    + buffSize + "] : " + delay);
        }

        return newBean;
    }

}
