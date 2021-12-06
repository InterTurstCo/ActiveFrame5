package ru.intertrust.cm.core.dao.impl.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;

import static org.junit.Assert.*;

public class SimpleInitializerTest {

    // Такой тест не дает никакой гарантии на корректность кода, но если тесты запускать почаще, то есть шанс словить багу, если она есть
    @Test
    public void concurrentInitializationTest() throws InterruptedException {
        final ExecutorService executorService = Executors.newFixedThreadPool(5);
        try {
            final SimpleInitializer simpleInitializer = new SimpleInitializer();

            // Запускаем на исполнение
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch finishLatch = new CountDownLatch(5);
            List<TestRunnable> tasks = new ArrayList<>();
            for (int i = 0; i < 5; ++i) {
                final TestRunnable runnable = getRunnable(simpleInitializer, startLatch, finishLatch);
                tasks.add(runnable);
                executorService.execute(runnable);
            }

            // Запускаем одновременно
            startLatch.countDown();

            // Ждем результат
            finishLatch.await();

            // Инициализироваться должен был лишь 1 поток, так что сумма должна быть равна 1
            final Integer sum = tasks.stream()
                    .map(it -> it.atomicInteger.get())
                    .reduce(0, Integer::sum);

            assertEquals(1L, (long) sum);

            // Запустим еще 1, но тут уже не должно быть инициализации, т.к. status должен быть finished
            finishLatch = new CountDownLatch(1);
            final TestRunnable runnable = getRunnable(simpleInitializer, null, finishLatch);
            executorService.execute(runnable);

            finishLatch.await();
            assertEquals(0L, runnable.atomicInteger.get());

        } finally {
            executorService.shutdownNow();
        }
    }

    private TestRunnable getRunnable(SimpleInitializer simpleInitializer, CountDownLatch startLatch,  CountDownLatch finishLatch) {
        return new TestRunnable(simpleInitializer, startLatch, finishLatch);
    }

    private static class TestRunnable implements Runnable {

        private final AtomicInteger atomicInteger = new AtomicInteger(0);
        private final SimpleInitializer simpleInitializer;
        private final CountDownLatch startLatch;
        private final CountDownLatch finishLatch;

        public TestRunnable(SimpleInitializer simpleInitializer, CountDownLatch startLatch, CountDownLatch finishLatch) {

            this.simpleInitializer = simpleInitializer;
            this.startLatch = startLatch;
            this.finishLatch = finishLatch;
        }

        @Override
        public void run() {
            try {
                if (startLatch != null) {
                    startLatch.await();
                }
            } catch (InterruptedException e) {
                return;
            }

            simpleInitializer.init("CATCH ME IF YOU CAN", () -> {
                atomicInteger.incrementAndGet();
            });

            if (finishLatch != null) {
                finishLatch.countDown();
            }
        }
    }

}