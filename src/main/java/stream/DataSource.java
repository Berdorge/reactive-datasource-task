package stream;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import stream.disposable.CompositeDisposable;
import stream.disposable.Disposable;
import stream.scheduler.DirectScheduler;
import stream.scheduler.Scheduler;

public final class DataSource implements Observable<Integer> {
    private final static long INTERVAL_ORIGIN_NANOS = TimeUnit.SECONDS.toNanos(1);
    private final static long INTERVAL_BOUND_NANOS = TimeUnit.SECONDS.toNanos(5) + 1;

    private final int T;
    private final Random random;
    private Scheduler subscriptionScheduler;
    private Scheduler observationScheduler;
    private Runnable completionRunnable;

    public DataSource(int T) {
        this(T, new Random());
    }

    public DataSource(int T, Random random) {
        this.T = T;
        this.random = random;
        this.subscriptionScheduler = new DirectScheduler();
        this.observationScheduler = new DirectScheduler();
    }

    @Override
    public DataSource subscribeOn(Scheduler scheduler) {
        this.subscriptionScheduler = scheduler;
        return this;
    }

    @Override
    public DataSource observeOn(Scheduler scheduler) {
        this.observationScheduler = scheduler;
        return this;
    }

    @Override
    public Observable<Integer> doOnComplete(Runnable runnable) {
        this.completionRunnable = runnable;
        return this;
    }

    @Override
    public Disposable subscribe(Consumer<Integer> consumer) {
        final CompositeDisposable disposable = new CompositeDisposable();
        scheduleNextEmission(disposable, consumer, 0);
        return disposable;
    }

    private void scheduleNextEmission(
        CompositeDisposable disposable,
        Consumer<Integer> consumer,
        int emission
    ) {
        if (emission < T) {
            final long interval = random.nextLong(INTERVAL_ORIGIN_NANOS, INTERVAL_BOUND_NANOS);
            final Runnable runnable = new EmissionRunnable(disposable, consumer, emission);
            disposable.add(subscriptionScheduler.schedule(runnable, interval, TimeUnit.NANOSECONDS));
        } else {
            disposable.add(observationScheduler.schedule(completionRunnable));
        }
    }

    private final class EmissionRunnable implements Runnable {
        private final CompositeDisposable disposable;
        private final Consumer<Integer> consumer;
        private final int emission;

        public EmissionRunnable(CompositeDisposable disposable, Consumer<Integer> consumer, int emission) {
            this.disposable = disposable;
            this.consumer = consumer;
            this.emission = emission;
        }

        @Override
        public void run() {
            final int value = random.nextInt();
            disposable.add(observationScheduler.schedule(() -> consumer.accept(value)));
            scheduleNextEmission(disposable, consumer, emission + 1);
        }
    }
}
