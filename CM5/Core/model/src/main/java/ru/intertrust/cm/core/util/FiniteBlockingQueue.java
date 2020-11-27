package ru.intertrust.cm.core.util;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Вариация на тему {@code BlockingQueue<T>}.
 * Не имплементирует ее лишь потому, что на момент написания класса мне были нужны только методы {@code take()} и {@code put()}.
 * <p>
 * От стандартной {@code BlockingQueue<T>} отличается наличием дополнительных методов {@link #markEndOfData()} и {@link #interruptClients()}.
 * <p>
 * Данная имплементация <b>не поддерживает</b> {@code null} в пишущих методах.
 * <p>
 * Все методы класса, выбрасывающие {@link InterruptedException}, выбрасывают его <b>только</b> в случае, если в каком-то потоке-клиенте
 * был вызван метод {@link #interruptClients()}.
 * @author Gleb Nozdrachev
 * @param <T> - тип хранимых значений;
 */
@ThreadSafe
public class FiniteBlockingQueue<T> {

    private static final Object VALUE_END_OF_DATA = new Object();

    private final Queue<T> queue = new LinkedList<>();
    private final Lock lock = new ReentrantLock();
    private final Condition writers = this.lock.newCondition();
    private final Condition readers = this.lock.newCondition();
    private final int capacity;
    private boolean isEndOfData;
    private boolean isAlwaysReturnNull;
    private boolean isClientsInterrupted;

    /**
     * Создает новый инстанс с заданной вместительностью очереди.
     * @param capacity - вместительность очереди, должна быть > 0;
     */
    public FiniteBlockingQueue (final int capacity) {

        if (capacity < 1) {
            throw new IllegalArgumentException("capacity must be positive, but is " + capacity);
        }

        this.capacity = capacity;

    }

    /**
     * Помечает очередь флагом "конец данных". По смыслу вызывается потоком-поставщиком. Не гарантируется "мгновенный" возврат из метода.
     * <p>
     * Для потоков-читателей сигналом "конец данных" является получение {@code null} при чтении из очереди.
     * <p>
     * Разрешен повторный вызов метода (например, из другого потока). Вызов пишущих методов после данного приведет к выбросу исключения.
     * @throws InterruptedException см. в описании класса;
     */
    public void markEndOfData () throws InterruptedException {
        this.put((T)VALUE_END_OF_DATA);
    }

    /**
     * Прервывает работу прочих потоков-клиентов данной очереди. Все ожидающие методы, вызванные в других потоках-клиентах,
     * выбросят {@link InterruptedException}. Не гарантируется "мгновенный" возврат из метода
     * <p>
     * Типичное использование - в обработчике ошибок потока.
     */
    public void interruptClients () {

        this.lock.lock();

        try {
            this.isClientsInterrupted = true;
            this.writers.signalAll();
            this.readers.signalAll();
        } finally {
            this.lock.unlock();
        }

    }

    public void put (final @Nonnull T value) throws InterruptedException {

        Args.notNull(value, "value");
        this.lock.lock();

        try {

            if (this.isClientsInterrupted) {
                throw new InterruptedException();
            }

            if (value == VALUE_END_OF_DATA) {
                if (this.isEndOfData) {
                    return;
                } else {
                    this.isEndOfData = true;
                }
            } else if (this.isEndOfData) {
                throw new RuntimeException("This queue already marked as \"end-of-data\"");
            }

            while (this.queue.size() == this.capacity) {
                this.writers.await();
                if (this.isClientsInterrupted) {
                    throw new InterruptedException();
                }
            }

            this.queue.add(value);
            this.readers.signal();

        } finally {
            this.lock.unlock();
        }

    }

    public T take () throws InterruptedException {

        this.lock.lock();

        try {

            if (this.isClientsInterrupted) {
                throw new InterruptedException();
            }

            T result = null;

            while (!this.isAlwaysReturnNull && (result = this.queue.poll()) == null) {
                this.readers.await();
                if (this.isClientsInterrupted) {
                    throw new InterruptedException();
                }
            }

            if (result == VALUE_END_OF_DATA) {
                this.isAlwaysReturnNull = true;
                this.readers.signalAll();
                result = null;
            } else if (!this.isAlwaysReturnNull) {
                this.writers.signal();
            }

            return result;

        } finally {
            this.lock.unlock();
        }

    }

}