package stream;

import java.util.function.Consumer;
import stream.disposable.Disposable;
import stream.scheduler.Scheduler;
import stream.util.TriFunction;

public interface Observable<T> {
    static <T, R> Observable<R> zip(
        Observable<T> observable1,
        Observable<T> observable2,
        Observable<T> observable3,
        TriFunction<T, T, T, R> zipper
    ) {
        return new ZipObservable<>(observable1, observable2, observable3, zipper);
    }

    Observable<T> subscribeOn(Scheduler scheduler);

    Observable<T> observeOn(Scheduler scheduler);

    Observable<T> doOnComplete(Runnable runnable);

    Disposable subscribe(Consumer<T> consumer);
}
