package stream;

import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Consumer;
import stream.disposable.CompositeDisposable;
import stream.disposable.Disposable;
import stream.scheduler.Scheduler;
import stream.util.TriFunction;

// This whole class is way too simple and naive. But, well, I'm lazy.
final class ZipObservable<T, R> implements Observable<R> {
    private final Observable<T> source1;
    private final Observable<T> source2;
    private final Observable<T> source3;
    private final TriFunction<T, T, T, R> zipper;

    private final Queue<T> queue1;
    private final Queue<T> queue2;
    private final Queue<T> queue3;

    ZipObservable(
        Observable<T> source1,
        Observable<T> source2,
        Observable<T> source3,
        TriFunction<T, T, T, R> zipper
    ) {
        this.source1 = source1;
        this.source2 = source2;
        this.source3 = source3;
        this.zipper = zipper;
        this.queue1 = new LinkedList<>();
        this.queue2 = new LinkedList<>();
        this.queue3 = new LinkedList<>();
    }

    @Override
    public Observable<R> subscribeOn(Scheduler scheduler) {
        source1.subscribeOn(scheduler);
        source2.subscribeOn(scheduler);
        source3.subscribeOn(scheduler);
        return this;
    }

    @Override
    public Observable<R> observeOn(Scheduler scheduler) {
        source1.observeOn(scheduler);
        source2.observeOn(scheduler);
        source3.observeOn(scheduler);
        return this;
    }

    @Override
    public Observable<R> doOnComplete(Runnable runnable) {
        final CompletionObserver completionObserver = new CompletionObserver(runnable);
        source1.doOnComplete(completionObserver);
        source2.doOnComplete(completionObserver);
        source3.doOnComplete(completionObserver);
        return this;
    }

    @Override
    public Disposable subscribe(Consumer<R> consumer) {
        final CompositeDisposable disposable = new CompositeDisposable();
        disposable.add(source1.subscribe(new Observer(queue1, consumer)));
        disposable.add(source2.subscribe(new Observer(queue2, consumer)));
        disposable.add(source3.subscribe(new Observer(queue3, consumer)));
        return disposable;
    }

    private void tryEmit(Consumer<R> consumer) {
        if (queue1.isEmpty() || queue2.isEmpty() || queue3.isEmpty()) {
            return;
        }
        final T value1 = queue1.poll();
        final T value2 = queue2.poll();
        final T value3 = queue3.poll();
        final R value = zipper.apply(value1, value2, value3);
        consumer.accept(value);
    }

    private final class Observer implements Consumer<T> {
        private final Queue<T> queue;
        private final Consumer<R> consumer;

        Observer(Queue<T> queue, Consumer<R> consumer) {
            this.queue = queue;
            this.consumer = consumer;
        }

        @Override
        public void accept(T value) {
            queue.offer(value);
            tryEmit(consumer);
        }
    }

    private static final class CompletionObserver implements Runnable {
        private final Runnable runnable;
        private int completed = 0;

        CompletionObserver(Runnable runnable) {
            this.runnable = runnable;
        }

        @Override
        public void run() {
            completed++;
            if (completed == 3) {
                runnable.run();
            }
        }
    }
}
