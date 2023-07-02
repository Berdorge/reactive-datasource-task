package stream.disposable;

import java.util.concurrent.Future;

public final class FutureDisposable implements Disposable {
    private final Future<?> future;

    public FutureDisposable(Future<?> future) {
        this.future = future;
    }

    @Override
    public void dispose() {
        future.cancel(true);
    }
}
