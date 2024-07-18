package com.telda;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Job<K, V> {
    K id;
    Integer runs;
    Long milliseconds = null;
    String cron = null;
    Callable<V> func;
    Thread mainThread;
    ArrayList<Thread> jobThreads = new ArrayList<>();
    Logger logger = Logger.getLogger(Job.class.getName());

    public enum JobStatus {
        PAUSED, RUNNING, WAITING, STOPPED
    }
    JobStatus status = JobStatus.WAITING;
    ArrayList<V> returnValues = new ArrayList<>();
    ArrayList<Double> executionTimes = new ArrayList<>();
    ArrayList<IJobRunCallback<V>> callbacks = new ArrayList<>();

    protected Job(K id, Integer runs, long milliseconds, Callable<V> func) {
        validate(runs, milliseconds);
        this.id = id;
        this.runs = runs;
        this.milliseconds = milliseconds;
        this.func = func;
        setup();
    }

    protected Job(K id, Integer runs, String cron, Callable<V> func) {
        validate(runs, null);
        this.id = id;
        this.runs = runs;
        this.cron = cron;
        this.func = func;
        setup();
    }

    private static void validate(Integer runs, Long milliseconds) {
        if (milliseconds != null && milliseconds < 0) {
            throw new IllegalArgumentException("Milliseconds can't be negative");
        }
        if (runs != null && runs < 0) {
            throw new IllegalArgumentException("Runs can't be negative");
        }
    }

    private void setup() {
        mainThread = new Thread(() -> {
            try {
                while(runs == null || runs > 0){
                    Thread.sleep(calculateWait());
                    if (runs != null)
                        runs--;
                    Thread job = getNewJobThread();
                    jobThreads.add(job);
                }
                status = calculateStatus();
            } catch (InterruptedException e) {
                logger.info("Cron job interrupted");
            }
        });
        mainThread.start();
    }

    private Thread getNewJobThread() {
        Thread job = new Thread(() -> {
            status = calculateStatus();
            logger.info("Started Cron job");
            try {
                long startTime = System.nanoTime();
                V ret = func.call();
                long endTime = System.nanoTime();
                returnValues.add(ret);
                double executionTime = (endTime - startTime) * 1e-6; // Nano to milli
                executionTimes.add(executionTime);
                for (IJobRunCallback<V> callback: callbacks) {
                    callback.callback(ret, executionTime);
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error occurred while execution", e);
            }
            logger.info("Finished Cron job");
        });
        job.start();
        new Thread(() -> {
            try {
                job.join();
            } catch (InterruptedException e) {
                logger.log(Level.SEVERE, "Waiter thread got interrupted");
            }
            status = calculateStatus();
        }).start();
        return job;
    }

    private long calculateWait() {
        if (milliseconds != null) {
            return milliseconds;
        }
        if (cron != null) {
            // TODO: calculate next cron
        }
        throw new IllegalArgumentException("No delay nor Cron expression was provided");
    }

    private JobStatus calculateStatus() {
        jobThreads.removeIf((thread) ->
                !thread.isAlive()
        );

        if (status == JobStatus.PAUSED) {
            return JobStatus.PAUSED;
        }

        if (jobThreads.isEmpty() && runs != null && runs == 0) {
            return JobStatus.STOPPED;
        }

        return jobThreads.isEmpty() ? JobStatus.WAITING : JobStatus.RUNNING;
    }

    public K getId() {
        return id;
    }

    public void pause() {
        mainThread.interrupt();
        status = JobStatus.PAUSED;
    }

    public void resume(boolean resetTimer) {

    }

    public void resume() {
        resume(false);
    }

    public void stop() {
        mainThread.interrupt();
        runs = 0;
        status = calculateStatus();
    }

    public List<V> getReturnValues() {
        return returnValues;
    }

    public List<Double> getExecutionTimes() {
        return executionTimes;
    }

    public JobStatus getStatus() {
        return status;
    }

    public void addOnFinishCallback(IJobRunCallback<V> callback) {
        callbacks.add(callback);
    }

    public void deleteOnFinishCallback(IJobRunCallback<V> callback) {
        callbacks.remove(callback);
    }
}
