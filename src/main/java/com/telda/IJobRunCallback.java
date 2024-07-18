package com.telda;

public interface IJobRunCallback<V> {
    void callback(V result, double executionTime);
}
