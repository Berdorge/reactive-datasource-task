import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import stream.DataSource;
import stream.Observable;
import stream.scheduler.ExecutorServiceScheduler;
import stream.scheduler.Scheduler;

public final class Main {
    public static void main(String[] args) {
        int T = 5;
        if (args.length == 0) {
            System.out.println("No arguments provided.");
        } else {
            try {
                T = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("Couldn't parse first argument as integer.");
            }
        }
        System.out.println("Using T = " + T + ".");
        dataSourceTask(T);
    }

    private static void dataSourceTask(int T) {
        final MainThread mainThread = new MainThread();
        final Random random = new Random();
        final List<ExecutorService> executors = List.of(
            Executors.newSingleThreadExecutor(),
            Executors.newSingleThreadExecutor(),
            Executors.newSingleThreadExecutor()
        );
        final List<DataSource> dataSources = List.of(
            new DataSource(T, random),
            new DataSource(T, random),
            new DataSource(T, random)
        );
        final Scheduler observationScheduler = new ExecutorServiceScheduler(mainThread);
        for (int i = 0; i < executors.size(); i++) {
            dataSources.get(i)
                .subscribeOn(new ExecutorServiceScheduler(executors.get(i)));
        }
        Observable.zip(
                dataSources.get(0),
                dataSources.get(1),
                dataSources.get(2),
                (a, b, c) -> String.format("%d,%d,%d", a, b, c)
            )
            .observeOn(observationScheduler)
            .doOnComplete(mainThread::shutdown)
            .subscribe(System.out::println);
        mainThread.run();
        for (ExecutorService executor : executors) {
            executor.shutdown();
        }
    }
}
