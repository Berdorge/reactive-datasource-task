package stream.scheduler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import stream.disposable.Disposable;
import stream.disposable.FutureDisposable;

public final class ExecutorServiceScheduler implements Scheduler {
    private final ExecutorService executorService;
    private final DirectScheduler delegate = new DirectScheduler();

    public ExecutorServiceScheduler(ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public Disposable schedule(Runnable runnable) {
        final Future<?> future = executorService.submit(runnable);
        return new FutureDisposable(future);
    }

    @Override
    public Disposable schedule(Runnable runnable, long delay, TimeUnit unit) {
        final Future<?> future = executorService.submit(() -> {
            delegate.schedule(runnable, delay, unit);
        });
        return new FutureDisposable(future);
    }
}
