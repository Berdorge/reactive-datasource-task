package stream.disposable;

public final class EmptyDisposable implements Disposable {
    public static final EmptyDisposable INSTANCE = new EmptyDisposable();

    private EmptyDisposable() {
        // singleton
    }

    @Override
    public void dispose() {
        // do nothing
    }
}
