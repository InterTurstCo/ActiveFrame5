package ru.intertrust.globalcache.api;

import static org.junit.Assert.assertTrue;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import ru.intertrust.cm.globalcache.api.util.Size;

public class SizeTest {

    @Test
    public void testMultithreadingSafety() throws InterruptedException {
        final Semaphore semaphore = new Semaphore(0);
        final Size size = new Size(new Size());
        Thread a = new Thread(new Runnable() {

            @Override
            public void run() {
                for (int i = 0; i < 1000; i++) {
                    size.detachFromTotal();
                    size.setTotal(new Size());
                }
                semaphore.release();
            }
        });

        Thread b = new Thread(new Runnable() {

            @Override
            public void run() {
                for (int i = 0; i < 1000; i++) {
                    size.add(1);
                }
                semaphore.release();
            }
        });

        a.start();
        b.start();
        assertTrue(semaphore.tryAcquire(2, 1000, TimeUnit.MILLISECONDS));
    }
}
