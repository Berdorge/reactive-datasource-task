package stream.scheduler;

import java.util.concurrent.TimeUnit;
import stream.disposable.Disposable;

public interface Scheduler {
    Disposable schedule(Runnable runnable);

    Disposable schedule(Runnable runnable, long delay, TimeUnit unit);
}
