package com.example.bankingapp.utils;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Two execution contexts the app needs:
 *  - {@link #io()}   for blocking JDBC calls (never on the Android main thread).
 *  - {@link #main()} to post LiveData updates / UI work back to main.
 *
 * In unit tests pass a synchronous executor like {@code Runnable::run} so
 * ViewModel methods complete before the test moves on — no latches needed.
 */
public class AppExecutors {

    private static AppExecutors instance;

    public static synchronized AppExecutors get() {
        if (instance == null) {
            Handler mainHandler = new Handler(Looper.getMainLooper());
            instance = new AppExecutors(
                    Executors.newFixedThreadPool(4),
                    mainHandler::post);
        }
        return instance;
    }

    private final Executor io;
    private final Executor main;

    /** Public for tests — inject synchronous executors. */
    public AppExecutors(Executor io, Executor main) {
        this.io = io;
        this.main = main;
    }

    public Executor io() { return io; }
    public Executor main() { return main; }
}
