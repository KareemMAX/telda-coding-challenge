package com.telda;

import jdk.jshell.spi.ExecutionControl;

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
    Thread thread;
    Logger logger = Logger.getLogger(Job.class.getName());

    public enum JobStatus {
        PAUSED, RUNNING, WAITING, STOPPED
    }
    JobStatus status = JobStatus.WAITING;
    ArrayList<V> returnValues = new ArrayList<>();
    ArrayList<Double> executionTimes = new ArrayList<>();

    protected Job(K id, Integer runs, long milliseconds, Callable<V> func) {
        this.id = id;
        this.runs = runs;
        this.milliseconds = milliseconds;
        this.func = func;
        setup();
    }

    protected Job(K id, Integer runs, String cron, Callable<V> func) {
        this.id = id;
        this.runs = runs;
        this.cron = cron;
        this.func = func;
        setup();
    }

    void setup() {
        thread = new Thread(() -> {
            try {
                while(runs > 0){
                    Thread.sleep(calculateWait());
                    runs--;
                    status = JobStatus.RUNNING;
                    V ret = func.call();
                    returnValues.add(ret);
                    status = JobStatus.WAITING;
                }
                status = JobStatus.STOPPED;
            } catch (InterruptedException e) {
                logger.warning("Cron job interrupted");
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error occurred while execution", e);
            }
        });
        thread.start();
    }

    long calculateWait() {
        if (milliseconds != null) {
            return milliseconds;
        }
        if (cron != null) {
            // TODO: calculate next cron
        }
        throw new IllegalArgumentException("No delay nor Cron expression was provided");
    }

    public K getId() {
        return id;
    }

    public void pause() throws ExecutionControl.NotImplementedException {
        throw new ExecutionControl.NotImplementedException("TODO");
    }

    public void resume(boolean resetTimer) throws ExecutionControl.NotImplementedException {
        throw new ExecutionControl.NotImplementedException("TODO");
    }

    public void resume() throws ExecutionControl.NotImplementedException {
        resume(false);
    }

    public void stop() throws ExecutionControl.NotImplementedException {
        throw new ExecutionControl.NotImplementedException("TODO");
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

    public void addOnFinishCallback(IJobRunCallback<V> callback) throws ExecutionControl.NotImplementedException {
        throw new ExecutionControl.NotImplementedException("TODO");
    }
}
