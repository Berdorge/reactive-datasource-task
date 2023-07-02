import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public final class MainThread extends AbstractExecutorService {
    private final Object lock = new Object();
    private final BlockingQueue<Runnable> queue;
    private final PoisonPill poisonPill = new PoisonPill();

    private Thread thread;
    private boolean isAcceptingNewWork = true;
    private boolean isRunning = true;

    public MainThread() {
        this.queue = new LinkedBlockingQueue<>();
    }

    @Override
    public void shutdown() {
        synchronized (lock) {
            if (isAcceptingNewWork) {
                isAcceptingNewWork = false;
                if (thread == null) {
                    poisonPill.run();
                } else {
                    queue.add(poisonPill);
                }
            }
        }
    }

    @Override
    public List<Runnable> shutdownNow() {
        final List<Runnable> notExecuted = new ArrayList<>();
        synchronized (lock) {
            if (thread == null) {
                poisonPill.run();
            } else {
                if (isAcceptingNewWork) {
                    isAcceptingNewWork = false;
                    queue.drainTo(notExecuted);
                    queue.add(poisonPill);
                } else {
                    queue.drainTo(notExecuted, queue.size() - 1);
                }
                thread.interrupt();
            }
        }
        return notExecuted;
    }

    @Override
    public boolean isShutdown() {
        synchronized (lock) {
            return !isAcceptingNewWork;
        }
    }

    @Override
    public boolean isTerminated() {
        synchronized (lock) {
            return !isRunning;
        }
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        final long start = System.currentTimeMillis();
        long millis = unit.toMillis(timeout);
        synchronized (lock) {
            while (isRunning && millis > 0) {
                lock.wait(millis);
                millis -= System.currentTimeMillis() - start;
            }
            return !isRunning;
        }
    }

    @Override
    public void execute(Runnable command) {
        synchronized (lock) {
            if (isAcceptingNewWork) {
                queue.add(command);
            } else {
                throw new IllegalStateException("Main thread has been shut down.");
            }
        }
    }

    public void run() {
        synchronized (lock) {
            if (isAcceptingNewWork) {
                if (thread == null) {
                    thread = Thread.currentThread();
                } else {
                    throw new IllegalStateException("Main thread has already been started.");
                }
            } else {
                throw new IllegalStateException("Main thread has been shut down.");
            }
        }
        while (isRunning) {
            try {
                final Runnable runnable = queue.take();
                runnable.run();
            } catch (InterruptedException e) {
                // ignore
            }
        }
    }

    private final class PoisonPill implements Runnable {
        @Override
        public void run() {
            synchronized (lock) {
                isRunning = false;
                lock.notifyAll();
            }
        }
    }
}