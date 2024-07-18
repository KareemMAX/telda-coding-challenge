package com.telda;

import java.util.List;

public interface IJobRunCallback<V> {
    void callback(V result, double executionTime);
}
