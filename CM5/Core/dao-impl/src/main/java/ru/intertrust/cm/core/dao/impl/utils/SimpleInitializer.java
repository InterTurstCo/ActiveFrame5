package ru.intertrust.cm.core.dao.impl.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import ru.intertrust.cm.core.model.FatalException;

/**
 * Simple Initializer is the util to protect some process from multiple executions.
 * To use this class, share the instance between threads and use method init.
 *
 * This class guaranties that the process with the same lockInfo will be executed only once per SimpleInitializer instance.
 */
public class SimpleInitializer {

    private final Map<String, InitProcess> initData = new ConcurrentHashMap<>();

    /**
     * Method init guaranties that the {@link Runnable} will be executed only once for the same lockInfo string
     *
     * @param lockInfo - any unique string for the process. It may be UNID, unique server name or smh else
     * @param process - any {@link Runnable} object. It will be executed in the current thread
     */
    public void init(String lockInfo, Runnable process) {

        final InitProcess currentProcess = new InitProcess(InitState.IN_PROCESS, new CountDownLatch(1));
        final InitProcess initProcess = initData.putIfAbsent(lockInfo, currentProcess);
        if (initProcess == null) {
            try {
                process.run();
                currentProcess.countDownLatch.countDown();
                initData.put(lockInfo, new InitProcess(InitState.FINISHED));
            } catch (Exception ex) {
                initData.remove(lockInfo);
                throw new FatalException(
                        "Error on init process", ex);
            }
        } else if (initProcess.state == InitState.IN_PROCESS) {
            try {
                // can't be null in "IN PROCESS" state
                initProcess.countDownLatch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new FatalException("Unable to initialize process " + lockInfo + ". Init process was interrupted", e);
            }
        }
    }

    private static class InitProcess {

        private final InitState state;
        private final CountDownLatch countDownLatch;

        private InitProcess(InitState state, CountDownLatch countDownLatch) {
            this.state = state;
            this.countDownLatch = countDownLatch;
        }

        private InitProcess(InitState state) {
            this.state = state;
            this.countDownLatch = null;
        }

    }

    private enum InitState {

        IN_PROCESS,
        FINISHED

    }
}
