package stream.disposable;

import java.util.ArrayList;
import java.util.Collection;

public final class CompositeDisposable implements Disposable {
    // We don't need a Collection: an interface supporting add() and drainTo() would be enough.
    // BlockingQueue, as well as a synchronized Collection (used now) are an overkill.
    // In theory a buffed ConcurrentLinkedQueue would be the best choice. But, well, I'm lazy.
    private Collection<Disposable> disposables;

    public CompositeDisposable() {
        disposables = new ArrayList<>();
    }

    @Override
    public void dispose() {
        final Collection<Disposable> disposables;
        synchronized (this) {
            disposables = this.disposables;
            this.disposables = new ArrayList<>();
        }
        for (Disposable disposable : disposables) {
            disposable.dispose();
        }
    }

    public void add(Disposable disposable) {
        synchronized (this) {
            disposables.add(disposable);
        }
    }
}
