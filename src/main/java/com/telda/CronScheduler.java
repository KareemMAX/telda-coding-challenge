package com.telda;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class CronScheduler {
    @SuppressWarnings("rawtypes")
    protected static final HashMap<Object, Job> store = new HashMap<>();

    public static <K, V> Job<K,V> runOnce(Callable<V> job, long delay, TimeUnit unit, K id) {
        return run(job, delay, unit, 1, id);
    }

    public static <V> Job<String, V> runOnce(Callable<V> job, long delay, TimeUnit unit) {
        String uuid = UUID.randomUUID().toString();
        return runOnce(job, delay, unit, uuid);
    }

    public static <K, V> Job<K, V> run(Callable<V> job, long delay, TimeUnit unit, K id) {
        return saveAndReturn(new Job<>(id, null, TimeUnit.MILLISECONDS.convert(delay, unit), job));
    }

    public static <K, V> Job<K, V> run(Callable<V> job, long delay, TimeUnit unit, int maxRuns, K id) {
        return saveAndReturn(new Job<>(id, maxRuns, TimeUnit.MILLISECONDS.convert(delay, unit), job));
    }

    public static <V> Job<String, V> run(Callable<V> job, long delay, TimeUnit unit) {
        String uuid = UUID.randomUUID().toString();
        return run(job, delay, unit, uuid);
    }

    public static <V> Job<String, V> run(Callable<V> job, long delay, TimeUnit unit, int maxRuns) {
        String uuid = UUID.randomUUID().toString();
        return run(job, delay, unit, maxRuns, uuid);
    }

    @SuppressWarnings("unchecked")
    public static <K, V> Job<K, V> getJob(K id) {
        return store.get(id);
    }

    private static <K, V> Job<K, V> saveAndReturn(Job<K, V> job) {
        if (store.containsKey(job.getId())) {
            throw new IllegalArgumentException("Duplicate key for job");
        }

        store.put(job.getId(), job);

        return job;
    }
}
