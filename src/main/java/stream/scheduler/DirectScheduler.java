package stream.scheduler;

import java.util.concurrent.TimeUnit;
import stream.disposable.Disposable;
import stream.disposable.EmptyDisposable;

public final class DirectScheduler implements Scheduler {
    @Override
    public Disposable schedule(Runnable runnable) {
        runnable.run();
        return EmptyDisposable.INSTANCE;
    }

    @Override
    public Disposable schedule(Runnable runnable, long delay, TimeUnit unit) {
        boolean shouldContinue = true;
        try {
            Thread.sleep(unit.toMillis(delay));
        } catch (InterruptedException e) {
            shouldContinue = false;
            Thread.currentThread().interrupt();
        }
        if (shouldContinue) {
            runnable.run();
        }
        return EmptyDisposable.INSTANCE;
    }
}
