package com.telda;

import jdk.jshell.spi.ExecutionControl;

import java.util.List;

public class Job<K, V> {
    K id;

    protected Job(K id) {

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

    public List<String> getLogs() throws ExecutionControl.NotImplementedException {
        throw new ExecutionControl.NotImplementedException("TODO");
    }

    public List<V> getReturnValues() throws ExecutionControl.NotImplementedException {
        throw new ExecutionControl.NotImplementedException("TODO");
    }

    public List<Double> getExecutionTimes() throws ExecutionControl.NotImplementedException {
        throw new ExecutionControl.NotImplementedException("TODO");
    }

    public enum JobStatus {
        PAUSED, RUNNING, WAITING, STOPPED
    }

    public JobStatus getStatus() throws ExecutionControl.NotImplementedException {
        throw new ExecutionControl.NotImplementedException("TODO");
    }

    public void addOnFinishCallback(IJobRunCallback<V> callback) throws ExecutionControl.NotImplementedException {
        throw new ExecutionControl.NotImplementedException("TODO");
    }
}
