package com.telda;

import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class CronScheduler {
    public static <K, V> Job<K,V> runOnce(Callable<V> job, long delay, TimeUnit unit, K id) {
        return run(job, delay, unit, 1, id);
    }

    public static <V> Job<String, V> runOnce(Callable<V> job, long delay, TimeUnit unit) {
        String uuid = UUID.randomUUID().toString();
        return runOnce(job, delay, unit, uuid);
    }

    public static <K, V> Job<K, V> run(Callable<V> job, long delay, TimeUnit unit, K id) {
        return new Job<>(id, null, TimeUnit.MILLISECONDS.convert(delay, unit), job);
    }

    public static <K, V> Job<K, V> run(Callable<V> job, long delay, TimeUnit unit, int maxRuns, K id) {
        return new Job<>(id, maxRuns, TimeUnit.MILLISECONDS.convert(delay, unit), job);
    }

    public static <V> Job<String, V> run(Callable<V> job, long delay, TimeUnit unit) {
        String uuid = UUID.randomUUID().toString();
        return run(job, delay, unit, uuid);
    }

    public static <V> Job<String, V> run(Callable<V> job, long delay, TimeUnit unit, int maxRuns) {
        String uuid = UUID.randomUUID().toString();
        return run(job, delay, unit, maxRuns, uuid);
    }

    public static <K, V> Job<K, V> run(Callable<V> job, String cron, K id) {
        return new Job<>(id, null, cron, job);
    }

    public static <K, V> Job<K, V> run(Callable<V> job, String cron, int maxRuns, K id) {
        return new Job<>(id, maxRuns, cron, job);
    }

    public static <V> Job<String, V> run(Callable<V> job, String cron) {
        String uuid = UUID.randomUUID().toString();
        return run(job, cron, uuid);
    }

    public static <V> Job<String, V> run(Callable<V> job, String cron, int maxRuns) {
        String uuid = UUID.randomUUID().toString();
        return run(job, cron, maxRuns, uuid);
    }

    public static <K, V> Job<K, V> getJob(K id) {
        return null;
    }
}
