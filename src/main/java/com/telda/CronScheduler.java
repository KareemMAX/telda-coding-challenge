package com.telda;

import jdk.jshell.spi.ExecutionControl;

import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class CronScheduler {
    public static <K, V> Job<K,V> runOnce(Callable<V> job, long delay, TimeUnit unit, K id) throws ExecutionControl.NotImplementedException {
        throw new ExecutionControl.NotImplementedException("TODO");
    }

    public static <V> Job<String, V> runOnce(Callable<V> job, long delay, TimeUnit unit) throws ExecutionControl.NotImplementedException {
        String uuid = UUID.randomUUID().toString();
        return runOnce(job, delay, unit, uuid);
    }

    public static <K, V> Job<K, V> run(Callable<V> job, long delay, TimeUnit unit, K id) throws ExecutionControl.NotImplementedException {
        throw new ExecutionControl.NotImplementedException("TODO");
    }

    public static <K, V> Job<K, V> run(Callable<V> job, long delay, TimeUnit unit, int maxRuns, K id) throws ExecutionControl.NotImplementedException {
        throw new ExecutionControl.NotImplementedException("TODO");
    }

    public static <V> Job<String, V> run(Callable<V> job, long delay, TimeUnit unit) throws ExecutionControl.NotImplementedException {
        String uuid = UUID.randomUUID().toString();
        return run(job, delay, unit, uuid);
    }

    public static <V> Job<String, V> run(Callable<V> job, long delay, TimeUnit unit, int maxRuns) throws ExecutionControl.NotImplementedException {
        String uuid = UUID.randomUUID().toString();
        return run(job, delay, unit, maxRuns, uuid);
    }

    public static <K, V> Job<K, V> run(Callable<V> job, String cron, K id) throws ExecutionControl.NotImplementedException {
        throw new ExecutionControl.NotImplementedException("TODO");
    }

    public static <K, V> Job<K, V> run(Callable<V> job, String cron, int maxRuns, K id) throws ExecutionControl.NotImplementedException {
        throw new ExecutionControl.NotImplementedException("TODO");
    }

    public static <V> Job<String, V> run(Callable<V> job, String cron) throws ExecutionControl.NotImplementedException {
        String uuid = UUID.randomUUID().toString();
        return run(job, cron, uuid);
    }

    public static <V> Job<String, V> run(Callable<V> job, String cron, int maxRuns) throws ExecutionControl.NotImplementedException {
        String uuid = UUID.randomUUID().toString();
        return run(job, cron, maxRuns, uuid);
    }

    public static <K, V> Job<K, V> getJob(K id) throws ExecutionControl.NotImplementedException {
        throw new ExecutionControl.NotImplementedException("TODO");
    }
}
